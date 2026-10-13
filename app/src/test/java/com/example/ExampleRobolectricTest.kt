package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TransactionType
import com.example.data.parser.AutoTransactionParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DanaKu", appName)
    }

    @Test
    fun `test auto transaction parser with DANA notification`() {
        val sample = "Pembayaran QRIS Rp 35.000 ke Kopi Kenangan berhasil. Saldo DANA kamu sekarang Rp 465.000."
        val result = AutoTransactionParser.parseMessage(sample)

        assertEquals(35000L, result.amount)
        assertEquals(TransactionType.EXPENSE, result.type)
        assertEquals("DANA", result.detectedWalletName)
        assertEquals("Makanan & Minuman", result.category)
        assertTrue(result.title.contains("Kopi Kenangan", ignoreCase = true))
    }

    @Test
    fun `test auto transaction parser with BCA salary SMS`() {
        val sample = "05/10 09:30 TRSF E-BANKING CR 8.500.000,00 DARI PT MAJU DIGITAL GAJI"
        val result = AutoTransactionParser.parseMessage(sample)

        assertEquals(8500000L, result.amount)
        assertEquals(TransactionType.INCOME, result.type)
        assertEquals("Bank BCA", result.detectedWalletName)
        assertEquals("Gaji & Pemasukan", result.category)
    }
}
