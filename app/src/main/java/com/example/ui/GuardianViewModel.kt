package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GuardianViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DatabaseProvider.getDatabase(application)
    private val repository = GuardianRepository(
        userDao = database.userDao(),
        taskDao = database.taskDao(),
        habitDao = database.habitDao()
    )

    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<User?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _criticalRiskAlert = MutableStateFlow<Task?>(null)
    val criticalRiskAlert: StateFlow<Task?> = _criticalRiskAlert.asStateFlow()

    private val alertedTasks = mutableSetOf<Long>()

    init {
        viewModelScope.launch {
            repository.ensureDefaultUser()
            allHabits.first().let { habits ->
                if (habits.isEmpty()) {
                    repository.addHabit("Study 2 Hours", "Daily")
                    repository.addHabit("Code Gym Challenge", "Daily")
                    repository.addHabit("Review Weekly Calendar", "Weekly")
                }
            }
        }

        // Real-Time Alert & Haptic Feedback System Observer
        viewModelScope.launch {
            allTasks.collect { tasks ->
                tasks.forEach { task ->
                    if (task.status != "COMPLETED" && task.riskLevel == "HIGH") {
                        if (!alertedTasks.contains(task.id)) {
                            alertedTasks.add(task.id)
                            AndroidAlertSystem.triggerCriticalAlert(getApplication(), task)
                            _criticalRiskAlert.value = task
                        }
                    } else {
                        alertedTasks.remove(task.id)
                    }
                }
            }
        }
    }

    fun clearCriticalRiskAlert() {
        _criticalRiskAlert.value = null
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun addTask(
        title: String,
        description: String = "",
        category: String,
        difficulty: Int,
        estimatedHours: Double,
        daysRemaining: Int,
        userPriority: String? = null,
        manualSubtasks: List<SubTask>? = null,
        importanceLevel: Int = 3
    ) {
        if (title.isBlank()) {
            _uiMessage.value = "Task title cannot be empty."
            return
        }
        if (daysRemaining < 0) {
            _uiMessage.value = "Deadline cannot be in the past!"
            return
        }
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                repository.addTask(
                    title = title,
                    description = description,
                    category = category,
                    difficulty = difficulty,
                    estimatedHours = estimatedHours,
                    daysRemaining = daysRemaining,
                    userPriority = userPriority,
                    manualSubtasks = manualSubtasks,
                    importanceLevel = importanceLevel
                )
                _uiMessage.value = if (manualSubtasks != null) "Task created with manual roadmap!" else "Task created! AI is breaking down details..."
            } catch (e: Exception) {
                _uiMessage.value = "Failed to create task: ${e.localizedMessage}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun toggleSubTask(taskId: Long, subTaskTitle: String) {
        viewModelScope.launch {
            try {
                repository.toggleSubTask(taskId, subTaskTitle)
            } catch (e: Exception) {
                _uiMessage.value = "Failed to update subtask: ${e.localizedMessage}"
            }
        }
    }

    fun toggleTask(taskId: Long) {
        viewModelScope.launch {
            try {
                repository.toggleTask(taskId)
            } catch (e: Exception) {
                _uiMessage.value = "Failed to update task completion: ${e.localizedMessage}"
            }
        }
    }

    fun triggerRescue(taskId: Long, hoursLeft: Int) {
        if (hoursLeft <= 0) {
            _uiMessage.value = "Please enter a positive number of hours left."
            return
        }
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                repository.triggerEmergencyRescue(taskId, hoursLeft)
                _uiMessage.value = "🚨 EMERGENCY OVERDRIVE PLAN ACTIVATED!"
            } catch (e: Exception) {
                _uiMessage.value = "Failed to activate emergency rescue: ${e.localizedMessage}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun forceRiskRecalculation(taskId: Long) {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                repository.refreshRiskForTask(taskId)
                _uiMessage.value = "AI risk metrics updated successfully."
            } catch (e: Exception) {
                _uiMessage.value = "Failed to update risk metrics: ${e.localizedMessage}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun addHabit(name: String, frequency: String) {
        if (name.isBlank()) {
            _uiMessage.value = "Habit name cannot be empty."
            return
        }
        viewModelScope.launch {
            try {
                repository.addHabit(name, frequency)
                _uiMessage.value = "New habit added."
            } catch (e: Exception) {
                _uiMessage.value = "Failed to add habit: ${e.localizedMessage}"
            }
        }
    }

    fun toggleHabit(habitId: Long, dateString: String) {
        viewModelScope.launch {
            try {
                repository.toggleHabitLog(habitId, dateString)
            } catch (e: Exception) {
                _uiMessage.value = "Failed to update log: ${e.localizedMessage}"
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteTask(taskId)
                _uiMessage.value = "Task deleted."
            } catch (e: Exception) {
                _uiMessage.value = "Failed to delete task."
            }
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteHabit(habitId)
                _uiMessage.value = "Habit deleted."
            } catch (e: Exception) {
                _uiMessage.value = "Failed to delete habit."
            }
        }
    }

    fun addFocusMinutes(taskId: Long, minutes: Long) {
        viewModelScope.launch {
            try {
                repository.addFocusTime(taskId, minutes)
                _uiMessage.value = "Focused for $minutes minutes!"
            } catch (e: Exception) {
                _uiMessage.value = "Failed to update focus time."
            }
        }
    }

    fun clearAllTasks() {
        viewModelScope.launch {
            try {
                repository.clearAllTasks()
                _uiMessage.value = "All tasks cleared successfully!"
            } catch (e: Exception) {
                _uiMessage.value = "Failed to clear tasks."
            }
        }
    }

    fun triggerNotificationOrVibration(title: String, message: String) {
        AndroidAlertSystem.triggerGeneralAlert(getApplication(), title, message)
    }

    fun getTodayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
