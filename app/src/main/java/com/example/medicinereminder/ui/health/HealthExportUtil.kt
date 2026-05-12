package com.example.medicinereminder.ui.health

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.medicinereminder.data.local.db.HealthRecordWithIndicator
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HealthExportUtil {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun exportToCsv(context: Context, records: List<HealthRecordWithIndicator>): Uri {
        val file = File(context.cacheDir, "health_records_export.csv")
        file.printWriter(Charsets.UTF_8).use { writer ->
            // Write BOM + header for Excel Chinese text compatibility
            writer.print('﻿')
            writer.println("日期,指标名称,数值,单位,备注")
            for (record in records.sortedBy { it.recordedAt }) {
                val date = dateTimeFormat.format(Date(record.recordedAt))
                val name = escapeCsv(record.indicatorName)
                val value = escapeCsv(record.value)
                val unit = escapeCsv(record.indicatorUnit)
                val notes = escapeCsv(record.notes)
                writer.println("$date,$name,$value,$unit,$notes")
            }
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }

    fun createShareIntent(context: Context, uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun escapeCsv(value: String): String {
        if (value.isEmpty()) return ""
        val needsQuoting = value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')
        return if (needsQuoting) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
