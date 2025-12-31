package com.jalay.manageexpenses.presentation.ui.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.content.FileProvider
import com.jalay.manageexpenses.domain.usecase.ExportDataUseCase
import com.jalay.manageexpenses.presentation.ui.components.*
import com.jalay.manageexpenses.presentation.ui.theme.*
import com.jalay.manageexpenses.presentation.viewmodel.ExportUiState
import com.jalay.manageexpenses.presentation.viewmodel.ExportViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel()
) {
    var selectedFormat by remember { mutableStateOf(ExportDataUseCase.ExportFormat.CSV) }
    var selectedDateRange by remember { mutableStateOf(DateRangeOption.ThisMonth) }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Export Transactions",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Export Format Card
            ShadcnCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Text(
                        text = "Export Format",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        FormatOption(
                            format = ExportDataUseCase.ExportFormat.CSV,
                            selected = selectedFormat == ExportDataUseCase.ExportFormat.CSV,
                            onClick = { selectedFormat = ExportDataUseCase.ExportFormat.CSV },
                            modifier = Modifier.weight(1f)
                        )
                        FormatOption(
                            format = ExportDataUseCase.ExportFormat.PDF,
                            selected = selectedFormat == ExportDataUseCase.ExportFormat.PDF,
                            onClick = { selectedFormat = ExportDataUseCase.ExportFormat.PDF },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Date Range Card
            ShadcnCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Text(
                        text = "Date Range",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))

                    // First row: This Month, Last 30 Days, Last Month
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        listOf(DateRangeOption.ThisMonth, DateRangeOption.Last30Days, DateRangeOption.LastMonth).forEach { range ->
                            FilterChip(
                                selected = selectedDateRange == range,
                                onClick = {
                                    selectedDateRange = range
                                    startDate = null
                                    endDate = null
                                },
                                label = {
                                    Text(
                                        text = range.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.sm))

                    // Second row: This Year, All Time, Custom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        listOf(DateRangeOption.ThisYear, DateRangeOption.AllTime, DateRangeOption.Custom).forEach { range ->
                            FilterChip(
                                selected = selectedDateRange == range,
                                onClick = {
                                    selectedDateRange = range
                                    if (range != DateRangeOption.Custom) {
                                        startDate = null
                                        endDate = null
                                    }
                                },
                                label = {
                                    Text(
                                        text = range.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (selectedDateRange == DateRangeOption.Custom) {
                        Spacer(modifier = Modifier.height(Spacing.md))

                        // Start Date
                        OutlinedTextField(
                            value = startDate?.let { formatDate(it) } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Start Date") },
                            leadingIcon = {
                                Icon(Icons.Default.CalendarToday, contentDescription = null)
                            },
                            trailingIcon = {
                                TextButton(onClick = { showStartDatePicker = true }) {
                                    Text("Select")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Radius.md)
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        // End Date
                        OutlinedTextField(
                            value = endDate?.let { formatDate(it) } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("End Date") },
                            leadingIcon = {
                                Icon(Icons.Default.CalendarToday, contentDescription = null)
                            },
                            trailingIcon = {
                                TextButton(onClick = { showEndDatePicker = true }) {
                                    Text("Select")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Radius.md)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Export Button
            PrimaryButton(
                text = "Export Transactions",
                onClick = {
                    val (startTime, endTime) = if (selectedDateRange == DateRangeOption.Custom) {
                        Pair(startDate?.time, endDate?.time)
                    } else {
                        selectedDateRange.getDateRange()
                    }
                    viewModel.export(selectedFormat, startTime, endTime)
                },
                enabled = uiState !is ExportUiState.Exporting && isValidDateRange(startDate, endDate, selectedDateRange),
                modifier = Modifier.fillMaxWidth()
            )

            when (val state = uiState) {
                is ExportUiState.Success -> {
                    ShadcnCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.lg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IconBadge(
                                icon = Icons.Default.CheckCircle,
                                size = 48.dp,
                                backgroundColor = IncomeGreen.copy(alpha = 0.1f),
                                iconColor = IncomeGreen
                            )

                            Spacer(modifier = Modifier.height(Spacing.md))

                            Text(
                                text = "Export Successful!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(Spacing.sm))

                            Text(
                                text = "File saved to Downloads",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(Spacing.md))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                            ) {
                                SecondaryButton(
                                    text = "Share",
                                    onClick = { shareFile(context, state.filePath) },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = { viewModel.resetState() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Close")
                                }
                            }
                        }
                    }
                }

                is ExportUiState.Error -> {
                    ShadcnCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.lg)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                            ) {
                                IconBadge(
                                    icon = Icons.Default.ErrorOutline,
                                    size = 32.dp,
                                    backgroundColor = ExpenseRed.copy(alpha = 0.1f),
                                    iconColor = ExpenseRed
                                )
                                Text(
                                    text = "Export Failed",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(Spacing.md))

                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(Spacing.md))

                            SecondaryButton(
                                text = "Try Again",
                                onClick = { viewModel.resetState() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                else -> {}
            }
        }
    }

    // Date Pickers
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        startDate = Date(it)
                        showStartDatePicker = false
                    }
                }) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        endDate = Date(it)
                        showEndDatePicker = false
                    }
                }) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun isValidDateRange(startDate: Date?, endDate: Date?, range: DateRangeOption): Boolean {
    if (range != DateRangeOption.Custom) return true
    if (startDate == null || endDate == null) return false
    return !startDate.after(endDate)
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(date)
}

private fun shareFile(context: Context, filePath: String) {
    try {
        val file = File(filePath)
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = when (filePath.substringAfterLast('.')) {
                "csv" -> "text/csv"
                "pdf" -> "application/pdf"
                else -> "*/*"
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(intent, "Share exported file")
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

enum class DateRangeOption(val label: String) {
    ThisMonth("This Month"),
    Last30Days("Last 30 Days"),
    LastMonth("Last Month"),
    ThisYear("This Year"),
    AllTime("All Time"),
    Custom("Custom");

    fun getDateRange(): Pair<Long?, Long?> {
        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()

        return when (this) {
            ThisMonth -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                Pair(calendar.timeInMillis, now)
            }
            Last30Days -> {
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                Pair(calendar.timeInMillis, now)
            }
            LastMonth -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                val end = calendar.timeInMillis - 1
                Pair(start, end)
            }
            ThisYear -> {
                calendar.set(Calendar.MONTH, Calendar.JANUARY)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                Pair(calendar.timeInMillis, now)
            }
            AllTime -> Pair(null, null)
            Custom -> Pair(null, null) // Handled separately
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatOption(
    format: ExportDataUseCase.ExportFormat,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = format.name,
                style = MaterialTheme.typography.labelMedium
            )
        },
        modifier = modifier,
        shape = RoundedCornerShape(Radius.md),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        )
    )
}
