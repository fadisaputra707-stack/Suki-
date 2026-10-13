package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AutoRule
import com.example.data.model.FinanceConstants
import com.example.data.model.SavingGoal
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.data.model.WalletAccount
import com.example.data.model.WalletType
import com.example.ui.components.getCategoryColor
import com.example.ui.components.getCategoryIcon
import com.example.ui.components.toFormattedDate
import com.example.ui.components.toRupiah
import com.example.ui.theme.DanaGreenDark
import com.example.ui.theme.DanaGreenPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurface
import com.example.ui.theme.TransferBlue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialog(
    wallets: List<WalletAccount>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Long, type: TransactionType, walletId: String, category: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: "") }
    var selectedCategory by remember { mutableStateOf(FinanceConstants.DEFAULT_CATEGORIES.first().name) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Catat Transaksi Manual", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        TransactionType.EXPENSE to "Pengeluaran",
                        TransactionType.INCOME to "Pemasukan"
                    ).forEach { (t, label) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (type == t) {
                                if (t == TransactionType.EXPENSE) ExpenseRed.copy(alpha = 0.2f) else IncomeGreen.copy(alpha = 0.2f)
                            } else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (type == t) {
                                    if (t == TransactionType.EXPENSE) ExpenseRed else IncomeGreen
                                } else Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { type = t }
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (type == t) FontWeight.Bold else FontWeight.Normal,
                                color = if (type == t) {
                                    if (t == TransactionType.EXPENSE) ExpenseRed else IncomeGreen
                                } else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Transaksi / Merchant") },
                    placeholder = { Text("Misal: Kopi Kenangan, Gaji") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_tx_title"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal (Rp)") },
                    placeholder = { Text("50000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_tx_amount"),
                    singleLine = true
                )

                Text("Pilih Dompet / Akun:", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    wallets.forEach { w ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedWalletId == w.id) DanaGreenPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedWalletId = w.id }
                        ) {
                            Text(
                                text = w.name,
                                fontSize = 12.sp,
                                fontWeight = if (selectedWalletId == w.id) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedWalletId == w.id) DanaGreenDark else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Text("Pilih Kategori:", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val filteredCats = FinanceConstants.DEFAULT_CATEGORIES.filter {
                        if (type == TransactionType.INCOME) !it.isExpense else it.isExpense
                    }
                    filteredCats.forEach { cat ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCategory == cat.name) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedCategory = cat.name }
                        ) {
                            Text(
                                text = cat.name,
                                fontSize = 11.sp,
                                fontWeight = if (selectedCategory == cat.name) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Tambahan (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toLongOrNull() ?: 0L
                    if (amt > 0 && selectedWalletId.isNotEmpty()) {
                        onConfirm(title, amt, type, selectedWalletId, selectedCategory, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DanaGreenPrimary,
                    contentColor = Color.Black
                ),
                modifier = Modifier.testTag("dialog_tx_save_btn")
            ) {
                Text("Simpan Transaksi", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun TransferDialog(
    wallets: List<WalletAccount>,
    onDismiss: () -> Unit,
    onConfirm: (fromId: String, toId: String, amount: Long, note: String) -> Unit
) {
    var fromId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: "") }
    var toId by remember { mutableStateOf(wallets.getOrNull(1)?.id ?: wallets.firstOrNull()?.id ?: "") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transfer Antar Dompet", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Dari Dompet:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    wallets.take(3).forEach { w ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (fromId == w.id) DanaGreenPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { fromId = w.id }
                        ) {
                            Text(
                                text = w.name,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Text("Ke Dompet Tujuan:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    wallets.take(4).forEach { w ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (toId == w.id) TransferBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { toId = w.id }
                        ) {
                            Text(
                                text = w.name,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal Transfer (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toLongOrNull() ?: 0L
                    if (amt > 0 && fromId.isNotEmpty() && toId.isNotEmpty() && fromId != toId) {
                        onConfirm(fromId, toId, amt, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = TransferBlue,
                    contentColor = Color.White
                )
            ) {
                Text("Kirim Sekarang", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun TopUpDialog(
    wallets: List<WalletAccount>,
    initialWalletId: String?,
    onDismiss: () -> Unit,
    onConfirm: (walletId: String, amount: Long) -> Unit
) {
    var selectedWalletId by remember { mutableStateOf(initialWalletId ?: wallets.firstOrNull()?.id ?: "") }
    var amountText by remember { mutableStateOf("100000") }

    val presets = listOf(50000L, 100000L, 200000L, 500000L, 1000000L)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Top Up Saldo Dompet", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Pilih Dompet:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    wallets.forEach { w ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedWalletId == w.id) DanaGreenPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedWalletId = w.id }
                        ) {
                            Text(
                                text = w.name,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Text("Nominal Cepat:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    presets.take(3).forEach { p ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (amountText == p.toString()) DanaGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { amountText = p.toString() }
                        ) {
                            Text(
                                text = p.toRupiah(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (amountText == p.toString()) Color.Black else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal Lain (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toLongOrNull() ?: 0L
                    if (amt > 0 && selectedWalletId.isNotEmpty()) {
                        onConfirm(selectedWalletId, amt)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DanaGreenPrimary,
                    contentColor = Color.Black
                )
            ) {
                Text("Top Up Sekarang", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun ScanQrPaymentDialog(
    wallets: List<WalletAccount>,
    onDismiss: () -> Unit,
    onPaymentConfirmed: (merchant: String, amount: Long, walletId: String) -> Unit
) {
    var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: "") }
    var selectedPresetMerchant by remember { mutableStateOf("Kopi Kenangan (QRIS)") }
    var amount by remember { mutableStateOf(35000L) }

    val presets = listOf(
        "Kopi Kenangan" to 35000L,
        "Indomaret Point" to 42500L,
        "Mie Gacoan" to 28000L,
        "SPBU Pertamina" to 50000L
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = DanaGreenDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bayar via QRIS", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = NavySurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = DanaGreenPrimary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Simulasi Pemindai QRIS Standar BI",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Deteksi QR Merchant Otomatis",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Text("Pilih Merchant QR:", style = MaterialTheme.typography.labelMedium)
                presets.forEach { (merchant, prAmount) ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedPresetMerchant == merchant) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPresetMerchant = merchant
                                amount = prAmount
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(merchant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(prAmount.toRupiah(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onPaymentConfirmed(selectedPresetMerchant, amount, selectedWalletId)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DanaGreenPrimary,
                    contentColor = Color.Black
                )
            ) {
                Text("Bayar ${amount.toRupiah()}", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun SetBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, limit: Long) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(FinanceConstants.DEFAULT_CATEGORIES.first().name) }
    var limitText by remember { mutableStateOf("1500000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atur Batas Anggaran", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Pilih Kategori:", style = MaterialTheme.typography.labelMedium)
                FinanceConstants.DEFAULT_CATEGORIES.filter { it.isExpense }.take(6).forEach { cat ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedCategory == cat.name) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCategory = cat.name }
                    ) {
                        Text(
                            text = cat.name,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Batas Maksimal Bulanan (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toLongOrNull() ?: 0L
                    if (limit > 0) {
                        onConfirm(selectedCategory, limit)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DanaGreenPrimary,
                    contentColor = Color.Black
                )
            ) {
                Text("Simpan Anggaran", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun AddSavingGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, targetAmount: Long, date: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("Desember 2026") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Target Celengan Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Target") },
                    placeholder = { Text("Misal: Dana Darurat, Beli Motor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal Target (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Perkiraan Tercapai") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toLongOrNull() ?: 0L
                    if (title.isNotBlank() && amt > 0) {
                        onConfirm(title, amt, dateText)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DanaGreenPrimary,
                    contentColor = Color.Black
                )
            ) {
                Text("Buat Target", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun DepositSavingGoalDialog(
    goal: SavingGoal,
    wallets: List<WalletAccount>,
    onDismiss: () -> Unit,
    onConfirm: (walletId: String, amount: Long) -> Unit
) {
    var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: "") }
    var amountText by remember { mutableStateOf("250000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nabung ke ${goal.title}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Potong dari Dompet:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    wallets.take(3).forEach { w ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedWalletId == w.id) DanaGreenPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedWalletId = w.id }
                        ) {
                            Text(
                                text = w.name,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah Tabungan (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toLongOrNull() ?: 0L
                    if (amt > 0 && selectedWalletId.isNotEmpty()) {
                        onConfirm(selectedWalletId, amt)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DanaGreenPrimary,
                    contentColor = Color.Black
                )
            ) {
                Text("Tabung Sekarang", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun AddAutoRuleDialog(
    wallets: List<WalletAccount>,
    onDismiss: () -> Unit,
    onConfirm: (AutoRule) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var patternKeyword by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var category by remember { mutableStateOf("Tagihan & Pulsa") }
    var walletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: "") }
    var recurrenceType by remember { mutableStateOf("BULANAN") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Aturan Otomatis", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Aturan") },
                    placeholder = { Text("Misal: Wifi Indihome Bulanan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = patternKeyword,
                    onValueChange = { patternKeyword = it },
                    label = { Text("Kata Kunci Deteksi") },
                    placeholder = { Text("Misal: INDIHOME atau GAJI") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("BULANAN", "MINGGUAN", "DETEKSI_NOTIFIKASI").forEach { rec ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (recurrenceType == rec) DanaGreenPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { recurrenceType = rec }
                        ) {
                            Text(
                                text = rec,
                                fontSize = 10.sp,
                                fontWeight = if (recurrenceType == rec) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toLongOrNull() ?: 0L
                    if (title.isNotBlank()) {
                        val rule = AutoRule(
                            id = "rule_" + System.currentTimeMillis(),
                            title = title,
                            patternKeyword = patternKeyword.ifBlank { title },
                            type = type,
                            amount = amt,
                            category = category,
                            targetWalletId = walletId,
                            isActive = true,
                            recurrenceType = recurrenceType
                        )
                        onConfirm(rule)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DanaGreenPrimary,
                    contentColor = Color.Black
                )
            ) {
                Text("Simpan Aturan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun AddWalletDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: WalletType, balance: Long, number: String, colorHex: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(WalletType.EWALLET) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hubungkan Dompet Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Dompet / Bank") },
                    placeholder = { Text("Misal: Bank Jago, SeaBank, LinkAja") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Nomor Akun / No HP") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Saldo Awal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(WalletType.EWALLET to "E-Wallet", WalletType.BANK to "Bank", WalletType.CASH to "Tunai").forEach { (wt, label) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedType == wt) DanaGreenPrimary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedType = wt }
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = balanceText.toLongOrNull() ?: 0L
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedType, bal, number, 0xFF118EEA)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DanaGreenPrimary,
                    contentColor = Color.Black
                )
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun TransactionDetailDialog(
    transaction: TransactionItem,
    walletName: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Detail Transaksi", fontWeight = FontWeight.Bold)
                if (transaction.isAutoDetected) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DanaGreenPrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "OTOMATIS",
                            color = DanaGreenDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = transaction.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = transaction.amount.toRupiah(withSign = transaction.type),
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                        )
                        Text(
                            text = "Dompet: $walletName",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Kategori: ${transaction.category}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Waktu: ${transaction.timestamp.toFormattedDate()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (transaction.detectedSource.isNotBlank()) {
                            Text(
                                text = "Sumber: ${transaction.detectedSource}",
                                style = MaterialTheme.typography.bodySmall,
                                color = DanaGreenDark
                            )
                        }
                        if (transaction.notes.isNotBlank()) {
                            Text(
                                text = "Catatan: ${transaction.notes}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hapus Transaksi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}
