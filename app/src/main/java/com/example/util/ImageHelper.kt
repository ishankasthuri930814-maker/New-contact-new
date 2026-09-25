package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max

object ImageHelper {
    private const val TAG = "ImageHelper"
    private const val MAX_DIMENSION = 300
    private const val COMPRESS_QUALITY = 75

    /**
     * Converts a Uri to an optimized, compressed Base64 JPEG string.
     */
    suspend fun uriToBase64(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, boundsOptions)
            inputStream?.close()

            val originalWidth = boundsOptions.outWidth
            val originalHeight = boundsOptions.outHeight
            if (originalWidth <= 0 || originalHeight <= 0) return@withContext null

            var sampleSize = 1
            val maxOriginal = max(originalWidth, originalHeight)
            if (maxOriginal > MAX_DIMENSION) {
                sampleSize = maxOriginal / MAX_DIMENSION
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            inputStream = context.contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (bitmap == null) return@withContext null

            // Handle orientation
            try {
                val exifStream = context.contentResolver.openInputStream(uri)
                if (exifStream != null) {
                    val exif = ExifInterface(exifStream)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    exifStream.close()

                    val rotation = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }

                    if (rotation != 0f) {
                        val matrix = Matrix().apply { postRotate(rotation) }
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Exif orientation read failed: ${e.message}")
            }

            // Scale if still larger than max dimension
            val currentMax = max(bitmap.width, bitmap.height)
            if (currentMax > MAX_DIMENSION) {
                val scale = MAX_DIMENSION.toFloat() / currentMax
                val newW = (bitmap.width * scale).toInt()
                val newH = (bitmap.height * scale).toInt()
                bitmap = Bitmap.createScaledBitmap(bitmap, newW, newH, true)
            }

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            Log.e(TAG, "Error converting image to Base64", e)
            null
        }
    }
}
