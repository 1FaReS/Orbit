package com.flowtask.app.domain.model

data class Tag(
    val id: Long = 0,
    val name: String,
    val icon: String? = null,
    val colorId: String,
)
