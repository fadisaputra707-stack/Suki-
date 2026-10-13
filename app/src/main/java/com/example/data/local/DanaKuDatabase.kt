package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WalletEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        AutoRuleEntity::class,
        SavingGoalEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DanaKuDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun autoRuleDao(): AutoRuleDao
    abstract fun savingGoalDao(): SavingGoalDao

    companion object {
        @Volatile
        private var INSTANCE: DanaKuDatabase? = null

        fun getInstance(context: Context): DanaKuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DanaKuDatabase::class.java,
                    "danaku_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                seedInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialData(database: DanaKuDatabase) {
            val now = System.currentTimeMillis()
            val dayMs = 86_400_000L

            // Wallets
            val initialWallets = listOf(
                WalletEntity(
                    id = "wallet_dana",
                    name = "DANA",
                    type = "EWALLET",
                    balance = 1450000,
                    accountNumber = "0812-9876-5432",
                    colorHex = 0xFF118EEA,
                    iconName = "account_balance_wallet",
                    isDefault = true
                ),
                WalletEntity(
                    id = "wallet_gopay",
                    name = "GoPay",
                    type = "EWALLET",
                    balance = 385000,
                    accountNumber = "0812-9876-5432",
                    colorHex = 0xFF00AED6,
                    iconName = "account_balance_wallet",
                    isDefault = false
                ),
                WalletEntity(
                    id = "wallet_bca",
                    name = "Bank BCA",
                    type = "BANK",
                    balance = 12850000,
                    accountNumber = "8045-1234-99",
                    colorHex = 0xFF003688,
                    iconName = "account_balance",
                    isDefault = false
                ),
                WalletEntity(
                    id = "wallet_shopeepay",
                    name = "ShopeePay",
                    type = "EWALLET",
                    balance = 220000,
                    accountNumber = "0812-9876-5432",
                    colorHex = 0xFFEE4D2D,
                    iconName = "shopping_bag",
                    isDefault = false
                ),
                WalletEntity(
                    id = "wallet_ovo",
                    name = "OVO Cash",
                    type = "EWALLET",
                    balance = 95000,
                    accountNumber = "0812-9876-5432",
                    colorHex = 0xFF4C3494,
                    iconName = "account_balance_wallet",
                    isDefault = false
                ),
                WalletEntity(
                    id = "wallet_cash",
                    name = "Uang Tunai",
                    type = "CASH",
                    balance = 450000,
                    accountNumber = "Dompet Fisik",
                    colorHex = 0xFF2E7D32,
                    iconName = "payments",
                    isDefault = false
                )
            )
            database.walletDao().insertWallets(initialWallets)

            // Transactions
            val initialTransactions = listOf(
                TransactionEntity(
                    id = "tx_1",
                    title = "Kopi Kenangan (QRIS)",
                    amount = 38000,
                    type = "EXPENSE",
                    sourceWalletId = "wallet_dana",
                    targetWalletId = null,
                    category = "Makanan & Minuman",
                    timestamp = now - (2 * 3600_000L),
                    notes = "Ice Avocado Coffee Large",
                    isAutoDetected = true,
                    detectedSource = "DANA QRIS Notif"
                ),
                TransactionEntity(
                    id = "tx_2",
                    title = "Gaji Bulanan PT Digital Nusantara",
                    amount = 8500000,
                    type = "INCOME",
                    sourceWalletId = "wallet_bca",
                    targetWalletId = null,
                    category = "Gaji & Pemasukan",
                    timestamp = now - (dayMs),
                    notes = "Payroll Gaji Pokok & Tunjangan",
                    isAutoDetected = true,
                    detectedSource = "BCA m-Transfer SMS"
                ),
                TransactionEntity(
                    id = "tx_3",
                    title = "Alfamart Belanja Bulanan",
                    amount = 145000,
                    type = "EXPENSE",
                    sourceWalletId = "wallet_gopay",
                    targetWalletId = null,
                    category = "Belanja & Minimarket",
                    timestamp = now - (2 * dayMs),
                    notes = "Minyak goreng, beras, snack",
                    isAutoDetected = true,
                    detectedSource = "GoPay Push Notif"
                ),
                TransactionEntity(
                    id = "tx_4",
                    title = "SPBU Pertamina Bensin Shell/Pertalite",
                    amount = 50000,
                    type = "EXPENSE",
                    sourceWalletId = "wallet_dana",
                    targetWalletId = null,
                    category = "Transportasi & Bensin",
                    timestamp = now - (3 * dayMs),
                    notes = "Isi bensin motor",
                    isAutoDetected = false,
                    detectedSource = "Manual"
                ),
                TransactionEntity(
                    id = "tx_5",
                    title = "Langganan Netflix Bulanan",
                    amount = 54000,
                    type = "EXPENSE",
                    sourceWalletId = "wallet_bca",
                    targetWalletId = null,
                    category = "Hiburan & Langganan",
                    timestamp = now - (4 * dayMs),
                    notes = "Auto-debit paket standar",
                    isAutoDetected = true,
                    detectedSource = "Aturan Otomatis Bulanan"
                ),
                TransactionEntity(
                    id = "tx_6",
                    title = "Token Listrik PLN 200rb",
                    amount = 202500,
                    type = "EXPENSE",
                    sourceWalletId = "wallet_dana",
                    targetWalletId = null,
                    category = "Tagihan & Pulsa",
                    timestamp = now - (5 * dayMs),
                    notes = "Listrik rumah no meter 321890",
                    isAutoDetected = true,
                    detectedSource = "DANA Tagihan"
                ),
                TransactionEntity(
                    id = "tx_7",
                    title = "Shopee Supermarket Belanjaan",
                    amount = 89000,
                    type = "EXPENSE",
                    sourceWalletId = "wallet_shopeepay",
                    targetWalletId = null,
                    category = "Belanja & Minimarket",
                    timestamp = now - (6 * dayMs),
                    notes = "Sabun & kebutuhan dapur",
                    isAutoDetected = true,
                    detectedSource = "ShopeePay Auto-detect"
                )
            )
            database.transactionDao().insertTransactions(initialTransactions)

            // Budgets
            val initialBudgets = listOf(
                BudgetEntity("Makanan & Minuman", 2000000, "2026-10"),
                BudgetEntity("Belanja & Minimarket", 1500000, "2026-10"),
                BudgetEntity("Transportasi & Bensin", 600000, "2026-10"),
                BudgetEntity("Tagihan & Pulsa", 800000, "2026-10"),
                BudgetEntity("Hiburan & Langganan", 400000, "2026-10")
            )
            database.budgetDao().insertBudgets(initialBudgets)

            // Auto-Rules
            val initialRules = listOf(
                AutoRuleEntity(
                    id = "rule_gaji",
                    title = "Gaji Masuk Bulanan",
                    patternKeyword = "GAJI",
                    type = "INCOME",
                    amount = 8500000,
                    category = "Gaji & Pemasukan",
                    targetWalletId = "wallet_bca",
                    isActive = true,
                    recurrenceType = "BULANAN"
                ),
                AutoRuleEntity(
                    id = "rule_netflix",
                    title = "Netflix Premium",
                    patternKeyword = "NETFLIX",
                    type = "EXPENSE",
                    amount = 54000,
                    category = "Hiburan & Langganan",
                    targetWalletId = "wallet_bca",
                    isActive = true,
                    recurrenceType = "BULANAN"
                ),
                AutoRuleEntity(
                    id = "rule_pln",
                    title = "Listrik PLN Rumah",
                    patternKeyword = "PLN",
                    type = "EXPENSE",
                    amount = 200000,
                    category = "Tagihan & Pulsa",
                    targetWalletId = "wallet_dana",
                    isActive = true,
                    recurrenceType = "BULANAN"
                ),
                AutoRuleEntity(
                    id = "rule_qris_dana",
                    title = "Deteksi QRIS DANA",
                    patternKeyword = "QRIS",
                    type = "EXPENSE",
                    amount = 0,
                    category = "Makanan & Minuman",
                    targetWalletId = "wallet_dana",
                    isActive = true,
                    recurrenceType = "DETEKSI_NOTIFIKASI"
                )
            )
            database.autoRuleDao().insertRules(initialRules)

            // Saving Goals
            val initialGoals = listOf(
                SavingGoalEntity(
                    id = "goal_darurat",
                    title = "Dana Darurat (6 Bulan)",
                    targetAmount = 30000000,
                    currentAmount = 18500000,
                    iconName = "shield",
                    colorHex = 0xFF10B981,
                    targetDate = "Desember 2026"
                ),
                SavingGoalEntity(
                    id = "goal_liburan",
                    title = "Liburan ke Labuan Bajo",
                    targetAmount = 7500000,
                    currentAmount = 4200000,
                    iconName = "flight",
                    colorHex = 0xFF3B82F6,
                    targetDate = "November 2026"
                ),
                SavingGoalEntity(
                    id = "goal_gadget",
                    title = "Upgrade Laptop Kerja",
                    targetAmount = 15000000,
                    currentAmount = 6000000,
                    iconName = "laptop",
                    colorHex = 0xFF8B5CF6,
                    targetDate = "Maret 2027"
                )
            )
            database.savingGoalDao().insertGoals(initialGoals)
        }
    }
}
