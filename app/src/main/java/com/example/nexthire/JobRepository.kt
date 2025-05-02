package com.example.nexthire.data

import com.example.nexthire.model.Job // ✅ This line is required!

object JobRepository {
    private fun generateId(title: String, company: String, location: String): String {
        fun clean(text: String) = text.trim().lowercase().replace("[^a-z0-9]+".toRegex(), "_")
        return "${clean(title)}_${clean(company)}_${clean(location)}"
    }

    val jobs = listOf(
        Job(
            id = generateId("Software Engineer", "Google", "California"),
            title = "Software Engineer",
            company = "Google",
            location = "California",
            type = "Full-time",
            salary = "$90K - $120K",
            description = "Develop amazing apps.",
            experience = "3+ years"
        ),
        Job(
            id = generateId("UI/UX Designer", "Apple", "California"),
            title = "UI/UX Designer",
            company = "Apple",
            location = "California",
            type = "Full-time",
            salary = "$80K - $100K",
            description = "Design clean UIs.",
            experience = "2+ years"
        ),
        Job(
            id = generateId("Android Developer", "Meta", "New York"),
            title = "Android Developer",
            company = "Meta",
            location = "New York",
            type = "Contract",
            salary = "$100K - $140K",
            description = "Kotlin and Jetpack Compose.",
            experience = "5+ years"
        ),
        Job(
            id = generateId("Backend Developer", "Amazon", "Seattle"),
            title = "Backend Developer",
            company = "Amazon",
            location = "Seattle",
            type = "Full-time",
            salary = "$95K - $130K",
            description = "Spring Boot preferred.",
            experience = "3+ years"
        ),
        Job(
            id = generateId("Data Scientist", "Netflix", "Los Angeles"),
            title = "Data Scientist",
            company = "Netflix",
            location = "Los Angeles",
            type = "Remote",
            salary = "$110K - $150K",
            description = "Python, ML stack.",
            experience = "2+ years"
        )
    )
}
