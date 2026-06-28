package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await

object FirestoreSyncManager {
    private const val TAG = "FirestoreSyncManager"
    private var firestore: FirebaseFirestore? = null

    fun initialize(context: Context) {
        try {
            // Safely check if Firebase has its parsed config resources
            val resId = context.resources.getIdentifier("google_app_id", "string", context.packageName)
            val isConfigured = resId != 0 && context.getString(resId).isNotEmpty()

            if (isConfigured) {
                // Initialize standard FirebaseApp
                FirebaseApp.initializeApp(context)
                firestore = FirebaseFirestore.getInstance()
                Log.i(TAG, "Firebase Firestore initialized successfully!")
            } else {
                Log.w(TAG, "google-services.json is missing or not configured. Running in Local-Only offline mode.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback warning: Firestore could not be initialized: ${e.localizedMessage}")
        }
    }

    fun isAvailable(): Boolean = firestore != null

    suspend fun fetchAndMergeAll(
        taskDao: TaskDao,
        habitDao: HabitDao,
        userDao: UserDao,
        userEmail: String
    ) {
        val db = firestore ?: return
        try {
            Log.i(TAG, "Starting Firestore background synchronization for: $userEmail")

            // 1. Sync User Document
            val userDoc = db.collection("users").document(userEmail).get().await()
            if (userDoc.exists()) {
                val firestoreUser = userDoc.data?.toUser()
                if (firestoreUser != null) {
                    userDao.insertUser(firestoreUser)
                }
            }

            // 2. Sync Tasks
            val tasksSnapshot = db.collection("users").document(userEmail).collection("tasks").get().await()
            val firestoreTasks = tasksSnapshot.documents.mapNotNull { it.data?.toTask() }
            val localTasks = taskDao.getAllTasksFlow().firstOrNull() ?: emptyList()

            // Room -> Firestore
            for (localTask in localTasks) {
                val existsInFirestore = firestoreTasks.any { it.id == localTask.id }
                if (!existsInFirestore) {
                    db.collection("users")
                        .document(userEmail)
                        .collection("tasks")
                        .document(localTask.id.toString())
                        .set(localTask.toMap(), SetOptions.merge())
                        .await()
                }
            }

            // Firestore -> Room
            for (fsTask in firestoreTasks) {
                val existsLocally = localTasks.any { it.id == fsTask.id }
                if (!existsLocally) {
                    taskDao.insertTask(fsTask)
                } else {
                    taskDao.updateTask(fsTask)
                }
            }

            // 3. Sync Habits
            val habitsSnapshot = db.collection("users").document(userEmail).collection("habits").get().await()
            val firestoreHabits = habitsSnapshot.documents.mapNotNull { it.data?.toHabit() }
            val localHabits = habitDao.getAllHabitsFlow().firstOrNull() ?: emptyList()

            // Room -> Firestore
            for (localHabit in localHabits) {
                val existsInFirestore = firestoreHabits.any { it.id == localHabit.id }
                if (!existsInFirestore) {
                    db.collection("users")
                        .document(userEmail)
                        .collection("habits")
                        .document(localHabit.id.toString())
                        .set(localHabit.toMap(), SetOptions.merge())
                        .await()
                }
            }

            // Firestore -> Room
            for (fsHabit in firestoreHabits) {
                val existsLocally = localHabits.any { it.id == fsHabit.id }
                if (!existsLocally) {
                    habitDao.insertHabit(fsHabit)
                } else {
                    habitDao.updateHabit(fsHabit)
                }
            }

            Log.i(TAG, "Firestore synchronization completed successfully!")
        } catch (e: Exception) {
            Log.e(TAG, "Firestore sync skipped/failed: ${e.localizedMessage}")
        }
    }

    suspend fun saveTaskToFirebase(userEmail: String, task: Task) {
        val db = firestore ?: return
        try {
            db.collection("users")
                .document(userEmail)
                .collection("tasks")
                .document(task.id.toString())
                .set(task.toMap(), SetOptions.merge())
                .await()
            Log.d(TAG, "Saved task ${task.id} to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving task ${task.id} to Firestore: ${e.localizedMessage}")
        }
    }

    suspend fun deleteTaskFromFirebase(userEmail: String, taskId: Long) {
        val db = firestore ?: return
        try {
            db.collection("users")
                .document(userEmail)
                .collection("tasks")
                .document(taskId.toString())
                .delete()
                .await()
            Log.d(TAG, "Deleted task $taskId from Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting task $taskId from Firestore: ${e.localizedMessage}")
        }
    }

    suspend fun clearAllTasksFromFirebase(userEmail: String) {
        val db = firestore ?: return
        try {
            val tasksSnapshot = db.collection("users").document(userEmail).collection("tasks").get().await()
            for (doc in tasksSnapshot.documents) {
                doc.reference.delete().await()
            }
            Log.d(TAG, "All tasks deleted from Firestore for $userEmail")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing tasks from Firestore: ${e.localizedMessage}")
        }
    }

    suspend fun saveHabitToFirebase(userEmail: String, habit: Habit) {
        val db = firestore ?: return
        try {
            db.collection("users")
                .document(userEmail)
                .collection("habits")
                .document(habit.id.toString())
                .set(habit.toMap(), SetOptions.merge())
                .await()
            Log.d(TAG, "Saved habit ${habit.id} to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving habit ${habit.id} to Firestore: ${e.localizedMessage}")
        }
    }

    suspend fun deleteHabitFromFirebase(userEmail: String, habitId: Long) {
        val db = firestore ?: return
        try {
            db.collection("users")
                .document(userEmail)
                .collection("habits")
                .document(habitId.toString())
                .delete()
                .await()
            Log.d(TAG, "Deleted habit $habitId from Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting habit $habitId from Firestore: ${e.localizedMessage}")
        }
    }

    suspend fun saveUserToFirebase(user: User) {
        val db = firestore ?: return
        try {
            db.collection("users")
                .document(user.email)
                .set(user.toMap(), SetOptions.merge())
                .await()
            Log.d(TAG, "Saved user ${user.email} to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user ${user.email} to Firestore: ${e.localizedMessage}")
        }
    }

    // Explicit mappings to avoid serialization problems with annotations
    private fun Task.toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "category" to category,
            "deadline" to deadline,
            "difficulty" to difficulty,
            "estimatedHours" to estimatedHours,
            "completionPercentage" to completionPercentage,
            "priority" to priority,
            "status" to status,
            "reasoning" to reasoning,
            "subtasksJson" to subtasksJson,
            "riskLevel" to riskLevel,
            "completionProbability" to completionProbability,
            "riskSuggestionsJson" to riskSuggestionsJson,
            "emergencyScheduleJson" to emergencyScheduleJson,
            "importanceLevel" to importanceLevel,
            "focusMinutesSpent" to focusMinutesSpent
        )
    }

    private fun Map<String, Any?>.toTask(): Task {
        return Task(
            id = (this["id"] as? Number)?.toLong() ?: 0L,
            title = this["title"] as? String ?: "",
            description = this["description"] as? String ?: "",
            category = this["category"] as? String ?: "",
            deadline = (this["deadline"] as? Number)?.toLong() ?: 0L,
            difficulty = (this["difficulty"] as? Number)?.toInt() ?: 1,
            estimatedHours = (this["estimatedHours"] as? Number)?.toDouble() ?: 0.0,
            completionPercentage = (this["completionPercentage"] as? Number)?.toInt() ?: 0,
            priority = this["priority"] as? String ?: "MEDIUM",
            status = this["status"] as? String ?: "PENDING",
            reasoning = this["reasoning"] as? String ?: "",
            subtasksJson = this["subtasksJson"] as? String ?: "[]",
            riskLevel = this["riskLevel"] as? String ?: "LOW",
            completionProbability = (this["completionProbability"] as? Number)?.toInt() ?: 100,
            riskSuggestionsJson = this["riskSuggestionsJson"] as? String ?: "[]",
            emergencyScheduleJson = this["emergencyScheduleJson"] as? String ?: "",
            importanceLevel = (this["importanceLevel"] as? Number)?.toInt() ?: 3,
            focusMinutesSpent = (this["focusMinutesSpent"] as? Number)?.toLong() ?: 0L
        )
    }

    private fun Habit.toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "frequency" to frequency,
            "logsJson" to logsJson
        )
    }

    private fun Map<String, Any?>.toHabit(): Habit {
        return Habit(
            id = (this["id"] as? Number)?.toLong() ?: 0L,
            name = this["name"] as? String ?: "",
            frequency = this["frequency"] as? String ?: "",
            logsJson = this["logsJson"] as? String ?: "[]"
        )
    }

    private fun User.toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "email" to email,
            "activeHoursStart" to activeHoursStart,
            "activeHoursEnd" to activeHoursEnd,
            "preferencesJson" to preferencesJson
        )
    }

    private fun Map<String, Any?>.toUser(): User {
        return User(
            id = (this["id"] as? Number)?.toInt() ?: 1,
            name = this["name"] as? String ?: "",
            email = this["email"] as? String ?: "",
            activeHoursStart = this["activeHoursStart"] as? String ?: "09:00",
            activeHoursEnd = this["activeHoursEnd"] as? String ?: "17:00",
            preferencesJson = this["preferencesJson"] as? String ?: "{}"
        )
    }
}
