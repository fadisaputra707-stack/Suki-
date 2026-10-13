package com.example.data.repository

import com.example.data.local.AutoRuleEntity
import com.example.data.local.BudgetEntity
import com.example.data.local.DanaKuDatabase
import com.example.data.local.SavingGoalEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.WalletEntity
import com.example.data.model.AutoRule
import com.example.data.model.CategoryBudget
import com.example.data.model.ParsedTransactionResult
import com.example.data.model.SavingGoal
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.data.model.WalletAccount
import com.example.data.parser.AutoTransactionParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class FinanceRepository(private val database: DanaKuDatabase) {

    private val walletDao = database.walletDao()
    private val transactionDao = database.transactionDao()
    private val budgetDao = database.budgetDao()
    private val autoRuleDao = database.autoRuleDao()
    private val savingGoalDao = database.savingGoalDao()

    val walletsFlow: Flow<List<WalletAccount>> = walletDao.getAllWallets().map { list ->
        list.map { it.toDomain() }
    }

    val transactionsFlow: Flow<List<TransactionItem>> = transactionDao.getAllTransactions().map { list ->
        list.map { it.toDomain() }
    }

    val budgetsFlow: Flow<List<CategoryBudget>> = budgetDao.getAllBudgets().map { list ->
        list.map { it.toDomain() }
    }

    val autoRulesFlow: Flow<List<AutoRule>> = autoRuleDao.getAllRules().map { list ->
        list.map { it.toDomain() }
    }

    val savingGoalsFlow: Flow<List<SavingGoal>> = savingGoalDao.getAllGoals().map { list ->
        list.map { it.toDomain() }
    }

    /**
     * Add a transaction and automatically adjust the wallet balance(s).
     */
    suspend fun addTransaction(transaction: TransactionItem) {
        transactionDao.insertTransaction(TransactionEntity.fromDomain(transaction))

        when (transaction.type) {
            TransactionType.EXPENSE -> {
                val wallet = walletDao.getWalletById(transaction.sourceWalletId)
                if (wallet != null) {
                    val newBalance = wallet.balance - transaction.amount
                    walletDao.updateBalance(wallet.id, newBalance)
                }
            }
            TransactionType.INCOME -> {
                val wallet = walletDao.getWalletById(transaction.sourceWalletId)
                if (wallet != null) {
                    val newBalance = wallet.balance + transaction.amount
                    walletDao.updateBalance(wallet.id, newBalance)
                }
            }
            TransactionType.TRANSFER -> {
                val sourceWallet = walletDao.getWalletById(transaction.sourceWalletId)
                if (sourceWallet != null) {
                    walletDao.updateBalance(sourceWallet.id, sourceWallet.balance - transaction.amount)
                }
                if (transaction.targetWalletId != null) {
                    val targetWallet = walletDao.getWalletById(transaction.targetWalletId)
                    if (targetWallet != null) {
                        walletDao.updateBalance(targetWallet.id, targetWallet.balance + transaction.amount)
                    }
                }
            }
        }
    }

    /**
     * Delete a transaction and reverse its effect on wallet balance.
     */
    suspend fun deleteTransaction(tx: TransactionItem) {
        transactionDao.deleteTransaction(tx.id)

        when (tx.type) {
            TransactionType.EXPENSE -> {
                val wallet = walletDao.getWalletById(tx.sourceWalletId)
                if (wallet != null) {
                    walletDao.updateBalance(wallet.id, wallet.balance + tx.amount)
                }
            }
            TransactionType.INCOME -> {
                val wallet = walletDao.getWalletById(tx.sourceWalletId)
                if (wallet != null) {
                    walletDao.updateBalance(wallet.id, wallet.balance - tx.amount)
                }
            }
            TransactionType.TRANSFER -> {
                val sourceWallet = walletDao.getWalletById(tx.sourceWalletId)
                if (sourceWallet != null) {
                    walletDao.updateBalance(sourceWallet.id, sourceWallet.balance + tx.amount)
                }
                if (tx.targetWalletId != null) {
                    val targetWallet = walletDao.getWalletById(tx.targetWalletId)
                    if (targetWallet != null) {
                        walletDao.updateBalance(targetWallet.id, targetWallet.balance - tx.amount)
                    }
                }
            }
        }
    }

    suspend fun addWallet(wallet: WalletAccount) {
        walletDao.insertWallet(WalletEntity.fromDomain(wallet))
    }

    suspend fun updateWallet(wallet: WalletAccount) {
        walletDao.updateWallet(WalletEntity.fromDomain(wallet))
    }

    suspend fun topUpWallet(walletId: String, amount: Long) {
        val wallet = walletDao.getWalletById(walletId) ?: return
        val updated = wallet.balance + amount
        walletDao.updateBalance(walletId, updated)

        // Also record top up transaction
        val tx = TransactionItem(
            id = UUID.randomUUID().toString(),
            title = "Top Up Saldo ${wallet.name}",
            amount = amount,
            type = TransactionType.INCOME,
            sourceWalletId = walletId,
            targetWalletId = null,
            category = "Gaji & Pemasukan",
            timestamp = System.currentTimeMillis(),
            notes = "Top Up via Virtual Account / ATM",
            isAutoDetected = false,
            detectedSource = "Top Up Manual"
        )
        transactionDao.insertTransaction(TransactionEntity.fromDomain(tx))
    }

    suspend fun transferBetweenWallets(fromWalletId: String, toWalletId: String, amount: Long, note: String) {
        val fromWallet = walletDao.getWalletById(fromWalletId) ?: return
        val toWallet = walletDao.getWalletById(toWalletId) ?: return

        walletDao.updateBalance(fromWalletId, fromWallet.balance - amount)
        walletDao.updateBalance(toWalletId, toWallet.balance + amount)

        val tx = TransactionItem(
            id = UUID.randomUUID().toString(),
            title = "Transfer ke ${toWallet.name}",
            amount = amount,
            type = TransactionType.TRANSFER,
            sourceWalletId = fromWalletId,
            targetWalletId = toWalletId,
            category = "Transfer Antar Dompet",
            timestamp = System.currentTimeMillis(),
            notes = note.ifEmpty { "Transfer dana dari ${fromWallet.name} ke ${toWallet.name}" },
            isAutoDetected = false,
            detectedSource = "Transfer DanaKu"
        )
        transactionDao.insertTransaction(TransactionEntity.fromDomain(tx))
    }

    suspend fun setBudget(budget: CategoryBudget) {
        budgetDao.insertBudget(BudgetEntity.fromDomain(budget))
    }

    suspend fun deleteBudget(category: String) {
        budgetDao.deleteBudget(category)
    }

    suspend fun saveAutoRule(rule: AutoRule) {
        autoRuleDao.insertRule(AutoRuleEntity.fromDomain(rule))
    }

    suspend fun toggleAutoRule(ruleId: String, currentActive: Boolean) {
        // Find rule, update isActive
        val rules = database.autoRuleDao()
        // Simple update
    }

    suspend fun deleteAutoRule(id: String) {
        autoRuleDao.deleteRule(id)
    }

    suspend fun executeRuleNow(rule: AutoRule): TransactionItem {
        val tx = TransactionItem(
            id = UUID.randomUUID().toString(),
            title = rule.title,
            amount = rule.amount,
            type = rule.type,
            sourceWalletId = rule.targetWalletId,
            targetWalletId = null,
            category = rule.category,
            timestamp = System.currentTimeMillis(),
            notes = "Dijalankan dari Aturan Otomatis (${rule.recurrenceType})",
            isAutoDetected = true,
            detectedSource = "Aturan Otomatis (${rule.title})"
        )
        addTransaction(tx)
        return tx
    }

    suspend fun saveSavingGoal(goal: SavingGoal) {
        savingGoalDao.insertGoal(SavingGoalEntity.fromDomain(goal))
    }

    suspend fun depositToSavingGoal(goalId: String, walletId: String, amount: Long) {
        val goals = savingGoalDao
        val wallet = walletDao.getWalletById(walletId) ?: return
        walletDao.updateBalance(walletId, wallet.balance - amount)

        // Also record transaction
        val tx = TransactionItem(
            id = UUID.randomUUID().toString(),
            title = "Setoran Tabungan",
            amount = amount,
            type = TransactionType.EXPENSE,
            sourceWalletId = walletId,
            targetWalletId = null,
            category = "Investasi & Dividen",
            timestamp = System.currentTimeMillis(),
            notes = "Alokasi Celengan Impian",
            isAutoDetected = false,
            detectedSource = "Setoran Celengan"
        )
        transactionDao.insertTransaction(TransactionEntity.fromDomain(tx))
    }

    /**
     * Process auto-parsed result into an actual saved transaction
     */
    suspend fun commitParsedResult(result: ParsedTransactionResult, walletId: String): TransactionItem {
        val tx = TransactionItem(
            id = UUID.randomUUID().toString(),
            title = result.title,
            amount = result.amount,
            type = result.type,
            sourceWalletId = walletId,
            targetWalletId = null,
            category = result.category,
            timestamp = System.currentTimeMillis(),
            notes = result.notes,
            isAutoDetected = true,
            detectedSource = "${result.detectedWalletName} Auto-Parser"
        )
        addTransaction(tx)
        return tx
    }
}
