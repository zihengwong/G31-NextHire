package com.example.nexthire.model

data class Job(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val company: String,
    val location: String,
    val type: String,
    val salary: String,
    val description: String,
    val experience: String,
    var isSaved: Boolean = false,
    var applied: Boolean = false
)