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
        }
    }

    private fun exportToPdf(transactions: List<com.jalay.manageexpenses.domain.model.Transaction>, file: File) {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val paint = android.graphics.Paint()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        val transactionsToExport = transactions.take(50)
        val itemsPerPage = 35
        val pages = transactionsToExport.chunked(itemsPerPage)

        pages.forEachIndexed { pageIndex, pageTransactions ->
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pageIndex + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            if (pageIndex == 0) {
                paint.textSize = 24f
                paint.color = android.graphics.Color.BLACK
                canvas.drawText("UPI Transactions Report", 150f, 50f, paint)
            }

            paint.textSize = 10f
            var yPos = if (pageIndex == 0) 100f else 50f

            pageTransactions.forEach { transaction ->
                val date = dateFormat.format(java.util.Date(transaction.timestamp))
                val line = "$date | ${transaction.transactionType.name} | ${transaction.recipientName.take(20)} | ₹${transaction.amount} | ${transaction.category}"
                canvas.drawText(line, 20f, yPos, paint)
                yPos += 20f
            }

            pdfDocument.finishPage(page)
        }

        if (pages.isEmpty()) {
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            paint.textSize = 24f
            canvas.drawText("No transactions to export", 150f, 400f, paint)
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