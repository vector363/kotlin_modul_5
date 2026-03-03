package com.example.modul_5.domain.model

data class Task(
    val id: Int = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val createdAt: Long,
    val completedAt: Long? = null
)