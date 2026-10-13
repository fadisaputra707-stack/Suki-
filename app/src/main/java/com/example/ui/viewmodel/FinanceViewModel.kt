package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DanaKuDatabase
import com.example.data.local.SavingGoalEntity
import com.example.data.model.AutoRule
import com.example.data.model.CategoryBudget
import com.example.data.model.ParsedTransactionResult
import com.example.data.model.SavingGoal
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.data.model.WalletAccount
import com.example.data.parser.AutoTransactionParser
import com.example.data.parser.NotificationSimulationTemplate
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class FinanceUiState(
    val wallets: List<WalletAccount> = emptyList(),
    val transactions: List<TransactionItem> = emptyList(),
    val budgets: List<CategoryBudget> = emptyList(),
    val autoRules: List<AutoRule> = emptyList(),
    val savingGoals: List<SavingGoal> = emptyList(),
    val isBalanceHidden: Boolean = false,
    val selectedWalletFilterId: String? = null,
    val selectedTypeFilter: TransactionType? = null,
    val filterOnlyAutoDetected: Boolean = false,
    val searchQuery: String = "",
    val activeSimulation: NotificationSimulationTemplate? = null,
    val parsedPreview: ParsedTransactionResult? = null,
    val userMessage: String? = null,
    val selectedNavTab: Int = 0 // 0: Beranda, 1: Deteksi Otomatis, 2: Transaksi, 3: Analisis, 4: Celengan
) {
    val totalBalance: Long get() = wallets.sumOf { it.balance }

    val monthlyIncome: Long get() = transactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }

    val monthlyExpense: Long get() = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }

    val autoDetectedTransactionsCount: Int get() = transactions
        .count { it.isAutoDetected }

    val filteredTransactions: List<TransactionItem> get() {
        return transactions.filter { item ->
            val matchWallet = selectedWalletFilterId == null || item.sourceWalletId == selectedWalletFilterId
            val matchType = selectedTypeFilter == null || item.type == selectedTypeFilter
            val matchAuto = !filterOnlyAutoDetected || item.isAutoDetected
            val matchQuery = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true) ||
                    item.notes.contains(searchQuery, ignoreCase = true)
            matchWallet && matchType && matchAuto && matchQuery
        }
    }
}

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DanaKuDatabase.getInstance(application)
    private val repository = FinanceRepository(database)

    private val _isBalanceHidden = MutableStateFlow(false)
    private val _selectedWalletFilterId = MutableStateFlow<String?>(null)
    private val _selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    private val _filterOnlyAutoDetected = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _activeSimulation = MutableStateFlow<NotificationSimulationTemplate?>(null)
    private val _parsedPreview = MutableStateFlow<ParsedTransactionResult?>(null)
    private val _userMessage = MutableStateFlow<String?>(null)
    private val _selectedNavTab = MutableStateFlow(0)

    val uiState: StateFlow<FinanceUiState> = combine(
        repository.walletsFlow,
        repository.transactionsFlow,
        repository.budgetsFlow,
        repository.autoRulesFlow,
        repository.savingGoalsFlow,
        _isBalanceHidden,
        _selectedWalletFilterId,
        _selectedTypeFilter,
        _filterOnlyAutoDetected,
        _searchQuery,
        _activeSimulation,
        _parsedPreview,
        _userMessage,
        _selectedNavTab
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        FinanceUiState(
            wallets = args[0] as List<WalletAccount>,
            transactions = args[1] as List<TransactionItem>,
            budgets = args[2] as List<CategoryBudget>,
            autoRules = args[3] as List<AutoRule>,
            savingGoals = args[4] as List<SavingGoal>,
            isBalanceHidden = args[5] as Boolean,
            selectedWalletFilterId = args[6] as String?,
            selectedTypeFilter = args[7] as TransactionType?,
            filterOnlyAutoDetected = args[8] as Boolean,
            searchQuery = args[9] as String,
            activeSimulation = args[10] as NotificationSimulationTemplate?,
            parsedPreview = args[11] as ParsedTransactionResult?,
            userMessage = args[12] as String?,
            selectedNavTab = args[13] as Int
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FinanceUiState()
    )

    fun toggleBalanceVisibility() {
        _isBalanceHidden.value = !_isBalanceHidden.value
    }

    fun setNavTab(tab: Int) {
        _selectedNavTab.value = tab
    }

    fun setWalletFilter(walletId: String?) {
        _selectedWalletFilterId.value = walletId
    }

    fun setTypeFilter(type: TransactionType?) {
        _selectedTypeFilter.value = type
    }

    fun toggleFilterOnlyAutoDetected() {
        _filterOnlyAutoDetected.value = !_filterOnlyAutoDetected.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun dismissSimulation() {
        _activeSimulation.value = null
        _parsedPreview.value = null
    }

    /**
     * Triggers a realistic push notification simulation and automatically parses it.
     */
    fun triggerSimulation(template: NotificationSimulationTemplate) {
        _activeSimulation.value = template
        val parsed = AutoTransactionParser.parseMessage(template.messageText)
        _parsedPreview.value = parsed
    }

    /**
     * Parses custom input text (SMS, Chat, OCR) in real-time.
     */
    fun parseCustomText(text: String) {
        if (text.isBlank()) {
            _parsedPreview.value = null
            return
        }
        val result = AutoTransactionParser.parseMessage(text)
        _parsedPreview.value = result
    }

    /**
     * Commits the currently parsed result as a real transaction into the selected or auto-detected wallet.
     */
    fun confirmAndSaveParsedResult(customWalletId: String? = null) {
        val preview = _parsedPreview.value ?: return
        viewModelScope.launch {
            val wallets = uiState.value.wallets
            val walletToUse = customWalletId
                ?: wallets.find { it.name.contains(preview.detectedWalletName, ignoreCase = true) }?.id
                ?: wallets.find { it.isDefault }?.id
                ?: wallets.firstOrNull()?.id
                ?: "wallet_dana"

            repository.commitParsedResult(preview, walletToUse)
            _activeSimulation.value = null
            _parsedPreview.value = null
            _userMessage.value = "Berhasil! Transaksi ${preview.title} (Rp ${preview.amount}) tercatat otomatis."
        }
    }

    fun addManualTransaction(
        title: String,
        amount: Long,
        type: TransactionType,
        sourceWalletId: String,
        targetWalletId: String? = null,
        category: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val tx = TransactionItem(
                id = UUID.randomUUID().toString(),
                title = title.ifEmpty { if (type == TransactionType.INCOME) "Pemasukan" else "Pengeluaran" },
                amount = amount,
                type = type,
                sourceWalletId = sourceWalletId,
                targetWalletId = targetWalletId,
                category = category,
                timestamp = System.currentTimeMillis(),
                notes = notes,
                isAutoDetected = false,
                detectedSource = "Manual"
            )
            repository.addTransaction(tx)
            _userMessage.value = "Transaksi berhasil dicatat"
        }
    }

    fun deleteTransaction(tx: TransactionItem) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
            _userMessage.value = "Transaksi dihapus, saldo dompet telah disesuaikan"
        }
    }

    fun topUpWallet(walletId: String, amount: Long) {
        viewModelScope.launch {
            repository.topUpWallet(walletId, amount)
            _userMessage.value = "Top up saldo berhasil ditambahkan"
        }
    }

    fun transferBetweenWallets(fromWalletId: String, toWalletId: String, amount: Long, note: String) {
        viewModelScope.launch {
            repository.transferBetweenWallets(fromWalletId, toWalletId, amount, note)
            _userMessage.value = "Transfer antar dompet berhasil"
        }
    }

    fun saveBudget(category: String, limit: Long) {
        viewModelScope.launch {
            repository.setBudget(CategoryBudget(category, limit, "2026-10"))
            _userMessage.value = "Batas anggaran untuk $category diperbarui"
        }
    }

    fun deleteBudget(category: String) {
        viewModelScope.launch {
            repository.deleteBudget(category)
            _userMessage.value = "Anggaran dihapus"
        }
    }

    fun saveAutoRule(rule: AutoRule) {
        viewModelScope.launch {
            repository.saveAutoRule(rule)
            _userMessage.value = "Aturan otomatis berhasil disimpan"
        }
    }

    fun deleteAutoRule(id: String) {
        viewModelScope.launch {
            repository.deleteAutoRule(id)
            _userMessage.value = "Aturan otomatis dihapus"
        }
    }

    fun executeRuleNow(rule: AutoRule) {
        viewModelScope.launch {
            val tx = repository.executeRuleNow(rule)
            _userMessage.value = "Aturan ${rule.title} dieksekusi! Transaksi dicatat otomatis."
        }
    }

    fun saveSavingGoal(goal: SavingGoal) {
        viewModelScope.launch {
            repository.saveSavingGoal(goal)
            _userMessage.value = "Target Celengan disimpan"
        }
    }

    fun depositToSavingGoal(goalId: String, walletId: String, amount: Long) {
        viewModelScope.launch {
            // Find goal and update amount
            val currentGoal = uiState.value.savingGoals.find { it.id == goalId }
            if (currentGoal != null) {
                val updated = currentGoal.copy(currentAmount = currentGoal.currentAmount + amount)
                repository.saveSavingGoal(updated)
                repository.depositToSavingGoal(goalId, walletId, amount)
                _userMessage.value = "Berhasil menabung Rp $amount ke ${currentGoal.title}!"
            }
        }
    }

    fun addCustomWallet(name: String, type: com.example.data.model.WalletType, balance: Long, accNumber: String, colorHex: Long) {
        viewModelScope.launch {
            val wallet = WalletAccount(
                id = "wallet_" + UUID.randomUUID().toString().take(8),
                name = name,
                type = type,
                balance = balance,
                accountNumber = accNumber,
                colorHex = colorHex,
                iconName = if (type == com.example.data.model.WalletType.BANK) "account_balance" else "account_balance_wallet"
            )
            repository.addWallet(wallet)
            _userMessage.value = "Dompet $name berhasil ditambahkan"
        }
    }
}
