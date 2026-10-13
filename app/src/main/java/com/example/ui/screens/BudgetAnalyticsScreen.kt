package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.components.FintechProgressBar
import com.example.ui.components.getCategoryColor
import com.example.ui.components.getCategoryIcon
import com.example.ui.components.toRupiah
import com.example.ui.theme.DanaGreenDark
import com.example.ui.theme.DanaGreenPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavySurface
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.FinanceUiState

@Composable
fun BudgetAnalyticsScreen(
    uiState: FinanceUiState,
    onOpenAddBudgetDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalExpense = uiState.monthlyExpense
    val totalIncome = uiState.monthlyIncome
    val netSavings = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) ((netSavings.toFloat() / totalIncome) * 100).toInt() else 0

    // Compute spending per category
    val expensesByCategory = uiState.transactions
        .filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("budget_analytics_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Analisis & Anggaran",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Evaluasi kesehatan finansial & kontrol batas pengeluaran bulanan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Financial Health Score (50-30-20 Rule Card)
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = NavySurface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = DanaGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kesehatan Finansial (Aturan 50/30/20)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (savingsRate >= 20) IncomeGreen.copy(alpha = 0.25f) else WarningAmber.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = if (savingsRate >= 20) "Sangat Sehat" else "Perlu Pantau",
                                color = if (savingsRate >= 20) IncomeGreen else WarningAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Rasio Tabungan Anda: ${savingsRate.coerceAtLeast(0)}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = if (savingsRate >= 20)
                            "Hebat! Anda berhasil menyisihkan lebih dari target ideal 20% penghasilan untuk masa depan."
                        else
                            "Tip: Cobalah kurangi belanja impulsif untuk meningkatkan alokasi tabungan minimal 20%.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 50-30-20 Visual Breakdown
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RuleBarItem(
                            label = "Kebutuhan Pokok (Makanan, Tagihan, Bensin)",
                            spent = expensesByCategory.filter {
                                it.first.contains("Makan") || it.first.contains("Tagihan") || it.first.contains("Transpor")
                            }.sumOf { it.second },
                            idealPercentage = "Ideal 50%",
                            barColor = DanaGreenPrimary
                        )

                        RuleBarItem(
                            label = "Keinginan & Gaya Hidup (Belanja, Hiburan)",
                            spent = expensesByCategory.filter {
                                it.first.contains("Belanja") || it.first.contains("Hiburan")
                            }.sumOf { it.second },
                            idealPercentage = "Ideal 30%",
                            barColor = WarningAmber
                        )

                        RuleBarItem(
                            label = "Tabungan & Investasi Disisihkan",
                            spent = netSavings.coerceAtLeast(0),
                            idealPercentage = "Ideal 20%",
                            barColor = IncomeGreen
                        )
                    }
                }
            }
        }

        // Category Budgets Header & Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Batas Anggaran Kategori",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = onOpenAddBudgetDialog,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DanaGreenPrimary,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_set_budget")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Atur Limit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Budgets List with live spending computation
        items(uiState.budgets, key = { it.category }) { budget ->
            val spent = expensesByCategory.find { it.first == budget.category }?.second ?: 0L
            val progress = if (budget.monthlyLimit > 0) (spent.toFloat() / budget.monthlyLimit) else 0f
            val isOverBudget = spent > budget.monthlyLimit
            val isNearLimit = progress >= 0.8f && !isOverBudget
            val sisa = (budget.monthlyLimit - spent).coerceAtLeast(0)

            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("budget_card_${budget.category}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val catColor = getCategoryColor(budget.category)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(catColor.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(budget.category),
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = budget.category,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Batas: ${budget.monthlyLimit.toRupiah()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isOverBudget) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ExpenseRed.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Overbudget!",
                                        color = ExpenseRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else if (isNearLimit) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = WarningAmber.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Hampir Habis",
                                    color = WarningAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FintechProgressBar(
                        progress = progress,
                        color = when {
                            isOverBudget -> ExpenseRed
                            isNearLimit -> WarningAmber
                            else -> DanaGreenPrimary
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Terpakai: ${spent.toRupiah()} (${(progress * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isOverBudget) "Lebih ${(spent - budget.monthlyLimit).toRupiah()}" else "Sisa: ${sisa.toRupiah()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverBudget) ExpenseRed else IncomeGreen
                        )
                    }
                }
            }
        }

        // Spending Distribution by Category
        item {
            Text(
                text = "Peringkat Pengeluaran",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(expensesByCategory) { (category, amount) ->
            val percentage = if (totalExpense > 0) ((amount.toFloat() / totalExpense) * 100).toInt() else 0
            val catColor = getCategoryColor(category)

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(catColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(category),
                            contentDescription = null,
                            tint = catColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = amount.toRupiah(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        FintechProgressBar(
                            progress = percentage / 100f,
                            color = catColor
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleBarItem(
    label: String,
    spent: Long,
    idealPercentage: String,
    barColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFFCBD5E1)
            )
            Text(
                text = "${spent.toRupiah()} ($idealPercentage)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        FintechProgressBar(
            progress = 0.65f,
            color = barColor
        )
    }
}
