package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SavingGoal
import com.example.data.model.TransactionItem
import com.example.data.parser.AutoTransactionParser
import com.example.ui.components.SimulatedNotificationBanner
import com.example.ui.screens.AddAutoRuleDialog
import com.example.ui.screens.AddSavingGoalDialog
import com.example.ui.screens.AddTransactionDialog
import com.example.ui.screens.AddWalletDialog
import com.example.ui.screens.AutoRecordScreen
import com.example.ui.screens.BudgetAnalyticsScreen
import com.example.ui.screens.DepositSavingGoalDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SavingsAndWalletsScreen
import com.example.ui.screens.ScanQrPaymentDialog
import com.example.ui.screens.SetBudgetDialog
import com.example.ui.screens.TopUpDialog
import com.example.ui.screens.TransactionDetailDialog
import com.example.ui.screens.TransactionsScreen
import com.example.ui.screens.TransferDialog
import com.example.ui.theme.DanaGreenDark
import com.example.ui.theme.DanaGreenPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                DanaKuApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DanaKuApp(viewModel: FinanceViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog state controllers
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showTopUpDialog by remember { mutableStateOf<String?>(null) }
    var showScanQrDialog by remember { mutableStateOf(false) }
    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showDepositGoalDialog by remember { mutableStateOf<SavingGoal?>(null) }
    var showAddRuleDialog by remember { mutableStateOf(false) }
    var showAddWalletDialog by remember { mutableStateOf(false) }
    var selectedDetailTx by remember { mutableStateOf<TransactionItem?>(null) }

    // Handle user message snackbars
    LaunchedEffect(uiState.userMessage) {
        val msg = uiState.userMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Hardware/gesture back press handling: if not on Home tab, return to Home tab
    BackHandler(enabled = uiState.selectedNavTab != 0) {
        viewModel.setNavTab(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = uiState.selectedNavTab == 0,
                    onClick = { viewModel.setNavTab(0) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                    label = { Text("Beranda", fontSize = 11.sp, fontWeight = if (uiState.selectedNavTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = DanaGreenDark,
                        indicatorColor = DanaGreenPrimary
                    ),
                    modifier = Modifier.testTag("nav_item_home")
                )

                NavigationBarItem(
                    selected = uiState.selectedNavTab == 1,
                    onClick = { viewModel.setNavTab(1) },
                    icon = {
                        BadgedBox(badge = {
                            Badge(containerColor = DanaGreenPrimary) {
                                Text("Auto", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Pencatatan Otomatis")
                        }
                    },
                    label = { Text("Otomatis", fontSize = 11.sp, fontWeight = if (uiState.selectedNavTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = DanaGreenDark,
                        indicatorColor = DanaGreenPrimary
                    ),
                    modifier = Modifier.testTag("nav_item_auto")
                )

                NavigationBarItem(
                    selected = uiState.selectedNavTab == 2,
                    onClick = { viewModel.setNavTab(2) },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Transaksi") },
                    label = { Text("Transaksi", fontSize = 11.sp, fontWeight = if (uiState.selectedNavTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = DanaGreenDark,
                        indicatorColor = DanaGreenPrimary
                    ),
                    modifier = Modifier.testTag("nav_item_transactions")
                )

                NavigationBarItem(
                    selected = uiState.selectedNavTab == 3,
                    onClick = { viewModel.setNavTab(3) },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Analisis") },
                    label = { Text("Analisis", fontSize = 11.sp, fontWeight = if (uiState.selectedNavTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = DanaGreenDark,
                        indicatorColor = DanaGreenPrimary
                    ),
                    modifier = Modifier.testTag("nav_item_analytics")
                )

                NavigationBarItem(
                    selected = uiState.selectedNavTab == 4,
                    onClick = { viewModel.setNavTab(4) },
                    icon = { Icon(Icons.Default.Savings, contentDescription = "Celengan") },
                    label = { Text("Celengan", fontSize = 11.sp, fontWeight = if (uiState.selectedNavTab == 4) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = DanaGreenDark,
                        indicatorColor = DanaGreenPrimary
                    ),
                    modifier = Modifier.testTag("nav_item_savings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Screen Body
            AnimatedContent(
                targetState = uiState.selectedNavTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> HomeScreen(
                        uiState = uiState,
                        onNavigateToAutoDetect = { viewModel.setNavTab(1) },
                        onNavigateToTransactions = { viewModel.setNavTab(2) },
                        onOpenTransferDialog = { showTransferDialog = true },
                        onOpenTopUpDialog = { walletId -> showTopUpDialog = walletId ?: uiState.wallets.firstOrNull()?.id },
                        onOpenAddTransactionDialog = { showAddTxDialog = true },
                        onOpenScanQrDialog = { showScanQrDialog = true },
                        onToggleBalanceVisibility = { viewModel.toggleBalanceVisibility() },
                        onTriggerSampleSimulation = {
                            val sample = AutoTransactionParser.SIMULATION_TEMPLATES.first()
                            viewModel.triggerSimulation(sample)
                        },
                        onTransactionClick = { txId ->
                            selectedDetailTx = uiState.transactions.find { it.id == txId }
                        }
                    )
                    1 -> AutoRecordScreen(
                        uiState = uiState,
                        onTriggerSimulation = { template -> viewModel.triggerSimulation(template) },
                        onParseText = { text -> viewModel.parseCustomText(text) },
                        onConfirmParsedResult = { viewModel.confirmAndSaveParsedResult() },
                        onExecuteRule = { rule -> viewModel.executeRuleNow(rule) },
                        onDeleteRule = { ruleId -> viewModel.deleteAutoRule(ruleId) },
                        onOpenAddRuleDialog = { showAddRuleDialog = true }
                    )
                    2 -> TransactionsScreen(
                        uiState = uiState,
                        onSearchChange = { query -> viewModel.setSearchQuery(query) },
                        onTypeFilterChange = { type -> viewModel.setTypeFilter(type) },
                        onWalletFilterChange = { walletId -> viewModel.setWalletFilter(walletId) },
                        onToggleAutoOnly = { viewModel.toggleFilterOnlyAutoDetected() },
                        onOpenAddTransactionDialog = { showAddTxDialog = true },
                        onTransactionClick = { txId ->
                            selectedDetailTx = uiState.transactions.find { it.id == txId }
                        }
                    )
                    3 -> BudgetAnalyticsScreen(
                        uiState = uiState,
                        onOpenAddBudgetDialog = { showSetBudgetDialog = true }
                    )
                    4 -> SavingsAndWalletsScreen(
                        uiState = uiState,
                        onOpenAddGoalDialog = { showAddGoalDialog = true },
                        onOpenDepositGoalDialog = { goal -> showDepositGoalDialog = goal },
                        onOpenAddWalletDialog = { showAddWalletDialog = true },
                        onOpenTopUpDialog = { walletId -> showTopUpDialog = walletId }
                    )
                }
            }

            // Top Floating Notification Simulation Banner
            SimulatedNotificationBanner(
                simulation = uiState.activeSimulation,
                parsedPreview = uiState.parsedPreview,
                onConfirm = { viewModel.confirmAndSaveParsedResult() },
                onDismiss = { viewModel.dismissSimulation() },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    // Dialogs
    if (showAddTxDialog) {
        AddTransactionDialog(
            wallets = uiState.wallets,
            onDismiss = { showAddTxDialog = false },
            onConfirm = { title, amount, type, walletId, category, notes ->
                viewModel.addManualTransaction(title, amount, type, walletId, null, category, notes)
                showAddTxDialog = false
            }
        )
    }

    if (showTransferDialog) {
        TransferDialog(
            wallets = uiState.wallets,
            onDismiss = { showTransferDialog = false },
            onConfirm = { fromId, toId, amount, note ->
                viewModel.transferBetweenWallets(fromId, toId, amount, note)
                showTransferDialog = false
            }
        )
    }

    if (showTopUpDialog != null) {
        TopUpDialog(
            wallets = uiState.wallets,
            initialWalletId = showTopUpDialog,
            onDismiss = { showTopUpDialog = null },
            onConfirm = { walletId, amount ->
                viewModel.topUpWallet(walletId, amount)
                showTopUpDialog = null
            }
        )
    }

    if (showScanQrDialog) {
        ScanQrPaymentDialog(
            wallets = uiState.wallets,
            onDismiss = { showScanQrDialog = false },
            onPaymentConfirmed = { merchant, amount, walletId ->
                val simulatedText = "Pembayaran QRIS Rp $amount ke $merchant berhasil dengan DANA."
                viewModel.parseCustomText(simulatedText)
                viewModel.confirmAndSaveParsedResult(walletId)
                showScanQrDialog = false
            }
        )
    }

    if (showSetBudgetDialog) {
        SetBudgetDialog(
            onDismiss = { showSetBudgetDialog = false },
            onConfirm = { category, limit ->
                viewModel.saveBudget(category, limit)
                showSetBudgetDialog = false
            }
        )
    }

    if (showAddGoalDialog) {
        AddSavingGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, targetAmount, date ->
                val goal = SavingGoal(
                    id = "goal_" + System.currentTimeMillis(),
                    title = title,
                    targetAmount = targetAmount,
                    currentAmount = 0L,
                    iconName = "savings",
                    targetDate = date
                )
                viewModel.saveSavingGoal(goal)
                showAddGoalDialog = false
            }
        )
    }

    val currentDepositGoal = showDepositGoalDialog
    if (currentDepositGoal != null) {
        DepositSavingGoalDialog(
            goal = currentDepositGoal,
            wallets = uiState.wallets,
            onDismiss = { showDepositGoalDialog = null },
            onConfirm = { walletId, amount ->
                viewModel.depositToSavingGoal(currentDepositGoal.id, walletId, amount)
                showDepositGoalDialog = null
            }
        )
    }

    if (showAddRuleDialog) {
        AddAutoRuleDialog(
            wallets = uiState.wallets,
            onDismiss = { showAddRuleDialog = false },
            onConfirm = { rule ->
                viewModel.saveAutoRule(rule)
                showAddRuleDialog = false
            }
        )
    }

    if (showAddWalletDialog) {
        AddWalletDialog(
            onDismiss = { showAddWalletDialog = false },
            onConfirm = { name, type, bal, num, colorHex ->
                viewModel.addCustomWallet(name, type, bal, num, colorHex)
                showAddWalletDialog = false
            }
        )
    }

    val detailTx = selectedDetailTx
    if (detailTx != null) {
        val walletName = uiState.wallets.find { it.id == detailTx.sourceWalletId }?.name ?: "Dompet"
        TransactionDetailDialog(
            transaction = detailTx,
            walletName = walletName,
            onDismiss = { selectedDetailTx = null },
            onDelete = {
                viewModel.deleteTransaction(detailTx)
                selectedDetailTx = null
            }
        )
    }
}
