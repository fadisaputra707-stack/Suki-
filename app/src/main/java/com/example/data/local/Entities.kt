package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AutoRule
import com.example.data.model.CategoryBudget
import com.example.data.model.SavingGoal
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.data.model.WalletAccount
import com.example.data.model.WalletType

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // EWALLET, BANK, CASH
    val balance: Long,
    val accountNumber: String,
    val colorHex: Long,
    val iconName: String,
    val isDefault: Boolean
) {
    fun toDomain() = WalletAccount(
        id = id,
        name = name,
        type = try { WalletType.valueOf(type) } catch (e: Exception) { WalletType.EWALLET },
        balance = balance,
        accountNumber = accountNumber,
        colorHex = colorHex,
        iconName = iconName,
        isDefault = isDefault
    )

    companion object {
        fun fromDomain(account: WalletAccount) = WalletEntity(
            id = account.id,
            name = account.name,
            type = account.type.name,
            balance = account.balance,
            accountNumber = account.accountNumber,
            colorHex = account.colorHex,
            iconName = account.iconName,
            isDefault = account.isDefault
        )
    }
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amount: Long,
    val type: String, // EXPENSE, INCOME, TRANSFER
    val sourceWalletId: String,
    val targetWalletId: String?,
    val category: String,
    val timestamp: Long,
    val notes: String,
    val isAutoDetected: Boolean,
    val detectedSource: String
) {
    fun toDomain() = TransactionItem(
        id = id,
        title = title,
        amount = amount,
        type = try { TransactionType.valueOf(type) } catch (e: Exception) { TransactionType.EXPENSE },
        sourceWalletId = sourceWalletId,
        targetWalletId = targetWalletId,
        category = category,
        timestamp = timestamp,
        notes = notes,
        isAutoDetected = isAutoDetected,
        detectedSource = detectedSource
    )

    companion object {
        fun fromDomain(tx: TransactionItem) = TransactionEntity(
            id = tx.id,
            title = tx.title,
            amount = tx.amount,
            type = tx.type.name,
            sourceWalletId = tx.sourceWalletId,
            targetWalletId = tx.targetWalletId,
            category = tx.category,
            timestamp = tx.timestamp,
            notes = tx.notes,
            isAutoDetected = tx.isAutoDetected,
            detectedSource = tx.detectedSource
        )
    }
}

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val category: String,
    val monthlyLimit: Long,
    val monthYear: String
) {
    fun toDomain() = CategoryBudget(
        category = category,
        monthlyLimit = monthlyLimit,
        monthYear = monthYear
    )

    companion object {
        fun fromDomain(budget: CategoryBudget) = BudgetEntity(
            category = budget.category,
            monthlyLimit = budget.monthlyLimit,
            monthYear = budget.monthYear
        )
    }
}

@Entity(tableName = "auto_rules")
data class AutoRuleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val patternKeyword: String,
    val type: String,
    val amount: Long,
    val category: String,
    val targetWalletId: String,
    val isActive: Boolean,
    val recurrenceType: String
) {
    fun toDomain() = AutoRule(
        id = id,
        title = title,
        patternKeyword = patternKeyword,
        type = try { TransactionType.valueOf(type) } catch (e: Exception) { TransactionType.EXPENSE },
        amount = amount,
        category = category,
        targetWalletId = targetWalletId,
        isActive = isActive,
        recurrenceType = recurrenceType
    )

    companion object {
        fun fromDomain(rule: AutoRule) = AutoRuleEntity(
            id = rule.id,
            title = rule.title,
            patternKeyword = rule.patternKeyword,
            type = rule.type.name,
            amount = rule.amount,
            category = rule.category,
            targetWalletId = rule.targetWalletId,
            isActive = rule.isActive,
            recurrenceType = rule.recurrenceType
        )
    }
}

@Entity(tableName = "saving_goals")
data class SavingGoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val targetAmount: Long,
    val currentAmount: Long,
    val iconName: String,
    val colorHex: Long,
    val targetDate: String
) {
    fun toDomain() = SavingGoal(
        id = id,
        title = title,
        targetAmount = targetAmount,
        currentAmount = currentAmount,
        iconName = iconName,
        colorHex = colorHex,
        targetDate = targetDate
    )

    companion object {
        fun fromDomain(goal: SavingGoal) = SavingGoalEntity(
            id = goal.id,
            title = goal.title,
            targetAmount = goal.targetAmount,
            currentAmount = goal.currentAmount,
            iconName = goal.iconName,
            colorHex = goal.colorHex,
            targetDate = goal.targetDate
        )
    }
}
