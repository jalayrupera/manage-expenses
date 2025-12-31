package com.jalay.manageexpenses.presentation.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jalay.manageexpenses.AppContainer
import com.jalay.manageexpenses.domain.model.TransactionType
import com.jalay.manageexpenses.presentation.ui.theme.ReceivedColor
import com.jalay.manageexpenses.presentation.ui.theme.SentColor
import com.jalay.manageexpenses.presentation.viewmodel.TransactionDetailUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    appContainer: AppContainer,
    transactionId: Long,
    onNavigateBack: () -> Unit
) {
    val viewModel: com.jalay.manageexpenses.presentation.viewmodel.TransactionDetailViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = TransactionDetailViewModelFactory(appContainer)
        )
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is TransactionDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is TransactionDetailUiState.Success -> {
                state.transaction?.let { transaction ->
                    TransactionDetailContent(
                        transaction = transaction,
                        availableCategories = state.availableCategories,
                        onNotesChange = { viewModel.updateNotes(it.ifBlank { null }) },
                        onCategoryChange = { viewModel.updateCategory(it) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
            }
            is TransactionDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Error: ${state.message}")
                }
            }
        }
    }
}

@Composable
fun TransactionDetailContent(
    transaction: com.jalay.manageexpenses.domain.model.Transaction,
    availableCategories: List<String>,
    onNotesChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var notesText by remember { mutableStateOf(transaction.notes ?: "") }
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                com.jalay.manageexpenses.presentation.ui.components.CategoryIcon(
                    category = transaction.category,
                    size = 64.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                val color = if (transaction.transactionType == TransactionType.SENT) {
                    SentColor
                } else {
                    ReceivedColor
                }
                val prefix = if (transaction.transactionType == TransactionType.SENT) "-" else "+"
                Text(
                    text = "$prefix₹${transaction.amount}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = color
                )
                Text(
                    text = transaction.recipientName,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        DetailRow("Date", formatDate(transaction.timestamp))
        DetailRow("Type", transaction.transactionType.name)
        DetailRow("UPI App", transaction.upiApp)
        DetailRow("Reference", transaction.transactionRef ?: "N/A")

        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            label = { Text("Category") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        var expanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change Category")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableCategories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            onCategoryChange(category)
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = notesText,
            onValueChange = {
                notesText = it
                onNotesChange(it)
            },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

class TransactionDetailViewModelFactory(
    private val appContainer: AppContainer
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(com.jalay.manageexpenses.presentation.viewmodel.TransactionDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return com.jalay.manageexpenses.presentation.viewmodel.TransactionDetailViewModel(
                getTransactionsUseCase = appContainer.getGetTransactionsUseCase(appContainer.getContext()),
                updateTransactionNotesUseCase = appContainer.getUpdateTransactionNotesUseCase(appContainer.getContext()),
                updateCategoryUseCase = appContainer.getUpdateCategoryUseCase(appContainer.getContext())
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}