package com.example.modul_5

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val photoRepository = PhotoRepository(application.applicationContext)

    private val _photos = MutableStateFlow<List<PhotoEntry>>(emptyList())
    val photos: StateFlow<List<PhotoEntry>> = _photos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadPhotos()
    }

    fun loadPhotos() {
        viewModelScope.launch {
            _isLoading.value = true
            val photos = photoRepository.loadAllPhotos()
            _photos.value = photos
            _isLoading.value = false
        }
    }

    fun addPhoto(filePath: String, fileName: String) {
        viewModelScope.launch {
            val newPhoto = PhotoEntry(
                fileName = fileName,
                filePath = filePath,
                timestamp = System.currentTimeMillis()
            )
            _photos.value = listOf(newPhoto) + _photos.value
        }
    }

    fun deletePhoto(fileName: String) {
        viewModelScope.launch {
            val success = photoRepository.deletePhoto(fileName)
            if (success) {
                _photos.value = _photos.value.filter { it.fileName != fileName }
            }
        }
    }

    fun getLastPhotoFile(): File = photoRepository.getLastPhotoFile()
        ?: throw IllegalStateException("No photo file created")

    fun exportPhotoToGallery(fileName: String): Boolean {
        val photo = _photos.value.find { it.fileName == fileName } ?: return false
        val file = File(photo.filePath)
        return photoRepository.exportToGallery(file)
    }

    fun createImageFile(): File = photoRepository.createImageFile()

    fun getUriForFile(file: File): android.net.Uri = photoRepository.getUriForFile(file)
}