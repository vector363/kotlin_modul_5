package com.example.modul_5

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class PhotoRepository(private val context: Context) {

    private val photoDir: File
        get() = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: context.filesDir

    private var lastCreatedFile: File? = null

    /**
     * Создать файл для фото
     */
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_${timeStamp}.jpg"
        val file = File(photoDir, imageFileName)
        lastCreatedFile = file  // ЭТО НУЖНО ДОБАВИТЬ!
        return file
    }

    fun getLastPhotoFile(): File? = lastCreatedFile

    /**
     * Получить URI для FileProvider
     */
    fun getUriForFile(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Загрузить все фото из папки
     */
    fun loadAllPhotos(): List<PhotoEntry> {
        return photoDir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".jpg") }
            ?.map { file ->
                PhotoEntry(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    timestamp = file.lastModified()
                )
            }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()
    }

    /**
     * Сохранить фото из битмапа (например, после сжатия)
     */
    fun saveBitmapToFile(bitmap: Bitmap, file: File): Boolean {
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Экспортировать фото в общую галерею (MediaStore)
     */
    fun exportToGallery(file: File): Boolean {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyApp")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return false

            context.contentResolver.openOutputStream(uri)?.use { output ->
                file.inputStream().use { input ->
                    input.copyTo(output)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }

            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun deletePhoto(fileName: String): Boolean {
        val file = File(photoDir, fileName)
        return if (file.exists()) {
            val deleted = file.delete()
            deleted
        } else {
            false
        }
    }

    /**
     * Получить Bitmap для отображения (сжатый)
     */
    fun getBitmapThumbnail(filePath: String, width: Int = 200, height: Int = 200): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(filePath, options)

            // Вычисляем сжатие
            val scale = calculateInSampleSize(options, width, height)

            val finalOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
            }
            BitmapFactory.decodeFile(filePath, finalOptions)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}