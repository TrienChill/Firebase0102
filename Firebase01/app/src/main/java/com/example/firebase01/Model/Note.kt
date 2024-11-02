package com.example.firebase01.Model

data class Note(
    var id: String = "",
    var title: String = "",
    var content: String = "",
    var createdDate: Long = System.currentTimeMillis()
)