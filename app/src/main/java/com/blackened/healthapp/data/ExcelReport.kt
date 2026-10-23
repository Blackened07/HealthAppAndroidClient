package com.blackened.healthapp.data

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.blackened.healthapp.data.network.HealthRecordResponseDTO
import com.blackened.healthapp.presentation.home.formatServerDate
import org.dhatim.fastexcel.Workbook
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

object ExcelReport {

    fun createReport(context: Context, reportList: List<HealthRecordResponseDTO>, userName: String): File? {
        if (reportList.isEmpty()) return null

        val dateStr = LocalDateTime.now().toString().substring(0, 10)
        val filepath = "${context.getExternalFilesDir(null)}${File.separator}${dateStr}${userName}_report.xlsx"
        val file = File(filepath)

        var element = reportList[0]

        try {
            FileOutputStream(file).use { os ->
                Workbook(os, "HealthAppFront", "1.0").use { wb ->

                    val sheet = wb.newWorksheet("Отчёт по ${element.type}")

                    sheet.value(0, 0, element.type)
                    sheet.range(0, 0, 0, 3).merge()
                    sheet.style(0, 0).bold().horizontalAlignment("center").set()

                    sheet.value(1, 0, "№")
                    sheet.value(1, 1, "Дата")
                    sheet.value(1, 2, "Значение")
                    sheet.value(1, 3, "Комментарий")

                    for (i in reportList.indices) {
                        element = reportList[i]

                        sheet.value(i + 2, 0, (i + 1).toString() + ")")
                        sheet.value(i + 2, 1, formatServerDate(element.timestamp))

                        val displayValue = if (element.value2 != null) {
                            "${element.value1} / ${element.value2}"
                        } else {
                            "${element.value1}"
                        }

                        sheet.value(i + 2, 2, displayValue)

                        sheet.value(i + 2, 3, element.note ?: "")
                    }

                    sheet.finish()
                }
            }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            return null
        }
    }

    fun shareFile(context: Context, file: File) {
        try {
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Отправить отчет через...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка отправки: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}