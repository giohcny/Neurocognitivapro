package com.neurocognitiva.pro.report

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.neurocognitiva.pro.domain.ReportBuilder
import com.neurocognitiva.pro.domain.ReportData
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Genera el informe en PDF A4 usando solo APIs de Android (sin librerías externas). */
class PdfExporter(private val context: Context) {
    private val pageW = 595
    private val pageH = 842
    private val margin = 40f
    private val contentW get() = pageW - 2 * margin

    private lateinit var doc: PdfDocument
    private var page: PdfDocument.Page? = null
    private lateinit var canvas: Canvas
    private var y = 0f
    private var pageNo = 0

    private val ink = Color.parseColor("#0F172A")
    private val muted = Color.parseColor("#64748B")
    private val accent = Color.parseColor("#4F46E5")

    private fun paint(size: Float, bold: Boolean = false, color: Int = ink) = TextPaint().apply {
        isAntiAlias = true
        textSize = size
        this.color = color
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }

    private fun newPage() {
        page?.let { finishPage(it) }
        pageNo += 1
        val info = PdfDocument.PageInfo.Builder(pageW, pageH, pageNo).create()
        val p = doc.startPage(info)
        page = p
        canvas = p.canvas
        y = margin
    }

    private fun finishPage(p: PdfDocument.Page) {
        val foot = paint(8f, color = muted)
        canvas.drawText("NeuroCognitiva Pro · Informe de tamizaje y estimulación cognitiva — no constituye diagnóstico", margin, pageH - 22f, foot)
        canvas.drawText("Pág. $pageNo", pageW - margin - 30f, pageH - 22f, foot)
        doc.finishPage(p)
    }

    private fun ensure(height: Float) {
        if (y + height > pageH - 44f) newPage()
    }

    /** Dibuja un párrafo con salto de línea automático. Devuelve la altura usada. */
    private fun paragraph(text: String, p: TextPaint, width: Float = contentW, x: Float = margin, gap: Float = 6f) {
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, p, width.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f, 1f)
            .build()
        ensure(layout.height.toFloat().coerceAtMost(pageH - 120f))
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
        y += layout.height + gap
    }

    private fun heading(text: String) {
        ensure(34f)
        y += 8f
        canvas.drawText(text.uppercase(), margin, y + 10f, paint(11f, true, accent))
        y += 14f
        val line = Paint().apply { color = Color.parseColor("#C7D2FE"); strokeWidth = 1f }
        canvas.drawLine(margin, y, pageW - margin, y, line)
        y += 8f
    }

    fun export(r: ReportData, aiText: String?): File {
        doc = PdfDocument()
        pageNo = 0
        page = null
        newPage()
        val p = r.patient

        // Encabezado
        val band = Paint().apply { color = Color.parseColor("#0F172A") }
        canvas.drawRoundRect(RectF(margin, y, pageW - margin, y + 62f), 10f, 10f, band)
        canvas.drawText("INFORME PSICOLÓGICO DE FUNCIONES COGNITIVAS", margin + 14f, y + 26f, paint(14f, true, Color.WHITE))
        canvas.drawText("Código ${r.code} · Fecha ${r.dateLabel}", margin + 14f, y + 46f, paint(9f, color = Color.parseColor("#94A3B8")))
        y += 74f

        if (r.hasSimulated) {
            val warn = Paint().apply { color = Color.parseColor("#FEF3C7") }
            canvas.drawRoundRect(RectF(margin, y, pageW - margin, y + 24f), 6f, 6f, warn)
            canvas.drawText("CONTIENE DATOS SIMULADOS (DEMOSTRACIÓN) — NO USAR COMO REGISTRO CLÍNICO", margin + 10f, y + 16f, paint(8.5f, true, Color.parseColor("#92400E")))
            y += 32f
        }

        heading("I. Identificación del evaluado")
        val body = paint(9.5f)
        listOf(
            "Nombre" to p.name, "Documento" to p.doc, "Edad" to "${p.age} años",
            "Género / Lateralidad" to "${p.gender} / ${p.handedness}", "Escolaridad" to p.education,
            "Ocupación" to p.occupation, "Evaluador" to p.evaluator, "Ámbito de aplicación" to p.appTier,
            "Protocolo" to p.evalProtocol, "Motivo / observaciones" to p.notes
        ).forEach { (k, v) ->
            paragraph("$k: ${v.ifBlank { "—" }}", body, gap = 2f)
        }

        heading("II. Perfil global")
        val g = r.global
        if (g != null) {
            paragraph("Índice Cognitivo Global (ICG): ${g.icg}/100 — ${g.category}", paint(11f, true))
            paragraph("Precisión media ${g.accuracy}% · Latencia media ${g.rtMs} ms · Nivel máximo ${g.maxLevel}/8 · ${r.sessionCount} sesiones en ${r.playedCount}/10 dominios", body)
            drawRadar(r)
        } else {
            paragraph("Sin datos suficientes: no se han completado actividades.", body)
        }

        heading("III. Detalle psicométrico por función")
        tableRow(listOf("Función", "Nivel", "Prec.", "TR ms", "Índice", "Clasificación"), header = true)
        r.metrics.forEach { m ->
            if (m.played) {
                tableRow(listOf(m.activity.shortTitle, "N${m.maxLevel}", "${m.accuracy}%", "${m.avgRtMs}", "${m.indexScore}", m.classification.label))
            } else {
                tableRow(listOf(m.activity.shortTitle, "—", "—", "—", "—", "No evaluado"))
            }
        }

        heading("IV. Análisis cualitativo por función")
        r.qualitative.filter { it.metric.played }.forEach { q ->
            paragraph(q.metric.activity.title, paint(10f, true), gap = 1f)
            paragraph(ReportBuilder.stripMarks(q.text), body, gap = 8f)
        }

        heading("V. Conclusión integrada")
        r.synthesis.forEach { paragraph(ReportBuilder.stripMarks(it), body, gap = 8f) }

        if (!aiText.isNullOrBlank()) {
            heading("Interpretación avanzada asistida por IA (requiere revisión profesional)")
            paragraph(aiText, body, gap = 8f)
        }

        ensure(70f)
        y += 24f
        canvas.drawLine(margin, y, margin + 200f, y, Paint().apply { color = ink; strokeWidth = 1f })
        canvas.drawText(p.evaluator.ifBlank { "Evaluador/a" }, margin, y + 14f, paint(9f, true))
        canvas.drawText("Firma y timbre profesional", margin, y + 26f, paint(8f, color = muted))
        y += 40f

        page?.let { finishPage(it) }
        val file = File(ShareUtils.exportDir(context), "informe_${r.code}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    private fun tableRow(cells: List<String>, header: Boolean = false) {
        val widths = floatArrayOf(0.30f, 0.09f, 0.10f, 0.11f, 0.10f, 0.30f).map { it * contentW }
        ensure(18f)
        if (header) {
            canvas.drawRect(RectF(margin, y, pageW - margin, y + 16f), Paint().apply { color = Color.parseColor("#E0E7FF") })
        }
        val tp = paint(8.5f, header)
        var x = margin + 4f
        cells.forEachIndexed { i, text ->
            var t = text
            while (t.length > 3 && tp.measureText(t) > widths[i] - 6f) t = t.dropLast(1)
            if (t != text) t = t.dropLast(1) + "…"
            canvas.drawText(t, x, y + 11.5f, tp)
            x += widths[i]
        }
        y += 17f
    }

    private fun drawRadar(r: ReportData) {
        val size = 190f
        ensure(size + 30f)
        val cx = pageW / 2f
        val cy = y + size / 2f + 8f
        val radius = size / 2f - 14f
        val n = r.metrics.size
        val grid = Paint().apply { color = Color.parseColor("#CBD5E1"); style = Paint.Style.STROKE; strokeWidth = 0.7f }
        fun pt(i: Int, f: Float): Pair<Float, Float> {
            val a = -PI / 2 + 2 * PI * i / n
            return (cx + (radius * f * cos(a)).toFloat()) to (cy + (radius * f * sin(a)).toFloat())
        }
        listOf(0.25f, 0.5f, 0.75f, 1f).forEach { f ->
            val path = Path()
            for (i in 0 until n) { val (x, yy) = pt(i, f); if (i == 0) path.moveTo(x, yy) else path.lineTo(x, yy) }
            path.close()
            canvas.drawPath(path, grid)
        }
        val area = Path()
        for (i in 0 until n) {
            val v = if (r.metrics[i].played) r.metrics[i].indexScore / 100f else 0f
            val (x, yy) = pt(i, v)
            if (i == 0) area.moveTo(x, yy) else area.lineTo(x, yy)
        }
        area.close()
        canvas.drawPath(area, Paint().apply { color = Color.parseColor("#554F46E5"); style = Paint.Style.FILL })
        canvas.drawPath(area, Paint().apply { color = accent; style = Paint.Style.STROKE; strokeWidth = 1.6f; isAntiAlias = true })
        val lp = paint(6.5f, true, muted).apply { textAlign = Paint.Align.CENTER }
        for (i in 0 until n) {
            val (x, yy) = pt(i, 1.17f)
            canvas.drawText(r.metrics[i].activity.shortTitle.take(16), x, yy + 2f, lp)
        }
        y += size + 24f
    }
}
