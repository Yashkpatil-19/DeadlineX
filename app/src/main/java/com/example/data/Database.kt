package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String,
    val activeHoursStart: String = "09:00",
    val activeHoursEnd: String = "17:00",
    val preferencesJson: String = "{}"
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "", // Added description
    val category: String, // Assignment, Exam, Project, etc.
    val deadline: Long, // timestamp
    val difficulty: Int, // 1 to 5
    val estimatedHours: Double,
    val completionPercentage: Int = 0,
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val status: String = "PENDING", // PENDING, IN_PROGRESS, COMPLETED
    val reasoning: String = "",
    val subtasksJson: String = "[]", // Serialized List<SubTask>
    val riskLevel: String = "LOW", // HIGH, MEDIUM, LOW
    val completionProbability: Int = 100,
    val riskSuggestionsJson: String = "[]", // Serialized List<String>
    val emergencyScheduleJson: String = "", // Serialized emergency rescue plan
    val importanceLevel: Int = 3, // Added Importance level (1-5)
    val focusMinutesSpent: Long = 0
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val frequency: String, // Daily, Weekly
    val logsJson: String = "[]" // Serialized List<String> (dates of completion)
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 1 LIMIT 1")
    fun getUser(): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY deadline ASC")
    fun getAllTasksFlow(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun getTaskByIdFlow(id: Long): Flow<Task?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun getAllHabitsFlow(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)
}

@Database(entities = [User::class, Task::class, Habit::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
}

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "deadline_guardian_database"
            )
            .fallbackToDestructiveMigration()
            .build()
            INSTANCE = instance
            instance
        }
    }
}
