package com.example.nexthire.model

data class NotificationItem(
    val message: String,
    val time: String,
    val jobTitle: String = "",
    val jobCompany: String = "",
    val jobId: String
)
