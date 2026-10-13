package com.example.data.parser

import com.example.data.model.ParsedTransactionResult
import com.example.data.model.TransactionType
import java.util.regex.Pattern

data class NotificationSimulationTemplate(
    val id: String,
    val senderName: String,
    val appIcon: String,
    val messageText: String,
    val defaultWalletId: String,
    val description: String
)

object AutoTransactionParser {

    /**
     * Parse raw text (SMS, Push Notification, Kuitansi / Struk) and extract transaction attributes.
     */
    fun parseMessage(rawText: String): ParsedTransactionResult {
        val cleanText = rawText.trim()
        val lowerText = cleanText.lowercase()

        // 1. Detect Type: Income vs Expense
        val isIncome = containsAny(
            lowerText,
            listOf(
                " cr ", "cr:", "kredit", "masuk", "menerima", "diterima", "gaji",
                "payroll", "cashback", "refund", "transfer masuk", "top up sebesar",
                "top up saldo berhasil", "setoran"
            )
        )
        val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

        // 2. Extract Amount
        val extractedAmount = extractAmount(cleanText)

        // 3. Detect Source Wallet / Bank
        val detectedWalletName = detectWallet(cleanText)

        // 4. Detect Category & Merchant Title
        val (category, detectedTitle) = detectCategoryAndTitle(cleanText, type)

        // 5. Calculate Confidence Score
        var score = 0.5f
        if (extractedAmount > 0) score += 0.3f
        if (detectedWalletName.isNotEmpty()) score += 0.1f
        if (category != "Lainnya") score += 0.1f
        val confidence = score.coerceIn(0.1f, 1.0f)

        return ParsedTransactionResult(
            title = detectedTitle.ifEmpty { if (type == TransactionType.INCOME) "Pemasukan Otomatis" else "Pengeluaran Otomatis" },
            amount = extractedAmount,
            type = type,
            detectedWalletName = detectedWalletName.ifEmpty { "DANA" },
            category = category,
            confidence = confidence,
            rawSnippet = if (cleanText.length > 120) cleanText.take(120) + "..." else cleanText,
            notes = "Terdeteksi otomatis via parser cerdas DanaKu"
        )
    }

    private fun extractAmount(text: String): Long {
        // Match patterns like:
        // "Rp 150.000,00", "Rp. 150.000", "Rp150.000", "Rp 35.000", "150.000,00", "sebesar Rp 45.000"
        val patterns = listOf(
            Pattern.compile("""(?:Rp|IDR)\.?\s*([0-9]{1,3}(?:\.[0-9]{3})+(?:,[0-9]{2})?)""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""(?:Rp|IDR)\.?\s*([0-9]+(?:\.[0-9]{3})*)""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""(?:sebesar|sejumlah|total)\s*(?:Rp\.?)?\s*([0-9]{1,3}(?:\.[0-9]{3})*)""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""\b([0-9]{1,3}(?:\.[0-9]{3})+)\b""")
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val match = matcher.group(1) ?: matcher.group(0)
                if (match != null) {
                    val sanitized = match
                        .replace("Rp", "", true)
                        .replace("IDR", "", true)
                        .split(",")[0] // Remove decimals if any (e.g. ,00)
                        .replace(".", "")
                        .replace(" ", "")
                        .trim()
                    val amountLong = sanitized.toLongOrNull()
                    if (amountLong != null && amountLong > 0) {
                        return amountLong
                    }
                }
            }
        }

        // Fallback for simple numbers like "50000" preceded by currency
        val fallback = Pattern.compile("""(?:Rp|IDR)\.?\s*(\d+)""", Pattern.CASE_INSENSITIVE).matcher(text)
        if (fallback.find()) {
            return fallback.group(1)?.toLongOrNull() ?: 0L
        }

        return 0L
    }

    private fun detectWallet(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("dana") -> "DANA"
            lower.contains("gopay") || lower.contains("go-pay") || lower.contains("gofood") || lower.contains("goride") -> "GoPay"
            lower.contains("shopeepay") || lower.contains("shopee") -> "ShopeePay"
            lower.contains("ovo") -> "OVO Cash"
            lower.contains("bca") || lower.contains("m-bca") || lower.contains("trsf e-banking") || lower.contains("klikbca") -> "Bank BCA"
            lower.contains("mandiri") || lower.contains("livin") -> "Bank Mandiri"
            lower.contains("tunai") || lower.contains("cash") -> "Uang Tunai"
            else -> "DANA"
        }
    }

    private fun detectCategoryAndTitle(text: String, type: TransactionType): Pair<String, String> {
        val lower = text.lowercase()

        if (type == TransactionType.INCOME) {
            return when {
                lower.contains("gaji") || lower.contains("payroll") || lower.contains("upah") ->
                    "Gaji & Pemasukan" to extractMerchantName(text, "Gaji Masuk")
                lower.contains("cashback") || lower.contains("bonus") || lower.contains("promo") ->
                    "Bonus & Cashback" to "Cashback & Reward"
                lower.contains("dividen") || lower.contains("reksadana") || lower.contains("saham") ->
                    "Investasi & Dividen" to "Hasil Investasi"
                else ->
                    "Gaji & Pemasukan" to extractMerchantName(text, "Transfer Masuk")
            }
        }

        // Expense Categorization
        return when {
            // Food & Drink
            containsAny(lower, listOf("kopi", "coffee", "kenangan", "fore", "starbucks", "gofood", "grabfood", "mcd", "kfc", "hokben", "resto", "cafe", "warung", "geprek", "chatime", "mie gacoan", "makan", "kuliner")) -> {
                val merchant = extractMerchantName(text, "Kopi / Makanan")
                "Makanan & Minuman" to merchant
            }
            // Groceries & Minimarket
            containsAny(lower, listOf("indomaret", "alfamart", "alfamidi", "hypermart", "superindo", "transmart", "supermarket", "pasar", "sayur", "toko sembako")) -> {
                val merchant = extractMerchantName(text, "Belanja Minimarket")
                "Belanja & Minimarket" to merchant
            }
            // Transport & Fuel
            containsAny(lower, listOf("pertamina", "spbu", "shell", "goride", "gocar", "grabbike", "grabcar", "bensin", "pertalite", "pertamax", "tol", "parkir", "kereta", "krl", "mrt")) -> {
                val merchant = extractMerchantName(text, "Transportasi")
                "Transportasi & Bensin" to merchant
            }
            // Bills & Utilities
            containsAny(lower, listOf("pln", "listrik", "pdam", "air", "token", "telkomsel", "indihome", "bpjs", "xl", "indosat", "tri", "pulsa", "paket data", "wifi")) -> {
                val merchant = extractMerchantName(text, "Tagihan & Pulsa")
                "Tagihan & Pulsa" to merchant
            }
            // Entertainment & Streaming
            containsAny(lower, listOf("netflix", "spotify", "youtube", "disney", "bioskop", "xxi", "cgv", "steam", "playstation", "game", "vidio")) -> {
                val merchant = extractMerchantName(text, "Langganan Hiburan")
                "Hiburan & Langganan" to merchant
            }
            // Health
            containsAny(lower, listOf("apotek", "kimia farma", "k24", "halodoc", "dokter", "rumah sakit", "klinik", "obat")) -> {
                val merchant = extractMerchantName(text, "Kesehatan")
                "Kesehatan" to merchant
            }
            else -> {
                val merchant = extractMerchantName(text, "Pengeluaran QRIS / Merchant")
                "Lainnya" to merchant
            }
        }
    }

    private fun extractMerchantName(text: String, fallback: String): String {
        // Try to find "di [Merchant]" or "ke [Merchant]" or "dari [Merchant]"
        val patterns = listOf(
            Pattern.compile("""(?:di|ke|merchant|pada)\s+([A-Za-z0-9\s&'-]{3,28})(?:\s+(?:berhasil|tgl|sebesar|menggunakan|via|\.))""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""(?:di|ke|dari)\s+([A-Za-z0-9\s&'-]{3,20})""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""(?:QRIS\s+Pembayaran\s+di\s+)([A-Za-z0-9\s&'-]{3,25})""", Pattern.CASE_INSENSITIVE)
        )

        for (pattern in patterns) {
            val m = pattern.matcher(text)
            if (m.find()) {
                val merchant = m.group(1)?.trim()
                if (!merchant.isNullOrEmpty() && merchant.length in 3..30) {
                    return merchant.split("\n")[0].trim()
                }
            }
        }

        // Check common brands in text
        val knownBrands = listOf(
            "Kopi Kenangan", "Fore Coffee", "Starbucks", "Indomaret", "Alfamart",
            "SPBU Pertamina", "Ayam Geprek", "Mie Gacoan", "Chatime", "Shopee Supermarket",
            "GoFood", "GrabFood", "PLN Listrik", "Telkomsel", "Netflix", "Spotify"
        )
        for (brand in knownBrands) {
            if (text.contains(brand, ignoreCase = true)) {
                return brand
            }
        }

        return fallback
    }

    private fun containsAny(text: String, keywords: List<String>): Boolean {
        for (k in keywords) {
            if (text.contains(k, ignoreCase = true)) return true
        }
        return false
    }

    /**
     * Presets of Indonesian Banking / E-Wallet Notification SMS & Push messages for realistic simulation.
     */
    val SIMULATION_TEMPLATES = listOf(
        NotificationSimulationTemplate(
            id = "sim_dana_qris",
            senderName = "DANA",
            appIcon = "account_balance_wallet",
            messageText = "Pembayaran QRIS Rp 35.000 ke Kopi Kenangan berhasil. Saldo DANA kamu sekarang Rp 1.415.000.",
            defaultWalletId = "wallet_dana",
            description = "DANA - Pembayaran QRIS Kopi Kenangan (Rp 35.000)"
        ),
        NotificationSimulationTemplate(
            id = "sim_bca_gaji",
            senderName = "Bank BCA (m-Transfer)",
            appIcon = "account_balance",
            messageText = "05/10 09:30 TRSF E-BANKING CR 8.500.000,00 DARI PT DIGITAL MAJU KARYA GAJI OKTOBER",
            defaultWalletId = "wallet_bca",
            description = "BCA - Transfer Masuk Gaji Kantor (Rp 8.500.000)"
        ),
        NotificationSimulationTemplate(
            id = "sim_gopay_food",
            senderName = "GoPay",
            appIcon = "account_balance_wallet",
            messageText = "Pembayaran GoFood sebesar Rp 45.000 berhasil menggunakan GoPay di Ayam Geprek Bensu.",
            defaultWalletId = "wallet_gopay",
            description = "GoPay - Pembelian GoFood Ayam Geprek (Rp 45.000)"
        ),
        NotificationSimulationTemplate(
            id = "sim_bca_indomaret",
            senderName = "m-BCA",
            appIcon = "account_balance",
            messageText = "08/10 16:15 TRSF E-BANKING DB 10/10 Rp 125.000,00 KE INDOMARET POINT BERHASIL",
            defaultWalletId = "wallet_bca",
            description = "BCA - Belanja Indomaret Point (Rp 125.000)"
        ),
        NotificationSimulationTemplate(
            id = "sim_ovo_pertamina",
            senderName = "OVO",
            appIcon = "account_balance_wallet",
            messageText = "Transaksi berhasil! Kamu telah membayar Rp 50.000 di SPBU Pertamina menggunakan OVO Cash.",
            defaultWalletId = "wallet_ovo",
            description = "OVO - Isi Bensin SPBU Pertamina (Rp 50.000)"
        ),
        NotificationSimulationTemplate(
            id = "sim_dana_pln",
            senderName = "DANA",
            appIcon = "account_balance_wallet",
            messageText = "Pembayaran tagihan Listrik PLN Pascabayar Rp 185.000 berhasil. No Pelanggan 52109876.",
            defaultWalletId = "wallet_dana",
            description = "DANA - Tagihan Listrik PLN (Rp 185.000)"
        ),
        NotificationSimulationTemplate(
            id = "sim_shopee_supermarket",
            senderName = "ShopeePay",
            appIcon = "shopping_bag",
            messageText = "Pembayaran Rp 72.000 ke Shopee Supermarket berhasil dengan ShopeePay.",
            defaultWalletId = "wallet_shopeepay",
            description = "ShopeePay - Belanjaan Supermarket (Rp 72.000)"
        )
    )

    /**
     * Sample receipt texts for OCR / Struk Belanja scanner simulation
     */
    val SAMPLE_RECEIPTS = listOf(
        """
        INDOMARET POINT SUDIRMAN
        JL. JENDERAL SUDIRMAN NO. 45 JAKARTA
        ========================================
        1X ULTRA MILK CHOCO 250ML       Rp  7.500
        1X OREO VANILLA 123G           Rp  9.800
        1X ROTI SARI ROTI COKELAT       Rp  6.000
        1X AIR MINERAL LE MINERALE     Rp  3.500
        ----------------------------------------
        TOTAL BELANJA :                Rp 26.800
        METODE BAYAR : QRIS DANA
        TGL: 12/10/2026 14:10:22
        ========================================
        TERIMA KASIH TELAH BERBELANJA
        """.trimIndent(),
        """
        KOPI KENANGAN - MALL KELAPA GADING
        ORDER #8912 - DINE IN
        ========================================
        1X KOPI KENANGAN MANTAN (L)    Rp 24.000
        1X ROTI COKLAT KLASIK          Rp 12.000
        SUBTOTAL                       Rp 36.000
        PB1 (10%)                      Rp  3.600
        ----------------------------------------
        TOTAL TAGIHAN                  Rp 39.600
        PEMBAYARAN : GOPAY QRIS
        WAKTU : 12-10-2026 15:45
        """.trimIndent(),
        """
        SPBU PERTAMINA 31.129.02
        KUNINGAN, JAKARTA SELATAN
        ========================================
        PRODUK   : PERTAMAX (RON 92)
        VOLUME   : 3.86 LITER
        HARGA/L  : Rp 12.950
        ----------------------------------------
        TOTAL PEMBAYARAN:              Rp 50.000
        BAYAR    : OVO CASH
        STATUS   : LUNAS
        ========================================
        """.trimIndent()
    )
}
