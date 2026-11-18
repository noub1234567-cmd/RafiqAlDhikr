package com.rafiqaldhikr.app.core.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.rafiqaldhikr.app.R
import java.io.File

/** Draws a dhikr or du'a as a PNG card (Amiri font, with its source) and shares it. */
object ShareCard {
    private const val WIDTH = 1080
    private const val MARGIN = 90
    private const val MAX_BODY_HEIGHT = 3200

    fun render(context: Context, text: String, source: String): Bitmap {
        val regular = ResourcesCompat.getFont(context, R.font.amiri_regular)
        val bold = ResourcesCompat.getFont(context, R.font.amiri_bold)
        val innerWidth = WIDTH - 2 * MARGIN

        fun layout(content: String, size: Float, color: Int, typeface: android.graphics.Typeface?): StaticLayout {
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                textSize = size
                this.typeface = typeface
            }
            return StaticLayout.Builder.obtain(content, 0, content.length, paint, innerWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_RTL)
                .setLineSpacing(0f, 1.35f)
                .build()
        }

        var size = 54f
        var body = layout(text, size, Color.parseColor("#1F2A24"), regular)
        while (body.height > MAX_BODY_HEIGHT && size > 28f) {
            size -= 2f
            body = layout(text, size, Color.parseColor("#1F2A24"), regular)
        }
        val src = if (source.isBlank()) null else layout(source, 36f, Color.parseColor("#6B7A70"), regular)
        val title = layout("رفيقك في الذكر", 40f, Color.parseColor("#1B5E4B"), bold)

        val gap = 60
        val height = MARGIN + title.height + gap + body.height + (src?.let { gap + it.height } ?: 0) + MARGIN
        val bmp = Bitmap.createBitmap(WIDTH, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.parseColor("#FBF6EA"))
        val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = Color.parseColor("#1B5E4B")
        }
        c.drawRoundRect(RectF(30f, 30f, WIDTH - 30f, height - 30f), 36f, 36f, frame)

        var y = MARGIN.toFloat()
        fun draw(l: StaticLayout) {
            c.save()
            c.translate(MARGIN.toFloat(), y)
            l.draw(c)
            c.restore()
            y += l.height
        }
        draw(title)
        y += gap
        draw(body)
        if (src != null) {
            y += gap
            draw(src)
        }
        return bmp
    }

    /** Renders the card, saves it in the app cache and returns the share chooser; falls back to plain text. Call off the main thread. */
    fun buildChooser(context: Context, text: String, source: String): Intent {
        val plain = if (source.isBlank()) text else "$text\n$source"
        val intent = runCatching {
            val bmp = render(context, text, source)
            val dir = File(context.cacheDir, "shared").apply { mkdirs() }
            dir.listFiles()?.forEach { it.delete() }
            val file = File(dir, "dhikr_${System.currentTimeMillis()}.png")
            file.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bmp.recycle()
            val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
            Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, plain)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }.getOrElse {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, plain)
            }
        }
        return Intent.createChooser(intent, "مشاركة").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
