package com.example.data

import android.util.Log
import com.example.api.GeminiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class GuardianRepository(
    private val userDao: UserDao,
    private val taskDao: TaskDao,
    private val habitDao: HabitDao
) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasksFlow()
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabitsFlow()
    val currentUser: Flow<User?> = userDao.getUser()

    suspend fun ensureDefaultUser() = withContext(Dispatchers.IO) {
        val existing = currentUser.firstOrNull() ?: run {
            val defaultUser = User(
                id = 1,
                name = "Yash Patil",
                email = "yashpatil200618@gmail.com",
                activeHoursStart = "09:00",
                activeHoursEnd = "18:00",
                preferencesJson = "{}"
            )
            userDao.insertUser(defaultUser)
            Log.d("GuardianRepository", "Default user created.")
            defaultUser
        }

        // Run background sync
        FirestoreSyncManager.fetchAndMergeAll(
            taskDao = taskDao,
            habitDao = habitDao,
            userDao = userDao,
            userEmail = existing.email
        )
    }

    suspend fun addTask(
        title: String,
        description: String = "",
        category: String,
        difficulty: Int,
        estimatedHours: Double,
        daysRemaining: Int,
        userPriority: String? = null,
        manualSubtasks: List<SubTask>? = null,
        importanceLevel: Int = 3
    ): Long = withContext(Dispatchers.IO) {
        // Step 1: Calculate Priority via AI Agent Core or use user override
        val priorityResult = if (userPriority != null && userPriority != "AUTO") {
            val reasoningPrompt = "Explain in 2 short sentences why setting '$title' ($category, difficulty $difficulty/5, importance $importanceLevel/5) as $userPriority priority is logical. Description: $description"
            val aiReasoning = try {
                val resp = GeminiClient.queryGemini(reasoningPrompt, "You are DeadlineX helper.")
                // Strip off extra quotes or json braces if AI outputs raw text
                resp.replace("\"", "").replace("{", "").replace("}", "").trim()
            } catch (e: Exception) {
                ""
            }
            PriorityResult(
                priority = userPriority,
                reasoning = if (aiReasoning.isNotBlank()) aiReasoning else "Priority set manually by user to $userPriority."
            )
        } else {
            GeminiClient.calculatePriority(
                title = title,
                description = description,
                category = category,
                difficulty = difficulty,
                estimatedHours = estimatedHours,
                daysRemaining = daysRemaining,
                importanceLevel = importanceLevel
            )
        }

        // Step 2: Breakdown Task via AI Agent Core or use manual subtasks list if provided and not empty
        val subtasks = if (manualSubtasks != null && manualSubtasks.isNotEmpty()) {
            manualSubtasks
        } else {
            GeminiClient.breakdownTask(
                title = title,
                description = description,
                category = category,
                difficulty = difficulty,
                estimatedHours = estimatedHours,
                daysRemaining = daysRemaining
            )
        }

        val subtasksJson = JsonParser.toJsonList(subtasks, SubTask::class.java)

        // Calculate deadline timestamp
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysRemaining)
        val deadlineTimestamp = calendar.timeInMillis

        val task = Task(
            title = title,
            description = description,
            category = category,
            deadline = deadlineTimestamp,
            difficulty = difficulty,
            estimatedHours = estimatedHours,
            completionPercentage = 0,
            priority = priorityResult.priority,
            status = "PENDING",
            reasoning = priorityResult.reasoning,
            subtasksJson = subtasksJson,
            importanceLevel = importanceLevel
        )

        val newId = taskDao.insertTask(task)

        // Step 3: Automatically run risk analysis to set initial risk metrics
        refreshRiskForTask(newId)

        newId
    }

    suspend fun refreshRiskForTask(taskId: Long) = withContext(Dispatchers.IO) {
        val task = taskDao.getTaskById(taskId) ?: return@withContext
        val daysRemaining = getDaysRemaining(task.deadline)

        val riskResult = GeminiClient.predictRisk(
            title = task.title,
            difficulty = task.difficulty,
            estimatedHours = task.estimatedHours,
            daysRemaining = daysRemaining,
            completionPercentage = task.completionPercentage
        )

        val updatedTask = task.copy(
            riskLevel = riskResult.riskLevel,
            completionProbability = riskResult.completionProbability,
            riskSuggestionsJson = JsonParser.toJsonList(riskResult.suggestions, String::class.java)
        )

        taskDao.updateTask(updatedTask)
        val email = currentUser.firstOrNull()?.email ?: "yashpatil200618@gmail.com"
        FirestoreSyncManager.saveTaskToFirebase(email, updatedTask)
        Log.d("GuardianRepository", "Risk refreshed for task $taskId: ${riskResult.riskLevel}")
    }

    suspend fun triggerEmergencyRescue(taskId: Long, hoursLeft: Int) = withContext(Dispatchers.IO) {
        val task = taskDao.getTaskById(taskId) ?: return@withContext
        if (task.status == "COMPLETED") {
            throw IllegalArgumentException("Cannot activate Emergency Rescue for a completed task!")
        }

        val plan = GeminiClient.generateEmergencyPlan(
            title = task.title,
            estimatedHours = task.estimatedHours,
            hoursLeft = hoursLeft,
            completionPercentage = task.completionPercentage
        )

        val updatedTask = task.copy(
            riskLevel = "HIGH", // Overwrite risk to high under emergency
            emergencyScheduleJson = JsonParser.toJson(plan)
        )

        taskDao.updateTask(updatedTask)
        val email = currentUser.firstOrNull()?.email ?: "yashpatil200618@gmail.com"
        FirestoreSyncManager.saveTaskToFirebase(email, updatedTask)
        Log.d("GuardianRepository", "Emergency plan generated for task $taskId")
    }

    suspend fun toggleSubTask(taskId: Long, subTaskTitle: String) = withContext(Dispatchers.IO) {
        val task = taskDao.getTaskById(taskId) ?: return@withContext
        val subtasks = JsonParser.fromJsonList(task.subtasksJson, SubTask::class.java).toMutableList()

        var updatedIndex = -1
        for (i in subtasks.indices) {
            if (subtasks[i].title == subTaskTitle) {
                updatedIndex = i
                break
            }
        }

        if (updatedIndex != -1) {
            val target = subtasks[updatedIndex]
            val newStatus = if (target.status == "COMPLETED") "PENDING" else "COMPLETED"
            subtasks[updatedIndex] = target.copy(status = newStatus)

            val completedCount = subtasks.count { it.status == "COMPLETED" }
            val newPercentage = if (subtasks.isEmpty()) 0 else (completedCount * 100) / subtasks.size
            val newStatusString = when {
                newPercentage >= 100 -> "COMPLETED"
                newPercentage > 0 -> "IN_PROGRESS"
                else -> "PENDING"
            }

            val updatedTask = task.copy(
                subtasksJson = JsonParser.toJsonList(subtasks, SubTask::class.java),
                completionPercentage = newPercentage,
                status = newStatusString,
                emergencyScheduleJson = if (newStatusString == "COMPLETED") "" else task.emergencyScheduleJson
            )

            taskDao.updateTask(updatedTask)

            // Re-trigger risk metrics calculation since progress changed
            refreshRiskForTask(taskId)
        }
    }

    suspend fun toggleTask(taskId: Long) = withContext(Dispatchers.IO) {
        val task = taskDao.getTaskById(taskId) ?: return@withContext
        val newStatus = if (task.status == "COMPLETED") "PENDING" else "COMPLETED"
        val newPercentage = if (newStatus == "COMPLETED") 100 else 0

        // Also update subtasks if they exist
        val subtasks = if (task.subtasksJson.isNotEmpty()) {
            JsonParser.fromJsonList(task.subtasksJson, SubTask::class.java).map {
                it.copy(status = if (newStatus == "COMPLETED") "COMPLETED" else "PENDING")
            }
        } else {
            emptyList()
        }

        val updatedTask = task.copy(
            status = newStatus,
            completionPercentage = newPercentage,
            subtasksJson = JsonParser.toJsonList(subtasks, SubTask::class.java),
            emergencyScheduleJson = if (newStatus == "COMPLETED") "" else task.emergencyScheduleJson
        )

        taskDao.updateTask(updatedTask)
        val email = currentUser.firstOrNull()?.email ?: "yashpatil200618@gmail.com"
        FirestoreSyncManager.saveTaskToFirebase(email, updatedTask)

        // Re-trigger risk metrics calculation since progress changed
        refreshRiskForTask(taskId)
    }

    suspend fun addHabit(name: String, frequency: String) = withContext(Dispatchers.IO) {
        val habit = Habit(
            name = name,
            frequency = frequency,
            logsJson = "[]"
        )
        val newId = habitDao.insertHabit(habit)
        val insertedHabit = habit.copy(id = newId)
        val email = currentUser.firstOrNull()?.email ?: "yashpatil200618@gmail.com"
        FirestoreSyncManager.saveHabitToFirebase(email, insertedHabit)
    }

    suspend fun toggleHabitLog(habitId: Long, dateString: String) = withContext(Dispatchers.IO) {
        val habits = habitDao.getAllHabitsFlow().firstOrNull() ?: return@withContext
        val habit = habits.firstOrNull { it.id == habitId } ?: return@withContext

        val logs = JsonParser.fromJsonList(habit.logsJson, String::class.java).toMutableList()
        if (logs.contains(dateString)) {
            logs.remove(dateString)
        } else {
            logs.add(dateString)
        }

        val updated = habit.copy(logsJson = JsonParser.toJsonList(logs, String::class.java))
        habitDao.updateHabit(updated)
        val email = currentUser.firstOrNull()?.email ?: "yashpatil200618@gmail.com"
        FirestoreSyncManager.saveHabitToFirebase(email, updated)
    }

    suspend fun deleteHabit(habitId: Long) = withContext(Dispatchers.IO) {
        habitDao.deleteHabitById(habitId)
        val email = currentUser.firstOrNull()?.email ?: "yashpatil200618@gmail.com"
        FirestoreSyncManager.deleteHabitFromFirebase(email, habitId)
    }

    suspend fun deleteTask(taskId: Long) = withContext(Dispatchers.IO) {
        taskDao.deleteTaskById(taskId)
        val email = currentUser.firstOrNull()?.email ?: "yashpatil200618@gmail.com"
        FirestoreSyncManager.deleteTaskFromFirebase(email, taskId)
    }

    suspend fun clearAllTasks() = withContext(Dispatchers.IO) {
        taskDao.deleteAllTasks()
        val email = currentUser.firstOrNull()?.email ?: "yashpatil200618@gmail.com"
        FirestoreSyncManager.clearAllTasksFromFirebase(email)
    }

    suspend fun getTaskById(taskId: Long): Task? = withContext(Dispatchers.IO) {
        taskDao.getTaskById(taskId)
    }

    suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task)
        val email = currentUser.firstOrNull()?.email ?: "yashpatil200618@gmail.com"
        FirestoreSyncManager.saveTaskToFirebase(email, task)
    }

    suspend fun addFocusTime(taskId: Long, minutes: Long) = withContext(Dispatchers.IO) {
        val task = taskDao.getTaskById(taskId) ?: return@withContext
        val updated = task.copy(focusMinutesSpent = task.focusMinutesSpent + minutes)
        taskDao.updateTask(updated)
        val email = currentUser.firstOrNull()?.email ?: "yashpatil200618@gmail.com"
        FirestoreSyncManager.saveTaskToFirebase(email, updated)
    }

    private fun getDaysRemaining(deadlineTimestamp: Long): Int {
        val diff = deadlineTimestamp - System.currentTimeMillis()
        val days = (diff / (1000 * 60 * 60 * 24)).toInt()
        return if (days < 0) 0 else days
    }
}
