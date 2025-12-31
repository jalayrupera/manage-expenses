package com.jalay.manageexpenses.domain.usecase

import android.content.Context
import android.os.Environment
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.jalay.manageexpenses.data.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportDataUseCase(
    private val context: Context,
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        format: ExportFormat,
        startTime: Long? = null,
        endTime: Long? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val transactions = if (startTime != null && endTime != null) {
                repository.getTransactionsByDateRange(startTime, endTime).first()
            } else {
                repository.getAllTransactions().first()
            }

            val fileName = when (format) {
                ExportFormat.CSV -> "expenses_${System.currentTimeMillis()}.csv"
                ExportFormat.PDF -> "expenses_${System.currentTimeMillis()}.pdf"
            }

            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            val file = File(downloadsDir, fileName)

            when (format) {
                ExportFormat.CSV -> exportToCsv(transactions, file)
                ExportFormat.PDF -> exportToPdf(transactions, file)
            }

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun exportToCsv(transactions: List<com.jalay.manageexpenses.domain.model.Transaction>, file: File) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val sentTransactions = transactions.filter { it.transactionType.name == "SENT" }
        val receivedTransactions = transactions.filter { it.transactionType.name == "RECEIVED" }
        val totalSent = sentTransactions.sumOf { it.amount }
        val totalReceived = receivedTransactions.sumOf { it.amount }
        val netAmount = totalReceived - totalSent

        val rows = transactions.map { transaction ->
            listOf(
                dateFormat.format(java.util.Date(transaction.timestamp)),
                transaction.transactionType.name,
                transaction.recipientName,
                transaction.category,
                transaction.amount.toString(),
                transaction.notes ?: "",
                transaction.upiApp,
                transaction.transactionRef ?: ""
            )
        }

        csvWriter().open(file) {
            writeRow(listOf("Date", "Type", "Recipient", "Category", "Amount", "Notes", "UPI App", "Reference"))
            rows.forEach { row -> writeRow(row) }

            writeRow(listOf())
            writeRow(listOf("Totals"))
            writeRow(listOf("Total Sent:", "₹${String.format("%.2f", totalSent)}"))
            writeRow(listOf("Total Received:", "₹${String.format("%.2f", totalReceived)}"))
            writeRow(listOf("Net Amount:", "₹${String.format("%.2f", netAmount)}"))
        }
    }

    private fun exportToPdf(transactions: List<com.jalay.manageexpenses.domain.model.Transaction>, file: File) {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val paint = android.graphics.Paint()
        val boldPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            isFakeBoldText = true
        }
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val sentTransactions = transactions.filter { it.transactionType.name == "SENT" }
        val receivedTransactions = transactions.filter { it.transactionType.name == "RECEIVED" }
        val totalSent = sentTransactions.sumOf { it.amount }
        val totalReceived = receivedTransactions.sumOf { it.amount }
        val netAmount = totalReceived - totalSent

        val transactionsToExport = transactions.take(50)
        val itemsPerPage = 25
        val pages = transactionsToExport.chunked(itemsPerPage)

        pages.forEachIndexed { pageIndex, pageTransactions ->
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pageIndex + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            if (pageIndex == 0) {
                boldPaint.textSize = 24f
                canvas.drawText("UPI Transactions Report", 150f, 50f, boldPaint)

                paint.textSize = 12f
                val reportDate = "Generated: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())}"
                canvas.drawText(reportDate, 150f, 75f, paint)

                paint.textSize = 14f
                paint.isFakeBoldText = true
                canvas.drawText("Summary", 40f, 110f, paint)
                paint.isFakeBoldText = false
                paint.textSize = 12f

                val startY = 130f
                canvas.drawText("Total Transactions: ${transactions.size}", 40f, startY, paint)
                canvas.drawText("Total Sent: ₹${String.format("%.2f", totalSent)}", 40f, startY + 20f, paint)
                canvas.drawText("Total Received: ₹${String.format("%.2f", totalReceived)}", 40f, startY + 40f, paint)
                canvas.drawText("Net Amount: ₹${String.format("%.2f", netAmount)}", 40f, startY + 60f, paint)

                boldPaint.textSize = 14f
                canvas.drawText("Transactions", 40f, startY + 100f, boldPaint)

                paint.isFakeBoldText = true
                canvas.drawText("Date", 40f, startY + 130f, paint)
                canvas.drawText("Type", 120f, startY + 130f, paint)
                canvas.drawText("Recipient", 180f, startY + 130f, paint)
                canvas.drawText("Amount", 450f, startY + 130f, paint)
                paint.isFakeBoldText = false

                paint.strokeWidth = 1f
                canvas.drawLine(40f, startY + 140f, 550f, startY + 140f, paint)
            }

            paint.textSize = 10f
            var yPos = if (pageIndex == 0) 290f else 50f

            pageTransactions.forEach { transaction ->
                val date = dateFormat.format(java.util.Date(transaction.timestamp))
                val recipient = transaction.recipientName.take(20)
                val type = transaction.transactionType.name.take(4)

                canvas.drawText(date, 40f, yPos, paint)
                canvas.drawText(type, 120f, yPos, paint)
                canvas.drawText(recipient, 180f, yPos, paint)
                canvas.drawText("₹${String.format("%.2f", transaction.amount)}", 450f, yPos, paint)
                yPos += 18f
            }

            pdfDocument.finishPage(page)
        }

        if (pages.isEmpty()) {
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            boldPaint.textSize = 24f
            canvas.drawText("No transactions to export", 150f, 400f, boldPaint)
            pdfDocument.finishPage(page)
        }

        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        pdfDocument.close()
        fos.close()
    }

    enum class ExportFormat {
        CSV,
        PDF
    }
}