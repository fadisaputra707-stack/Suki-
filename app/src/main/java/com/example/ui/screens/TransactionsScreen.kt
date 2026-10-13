package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionType
import com.example.ui.components.TransactionRowItem
import com.example.ui.components.toRupiah
import com.example.ui.theme.DanaGreenDark
import com.example.ui.theme.DanaGreenPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.NavyCard
import com.example.ui.theme.TransferBlue
import com.example.ui.viewmodel.FinanceUiState

@Composable
fun TransactionsScreen(
    uiState: FinanceUiState,
    onSearchChange: (String) -> Unit,
    onTypeFilterChange: (TransactionType?) -> Unit,
    onWalletFilterChange: (String?) -> Unit,
    onToggleAutoOnly: () -> Unit,
    onOpenAddTransactionDialog: () -> Unit,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredList = uiState.filteredTransactions
    val totalFilteredExpense = filteredList
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }
    val totalFilteredIncome = filteredList
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("transactions_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Riwayat Transaksi",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Kelola, pantau, dan cari seluruh arus kas masuk & keluar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("tx_search_input"),
                placeholder = { Text("Cari merchant, toko, atau catatan...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Carousel
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Semua
                item {
                    FilterChipItem(
                        label = "Semua",
                        isSelected = uiState.selectedTypeFilter == null && !uiState.filterOnlyAutoDetected,
                        onClick = {
                            onTypeFilterChange(null)
                            if (uiState.filterOnlyAutoDetected) onToggleAutoOnly()
                        }
                    )
                }

                // Auto Detected Only
                item {
                    FilterChipItem(
                        label = "⚡ Otomatis (${uiState.autoDetectedTransactionsCount})",
                        isSelected = uiState.filterOnlyAutoDetected,
                        onClick = onToggleAutoOnly,
                        activeColor = DanaGreenPrimary
                    )
                }

                // Pengeluaran
                item {
                    FilterChipItem(
                        label = "Pengeluaran",
                        isSelected = uiState.selectedTypeFilter == TransactionType.EXPENSE,
                        onClick = { onTypeFilterChange(TransactionType.EXPENSE) },
                        activeColor = ExpenseRed
                    )
                }

                // Pemasukan
                item {
                    FilterChipItem(
                        label = "Pemasukan",
                        isSelected = uiState.selectedTypeFilter == TransactionType.INCOME,
                        onClick = { onTypeFilterChange(TransactionType.INCOME) },
                        activeColor = IncomeGreen
                    )
                }

                // Transfer
                item {
                    FilterChipItem(
                        label = "Transfer",
                        isSelected = uiState.selectedTypeFilter == TransactionType.TRANSFER,
                        onClick = { onTypeFilterChange(TransactionType.TRANSFER) },
                        activeColor = TransferBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Wallet Filter Chips Carousel
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChipItem(
                        label = "Semua Dompet",
                        isSelected = uiState.selectedWalletFilterId == null,
                        onClick = { onWalletFilterChange(null) }
                    )
                }
                items(uiState.wallets, key = { it.id }) { wallet ->
                    FilterChipItem(
                        label = wallet.name,
                        isSelected = uiState.selectedWalletFilterId == wallet.id,
                        onClick = { onWalletFilterChange(wallet.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Summary Badge
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredList.size} Transaksi ditemukan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (totalFilteredExpense > 0) {
                            Text(
                                text = "- ${totalFilteredExpense.toRupiah()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }
                        if (totalFilteredIncome > 0) {
                            Text(
                                text = "+ ${totalFilteredIncome.toRupiah()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transaction List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada transaksi yang cocok dengan filter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList, key = { it.id }) { tx ->
                        val walletName = uiState.wallets.find { it.id == tx.sourceWalletId }?.name ?: "Dompet"
                        TransactionRowItem(
                            transaction = tx,
                            walletName = walletName,
                            onClick = { onTransactionClick(tx.id) }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onOpenAddTransactionDialog,
            containerColor = DanaGreenPrimary,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 96.dp)
                .testTag("fab_add_tx")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Transaksi Manual")
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    activeColor: Color = DanaGreenDark
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) activeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
