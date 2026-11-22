package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageEncodingHelper {

  private const val DATA_URI_PREFIX = "data:image/jpeg;base64,"

  suspend fun encodeUriToCompactDataUrl(
    context: Context,
    uri: Uri,
    maxDimensionPx: Int = 380,
    maxBytes: Int = 95_000,
  ): String? =
    withContext(Dispatchers.IO) {
      try {
        val resolver = context.applicationContext.contentResolver
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { stream ->
          BitmapFactory.decodeStream(stream, null, boundsOptions)
        }
        val origW = boundsOptions.outWidth
        val origH = boundsOptions.outHeight
        if (origW <= 0 || origH <= 0) return@withContext null

        var sampleSize = 1
        while (origW / sampleSize > maxDimensionPx * 2 || origH / sampleSize > maxDimensionPx * 2) {
          sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded =
          resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
          } ?: return@withContext null

        val scale =
          minOf(
            maxDimensionPx.toFloat() / decoded.width.toFloat(),
            maxDimensionPx.toFloat() / decoded.height.toFloat(),
            1f,
          )
        val targetW = (decoded.width * scale).toInt().coerceAtLeast(1)
        val targetH = (decoded.height * scale).toInt().coerceAtLeast(1)
        val scaled =
          if (scale < 1f) Bitmap.createScaledBitmap(decoded, targetW, targetH, true) else decoded
        if (scaled !== decoded) decoded.recycle()

        var quality = 76
        var bytes: ByteArray
        do {
          val out = ByteArrayOutputStream()
          scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
          bytes = out.toByteArray()
          quality -= 12
        } while (bytes.size > maxBytes && quality >= 28)

        scaled.recycle()
        if (bytes.size > maxBytes) return@withContext null
        DATA_URI_PREFIX + Base64.encodeToString(bytes, Base64.NO_WRAP)
      } catch (_: Exception) {
        null
      }
    }

  fun decodeDataUrlToImageBitmap(dataUrl: String): ImageBitmap? {
    if (dataUrl.isBlank()) return null
    return try {
      val base64Part = dataUrl.substringAfter("base64,", dataUrl)
      val bytes = Base64.decode(base64Part, Base64.DEFAULT)
      val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
      bitmap.asImageBitmap()
    } catch (_: Exception) {
      null
    }
  }
}
