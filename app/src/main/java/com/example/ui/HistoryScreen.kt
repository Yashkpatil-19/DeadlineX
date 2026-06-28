package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: GuardianViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsState()
    val completedTasks = remember(tasks) { tasks.filter { it.status == "COMPLETED" } }

    // Aggregate statistics
    val totalCompletedCount = completedTasks.size
    val avgDifficulty = if (completedTasks.isNotEmpty()) {
        completedTasks.map { it.difficulty }.average()
    } else 0.0
    val avgImportance = if (completedTasks.isNotEmpty()) {
        completedTasks.map { it.importanceLevel }.average()
    } else 0.0
    val totalHoursSaved = completedTasks.map { it.estimatedHours }.sum()

    // Grouping count for distribution progress
    val assignmentsCount = completedTasks.count { it.category.equals("Assignment", ignoreCase = true) }
    val examsCount = completedTasks.count { it.category.equals("Exam", ignoreCase = true) }
    val projectsCount = completedTasks.count { it.category.equals("Project", ignoreCase = true) }
    val otherCount = completedTasks.size - assignmentsCount - examsCount - projectsCount

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Banner Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CyberSlateMedium)
                    .border(BorderStroke(1.dp, NeonCyan), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "AI TASK HISTORY LOG",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "PAST PRODUCTIVITY ARCHIVE",
                        color = ThemeWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Track finished assignments, exams prep, and milestone projects to review development velocity.",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Stats Overview grid (Bento Row)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Completed card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
                        border = BorderStroke(1.dp, CyberSlateLight),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("history_stat_completed")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "COMPLETED",
                                color = TextGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = totalCompletedCount.toString(),
                                color = NeonEmerald,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Total Hours Accomplished
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
                        border = BorderStroke(1.dp, CyberSlateLight),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("history_stat_hours")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "HOURS DONE",
                                color = TextGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f", totalHoursSaved),
                                color = NeonCyan,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Avg Difficulty
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
                        border = BorderStroke(1.dp, CyberSlateLight),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("history_stat_difficulty")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "AVG DIFFICULTY",
                                color = TextGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f/5", avgDifficulty),
                                color = NeonAmber,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Avg Importance
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
                        border = BorderStroke(1.dp, CyberSlateLight),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("history_stat_importance")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "AVG IMPORTANCE",
                                color = TextGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f/5", avgImportance),
                                color = NeonPurple,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Category Trends Breakdown Chart (Material 3 Beautiful Layout)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
                border = BorderStroke(1.dp, CyberSlateLight),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "PAST PRODUCTIVITY VELOCITY & VECTOR DISTRIBUTION",
                        color = ThemeWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (completedTasks.isEmpty()) {
                        Text(
                            text = "No finished vectors registered to calculate velocity distributions.",
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        )
                    } else {
                        // Progress distribution lines
                        val maxCount = maxOf(assignmentsCount, examsCount, projectsCount, otherCount, 1)

                        CategoryDistributionLine(
                            label = "Assignments",
                            count = assignmentsCount,
                            percentage = (assignmentsCount.toFloat() / completedTasks.size * 100).toInt(),
                            color = NeonPurple,
                            maxCount = maxCount
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CategoryDistributionLine(
                            label = "Exams",
                            count = examsCount,
                            percentage = (examsCount.toFloat() / completedTasks.size * 100).toInt(),
                            color = NeonRose,
                            maxCount = maxCount
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CategoryDistributionLine(
                            label = "Projects",
                            count = projectsCount,
                            percentage = (projectsCount.toFloat() / completedTasks.size * 100).toInt(),
                            color = NeonCyan,
                            maxCount = maxCount
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CategoryDistributionLine(
                            label = "Other Categories",
                            count = otherCount,
                            percentage = (otherCount.toFloat() / completedTasks.size * 100).toInt(),
                            color = NeonAmber,
                            maxCount = maxCount
                        )
                    }
                }
            }
        }

        // History Tasks Title Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMPLETED DEFENSE ARCHIVE",
                    color = ThemeWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${completedTasks.size} Saved",
                    color = NeonEmerald,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Completed Tasks List
        if (completedTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.HistoryToggleOff,
                            contentDescription = "Empty History",
                            tint = TextGray.copy(alpha = 0.25f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Productivity ledger is clean. Complete tasks to secure historical defense vectors.",
                            color = TextGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        } else {
            items(completedTasks, key = { it.id }) { task ->
                HistoryTaskItem(
                    task = task,
                    onRestore = { viewModel.toggleTask(task.id) },
                    onDelete = { viewModel.deleteTask(task.id) }
                )
            }
        }
    }
}

@Composable
fun CategoryDistributionLine(
    label: String,
    count: Int,
    percentage: Int,
    color: Color,
    maxCount: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = ThemeWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "$count task(s) ($percentage%)",
                color = TextGray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { count.toFloat() / maxCount.coerceAtLeast(1) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = CyberSlateLight
        )
    }
}

@Composable
fun HistoryTaskItem(
    task: Task,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("history_item_${task.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
        border = BorderStroke(1.dp, CyberSlateLight),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = NeonEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = task.category.uppercase(),
                        color = NeonPurple,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DIFF: ${task.difficulty}/5",
                        color = NeonAmber,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "IMP: ${task.importanceLevel}/5",
                        color = NeonCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = task.title,
                color = ThemeWhite.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (task.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = task.description,
                    color = TextGray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Divider(color = CyberSlateLight, thickness = 1.dp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Hours spent",
                        tint = TextGray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${task.estimatedHours} Hours saved",
                        color = TextGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Revert back button
                    TextButton(
                        onClick = onRestore,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = "Restore",
                            tint = NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ACTIVATE",
                            color = NeonCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Delete forever button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete from history",
                            tint = NeonRose.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
