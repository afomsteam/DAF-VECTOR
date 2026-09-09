package com.afomsteam.enlistedplanner.util

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.afomsteam.enlistedplanner.data.Mga
import com.afomsteam.enlistedplanner.data.PlannerState
import com.afomsteam.enlistedplanner.logic.Dates
import java.io.File
import java.time.LocalDate

object PdfExporter {
    fun createQuarterSummary(context: Context, state: PlannerState): File {
        val today = LocalDate.now()
        val quarter = ((today.monthValue - 1) / 3) + 1
        val start = LocalDate.of(today.year, (quarter - 1) * 3 + 1, 1)
        val items = state.accomplishments.filter { Dates.parse(it.date)?.let { d -> !d.isBefore(start) && !d.isAfter(today) } == true }

        val doc = PdfDocument()
        val paint = Paint().apply { isAntiAlias = true }
        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(612, 792, pageNumber).create())
        var canvas = page.canvas
        var y = 52f

        fun line(text: String, size: Float = 11f, bold: Boolean = false, indent: Float = 0f) {
            if (y > 742f) {
                doc.finishPage(page)
                pageNumber++
                page = doc.startPage(PdfDocument.PageInfo.Builder(612, 792, pageNumber).create())
                canvas = page.canvas
                y = 52f
            }
            paint.textSize = size
            paint.isFakeBoldText = bold
            val max = 86
            text.chunked(max).forEach { chunk ->
                canvas.drawText(chunk, 48f + indent, y, paint)
                y += size + 5f
            }
        }

        line("ENLISTED PLANNER — Q$quarter ${today.year} CAREER SUMMARY", 16f, true)
        line("${state.profile.grade.label} ${state.profile.name}".trim(), 12f, true)
        line("Planning aid • Generated ${Dates.display(today.toString())}", 9f)
        y += 8f
        line("PERFORMANCE EVIDENCE", 13f, true)
        Mga.entries.forEach { mga ->
            val group = items.filter { it.mga == mga }
            line("${mga.label}: ${group.size}", 11f, true)
            group.forEach { a ->
                line("• ${a.action}", 10f, false, 8f)
                if (a.impact.isNotBlank()) line("  Impact: ${a.impact}", 9f, false, 12f)
                if (a.result.isNotBlank()) line("  Result: ${a.result}", 9f, false, 12f)
                if (a.evidence.isNotBlank()) line("  Evidence: ${a.evidence}", 9f, false, 12f)
                if (a.challenge.isNotBlank()) line("  Challenge/assistance: ${a.challenge}", 9f, false, 12f)
                line("  ${a.date} • ${a.impactLevel}${a.alq.takeIf { it.isNotBlank() }?.let { " • $it" } ?: ""}${a.mileFocus.takeIf { it.isNotBlank() }?.let { " • $it" } ?: ""}", 8f, false, 12f)
            }
        }
        y += 8f
        line("DEVELOPMENT", 13f, true)
        state.goals.filterNot { it.completed }.forEach { g -> line("• ${g.title}${g.nextAction.takeIf { it.isNotBlank() }?.let { " — Next: $it" } ?: ""}", 10f) }
        y += 8f
        line("EXPERIENCE", 13f, true)
        state.experience.takeLast(12).forEach { e -> line("• ${e.title} (${e.category})${e.impact.takeIf { it.isNotBlank() }?.let { " — $it" } ?: ""}", 10f) }
        y += 12f
        line("This export is a personal planning summary, not an official personnel record. Verify official requirements and records in the appropriate DAF system.", 8f)

        doc.finishPage(page)
        val file = File(context.cacheDir, "Enlisted-Planner-Q$quarter-${today.year}.pdf")
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }, "Share quarter summary"))
    }
}
