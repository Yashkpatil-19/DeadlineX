package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST
    suspend fun generateContent(
        @Url url: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    fun isApiKeyAvailable(): Boolean {
        val apiKey = BuildConfig.GEMINI_API_KEY
        return apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"
    }

    suspend fun queryGemini(prompt: String, systemInstruction: String? = null): String {
        if (!isApiKeyAvailable()) {
            Log.e("GeminiClient", "API Key is missing or placeholder. Running fallback.")
            return ""
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val url = "v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(responseMimeType = "application/json"),
            systemInstruction = systemInstruction?.let { Content(parts = listOf(Part(text = it))) }
        )

        return try {
            val response = service.generateContent(url, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            Log.d("GeminiClient", "Gemini Response: $text")
            text
        } catch (e: Exception) {
            Log.e("GeminiClient", "Error calling Gemini API", e)
            ""
        }
    }

    suspend fun calculatePriority(
        title: String,
        description: String = "",
        category: String,
        difficulty: Int,
        estimatedHours: Double,
        daysRemaining: Int,
        importanceLevel: Int
    ): PriorityResult {
        val prompt = """
            Analyze the following task and determine its priority (HIGH, MEDIUM, or LOW) and explain your reasoning in 2 short sentences.
            Task Title: $title
            ${if (description.isNotEmpty()) "Description: $description" else ""}
            Category: $category
            Difficulty (1-5): $difficulty
            Importance Level (1-5): $importanceLevel
            Estimated Hours required: $estimatedHours
            Days remaining until deadline: $daysRemaining

            You MUST return a JSON object with these exact keys:
            {
                "priority": "HIGH" | "MEDIUM" | "LOW",
                "reasoning": "your short explanation here"
            }
        """.trimIndent()

        val systemPrompt = "You are DeadlineX, an autonomous task manager that evaluates priorities based on urgency, scope, importance, and difficulty."
        val jsonResponse = queryGemini(prompt, systemPrompt)

        val result = JsonParser.fromJson<PriorityResult>(jsonResponse)
        if (result != null) return result

        // Fallback Logic
        val urgency = (10 - daysRemaining.coerceIn(0, 10)) * 10 // 0 to 100
        val impScore = importanceLevel * 20 // 20 to 100
        val diffScore = difficulty * 20 // 20 to 100
        val timePressure = ((estimatedHours / (daysRemaining.coerceAtLeast(1) * 8.0)) * 100).coerceIn(0.0, 100.0)

        val score = (urgency * 0.35) + (impScore * 0.35) + (diffScore * 0.15) + (timePressure * 0.15)

        val fallbackPriority = when {
            score >= 65 || daysRemaining <= 1 -> "HIGH"
            score >= 35 || daysRemaining <= 4 -> "MEDIUM"
            else -> "LOW"
        }
        val fallbackReasoning = "Calculated locally via Offline Guardian Engine. Priority based on urgency ($daysRemaining days left), importance ($importanceLevel/5), difficulty ($difficulty/5), and time estimate ($estimatedHours hrs)."
        return PriorityResult(priority = fallbackPriority, reasoning = fallbackReasoning)
    }

    suspend fun breakdownTask(
        title: String,
        description: String = "",
        category: String,
        difficulty: Int,
        estimatedHours: Double,
        daysRemaining: Int
    ): List<SubTask> {
        val totalDays = if (daysRemaining <= 0) 1 else daysRemaining
        val totalMinutes = (estimatedHours * 60).toInt().coerceAtLeast(30)

        val prompt = """
            You need to create a customized day-by-day subtask roadmap for this specific task:
            Task Name: $title
            ${if (description.isNotEmpty()) "Description: $description" else ""}
            Category: $category
            Difficulty (1-5): $difficulty
            Estimated Hours Required: $estimatedHours hours (total $totalMinutes minutes)
            Days remaining until deadline: $totalDays days

            Your objective is to generate a sequence of subtasks that adheres strictly to these guidelines:
            1. **Analyze Task Name**: Carefully analyze "$title" and make the step names highly relevant, specific, and themed exactly to this task. Do NOT use generic phase names like "Phase 1: Research" or "Phase 2: Core feature".
            2. **Simple-to-Complex Progression**: Break down the task into small steps that gradually increase in difficulty, starting from the absolute simplest foundational steps up to the final advanced execution steps (leveling up).
               - *Example*: For "Presentation Preparation", the steps should be:
                 1) Write the script/outline (simple start)
                 2) Analyze and refine the script
                 3) Understand the script content thoroughly
                 4) Practice reading and speaking the script out loud
                 5) Give the presentation in front of a real person or camera, and recommend a real-world AI tool (like Yoodli, PowerPoint Presenter Coach, or similar) to rate and give feedback on your delivery.
               - Adapt this model to whatever the task "$title" is!
            3. **Simplest and Easiest Language**: Use extremely simple, clear, non-jargon, and easy-to-understand language. Any beginner should know exactly what to do.
            4. **AI Tool Suggestions for Rating/Feedback**: In the final subtask, always explicitly suggest specific AI tools (e.g., Grammarly, ChatGPT for writing, Yoodli for speech, PowerPoint Presenter Coach for presentations, GitHub Copilot/SonarQube for coding, etc.) that the user can use to rate or analyze their work.
            5. **Distribution**: Distribute the subtasks reasonably across the $totalDays days remaining.
            6. **Actionable & Granular**: Create between 3 and 6 subtasks in total. Specify realistic duration minutes for each.

            You MUST return a JSON array of objects with the exact schema:
            [
              {
                "title": "Subtask title in simple language tailored to the task",
                "durationMinutes": 60,
                "scheduledTime": "Day 1 - 10:00 AM"
              }
            ]
        """.trimIndent()

        val systemPrompt = "You are DeadlineX, an expert strategic planner that breaks down complex task names into simple-to-advanced roadmaps using extremely plain language and recommending AI rating tools."
        val jsonResponse = queryGemini(prompt, systemPrompt)

        val result = JsonParser.fromJsonList(jsonResponse, SubTask::class.java)
        if (result.isNotEmpty()) return result

        // Fallback Logic: Generates a perfectly distributed, balanced, and scheduled roadmap
        val subtasks = mutableListOf<SubTask>()
        val numDays = totalDays.coerceAtMost(6) // Distribute up to 6 days
        val minsPerDay = totalMinutes / numDays

        if (numDays <= 1) {
            // Single-day breakdown
            subtasks.add(SubTask("Phase 1: Research, core specs setup & alignment for $title", (totalMinutes * 0.3).toInt().coerceAtLeast(15), "PENDING", "Day 1 - 09:00 AM"))
            subtasks.add(SubTask("Phase 2: Core feature development & heavy coding", (totalMinutes * 0.5).toInt().coerceAtLeast(30), "PENDING", "Day 1 - 01:30 PM"))
            subtasks.add(SubTask("Phase 3: Integration, polishing interface & validation", (totalMinutes * 0.2).toInt().coerceAtLeast(15), "PENDING", "Day 1 - 06:00 PM"))
        } else {
            // Multi-day distribution
            for (day in 1..numDays) {
                when (day) {
                    1 -> {
                        subtasks.add(SubTask("Phase 1: Initial setup, research, & structural layout for $title", minsPerDay, "PENDING", "Day 1 - 10:00 AM"))
                    }
                    numDays -> {
                        subtasks.add(SubTask("Phase $day: Final testing, bug fixing, styling & deployment", minsPerDay, "PENDING", "Day $day - 04:00 PM"))
                    }
                    else -> {
                        val percentageProgress = ((day - 1) * 100) / (numDays - 1)
                        subtasks.add(SubTask("Phase $day: Build & refine components (Aiming for $percentageProgress% completion)", minsPerDay, "PENDING", "Day $day - 02:00 PM"))
                    }
                }
            }
        }
        return subtasks
    }

    suspend fun predictRisk(
        title: String,
        difficulty: Int,
        estimatedHours: Double,
        daysRemaining: Int,
        completionPercentage: Int
    ): RiskResult {
        val prompt = """
            Evaluate the completion risk for this active task:
            Task: $title
            Difficulty: $difficulty/5
            Hours Estimated: $estimatedHours
            Days remaining: $daysRemaining
            Current Progress: $completionPercentage% Completed

            Analyze if the user will meet the deadline.
            Return a JSON object with these exact keys:
            {
              "completionProbability": 0 to 100,
              "riskLevel": "HIGH" | "MEDIUM" | "LOW",
              "suggestions": ["specific actionable tip 1", "specific actionable tip 2"]
            }
        """.trimIndent()

        val systemPrompt = "You are DeadlineX's risk-analyzer, warning users of dangerous deadline slippages and offering high-impact rescue suggestions."
        val jsonResponse = queryGemini(prompt, systemPrompt)

        val result = JsonParser.fromJson<RiskResult>(jsonResponse)
        if (result != null) return result

        // Fallback Logic
        val percentRemaining = 100 - completionPercentage
        val hoursNeeded = estimatedHours * (percentRemaining / 100.0)
        val workingHoursAvailable = daysRemaining * 4.0 // assume 4 productive hours per day
        val ratio = if (workingHoursAvailable > 0) hoursNeeded / workingHoursAvailable else 2.0

        val (riskLevel, prob, suggestions) = when {
            completionPercentage >= 100 -> Triple("LOW", 100, listOf("Task is already complete. Great job staying ahead of schedule!"))
            daysRemaining <= 0 -> Triple("HIGH", 0, listOf("The deadline has passed! Trigger Emergency Rescue mode now.", "Eliminate all external distractions and work in high-impact sprints."))
            ratio > 1.2 -> Triple("HIGH", 35, listOf(
                "You are severely behind. Estimated hours left exceed available time.",
                "Activate Emergency Rescue Mode to filter out secondary commitments.",
                "Divide into 90-minute hyper-focus blocks with zero phone notification access."
            ))
            ratio > 0.7 -> Triple("MEDIUM", 68, listOf(
                "Progress is moderate but tight. Any minor delay will push you into critical risk.",
                "Frontload the heavy elements today. Do not save key development for the final day.",
                "Cancel at least 1 leisure habit (like Netflix or gaming) to gain buffer hours."
            ))
            else -> Triple("LOW", 92, listOf(
                "On track! Maintain your current pace.",
                "Do a quick quality check on completed parts before doing the remaining items."
            ))
        }

        return RiskResult(completionProbability = prob, riskLevel = riskLevel, suggestions = suggestions)
    }

    suspend fun generateEmergencyPlan(
        title: String,
        estimatedHours: Double,
        hoursLeft: Int,
        completionPercentage: Int
    ): EmergencyPlan {
        val prompt = """
            EMERGENCY OVERDRIVE MODE ACTIVATED.
            We have a critical deadline crisis!
            Task: $title
            Hours Left: $hoursLeft hours remaining before the hard deadline!
            Current Progress: $completionPercentage% Completed.

            We must construct an aggressive, hyper-focused hourly rescue schedule spanning the next 4 to 8 hours to salvage this task.
            Identify also 3 low-priority habits, services, or leisure activities the user must cancel immediately to survive (e.g., Netflix, gaming, social media).

            Return a JSON object with this exact schema:
            {
              "rescueSchedule": [
                { "timeSlot": "Hour 1-2", "activity": "Specific aggressive high-impact action item" }
              ],
              "activitiesToCancel": ["Netflix", "Social Media", "Gaming"]
            }
        """.trimIndent()

        val systemPrompt = "You are DeadlineX in emergency response mode. You deliver uncompromising, high-impact, military-grade productivity rescue plans."
        val jsonResponse = queryGemini(prompt, systemPrompt)

        val result = JsonParser.fromJson<EmergencyPlan>(jsonResponse)
        if (result != null) return result

        // Fallback Logic
        val list = listOf(
            RescueSlot("Hour 1-2", "Setup environment and code the absolute core architecture (no aesthetic polish yet)"),
            RescueSlot("Hour 3-4", "Develop minimum viable features (skip complex secondary parameters)"),
            RescueSlot("Hour 5-6", "Rigorous debugging of critical user path & high-risk issues"),
            RescueSlot("Hour 7", "Perform basic deployment, asset compiling, and smoke testing")
        )
        val cancel = listOf("Netflix / Streaming Services", "Social Media & Infinite Scrolling", "Cozy Multi-step Cooking (Eat simple quick snacks instead)")
        return EmergencyPlan(rescueSchedule = list, activitiesToCancel = cancel)
    }
}
