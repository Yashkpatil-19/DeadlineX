package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubTask(
    val title: String,
    val durationMinutes: Int,
    val status: String = "PENDING", // PENDING, COMPLETED
    val scheduledTime: String = "" // e.g., "Day 1, 10:00 AM"
)

@JsonClass(generateAdapter = true)
data class RescueSlot(
    val timeSlot: String, // e.g., "09:00 - 11:00"
    val activity: String // e.g., "Intense project architecture design"
)

@JsonClass(generateAdapter = true)
data class EmergencyPlan(
    val rescueSchedule: List<RescueSlot> = emptyList(),
    val activitiesToCancel: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PriorityResult(
    val priority: String, // HIGH, MEDIUM, LOW
    val reasoning: String
)

@JsonClass(generateAdapter = true)
data class RiskResult(
    val completionProbability: Int, // e.g., 65
    val riskLevel: String, // HIGH, MEDIUM, LOW
    val suggestions: List<String> = emptyList()
)
