package com.example.data.model

enum class WalletType {
    EWALLET,
    BANK,
    CASH
}

data class WalletAccount(
    val id: String,
    val name: String,
    val type: WalletType,
    val balance: Long,
    val accountNumber: String = "",
    val colorHex: Long = 0xFF118EEA,
    val iconName: String = "account_balance_wallet",
    val isDefault: Boolean = false
)

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

data class TransactionItem(
    val id: String,
    val title: String,
    val amount: Long,
    val type: TransactionType,
    val sourceWalletId: String,
    val targetWalletId: String? = null,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isAutoDetected: Boolean = false,
    val detectedSource: String = ""
)

data class CategoryBudget(
    val category: String,
    val monthlyLimit: Long,
    val monthYear: String = ""
)

data class AutoRule(
    val id: String,
    val title: String,
    val patternKeyword: String,
    val type: TransactionType,
    val amount: Long,
    val category: String,
    val targetWalletId: String,
    val isActive: Boolean = true,
    val recurrenceType: String = "BULANAN" // "BULANAN", "MINGGUAN", "DETEKSI_NOTIFIKASI"
)

data class SavingGoal(
    val id: String,
    val title: String,
    val targetAmount: Long,
    val currentAmount: Long,
    val iconName: String = "savings",
    val colorHex: Long = 0xFF00B894,
    val targetDate: String = ""
)

data class ParsedTransactionResult(
    val title: String,
    val amount: Long,
    val type: TransactionType,
    val detectedWalletName: String,
    val category: String,
    val confidence: Float,
    val rawSnippet: String,
    val notes: String = ""
)

data class DefaultCategoryInfo(
    val name: String,
    val iconName: String,
    val colorHex: Long,
    val isExpense: Boolean = true
)

object FinanceConstants {
    val DEFAULT_CATEGORIES = listOf(
        DefaultCategoryInfo("Makanan & Minuman", "restaurant", 0xFFF59E0B, true),
        DefaultCategoryInfo("Belanja & Minimarket", "shopping_cart", 0xFF3B82F6, true),
        DefaultCategoryInfo("Transportasi & Bensin", "directions_car", 0xFF00B894, true),
        DefaultCategoryInfo("Tagihan & Pulsa", "receipt_long", 0xFFEF4444, true),
        DefaultCategoryInfo("Hiburan & Langganan", "movie", 0xFF8B5CF6, true),
        DefaultCategoryInfo("Kesehatan", "favorite", 0xFFEC4899, true),
        DefaultCategoryInfo("Pendidikan", "school", 0xFF14B8A6, true),
        DefaultCategoryInfo("Gaji & Pemasukan", "payments", 0xFF10B981, false),
        DefaultCategoryInfo("Bonus & Cashback", "redeem", 0xFFF59E0B, false),
        DefaultCategoryInfo("Investasi & Dividen", "trending_up", 0xFF6366F1, false),
        DefaultCategoryInfo("Transfer Antar Dompet", "swap_horiz", 0xFF0284C7, true),
        DefaultCategoryInfo("Lainnya", "category", 0xFF64748B, true)
    )
}
