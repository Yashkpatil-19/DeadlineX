package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

enum class TimerState {
    IDLE, RUNNING, PAUSED, BREAK
}

@Composable
fun FocusTimerScreen(
    viewModel: GuardianViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsState()
    val activeTasks = tasks.filter { it.status != "COMPLETED" }
    
    // Sort so high-priority tasks are first
    val sortedTasks = remember(activeTasks) {
        activeTasks.sortedWith(
            compareByDescending<Task> { it.priority == "HIGH" }
                .thenByDescending { it.importanceLevel }
        )
    }

    var selectedTask by remember { mutableStateOf<Task?>(null) }
    
    // Auto-select first high priority task if none is selected
    LaunchedEffect(sortedTasks) {
        if (selectedTask == null && sortedTasks.isNotEmpty()) {
            selectedTask = sortedTasks.firstOrNull()
        } else if (selectedTask != null) {
            selectedTask = sortedTasks.firstOrNull { it.id == selectedTask!!.id }
        }
    }

    // Timer States
    var workMinutes by remember { mutableStateOf(25) }
    var breakMinutes by remember { mutableStateOf(5) }
    
    var timerState by remember { mutableStateOf(TimerState.IDLE) }
    var secondsRemaining by remember { mutableStateOf(workMinutes * 60) }
    
    // Keep seconds sync when configs change in Idle
    LaunchedEffect(workMinutes, timerState) {
        if (timerState == TimerState.IDLE) {
            secondsRemaining = workMinutes * 60
        }
    }

    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    // Live Clock Countdown Ticker
    LaunchedEffect(timerState, secondsRemaining) {
        if (timerState == TimerState.RUNNING || timerState == TimerState.BREAK) {
            if (secondsRemaining > 0) {
                delay(1000L)
                secondsRemaining -= 1
            } else {
                // Completed session!
                if (timerState == TimerState.RUNNING) {
                    // Log focus time spent
                    selectedTask?.let { task ->
                        viewModel.addFocusMinutes(task.id, workMinutes.toLong())
                    }
                    
                    // Alert of completion (Tactical alerts)
                    viewModel.triggerNotificationOrVibration(
                        title = "Focus Interval Completed",
                        message = "Outstanding job. Tactical focus objective met. Engage rest cycle now."
                    )
                    
                    // Switch to break state
                    timerState = TimerState.BREAK
                    secondsRemaining = breakMinutes * 60
                } else {
                    // Break completed
                    viewModel.triggerNotificationOrVibration(
                        title = "Break Interval Concluded",
                        message = "System rest cycle concluded. Re-engage tactical deadline objective."
                    )
                    timerState = TimerState.IDLE
                    secondsRemaining = workMinutes * 60
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        // --- SECTION 1: POLISHED TACTICAL CHRONOMETER DISPLAY ---
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
            border = BorderStroke(1.dp, if (timerState == TimerState.RUNNING) NeonCyan else CyberSlateLight),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tactical Status Badge
                val (badgeText, badgeColor) = when (timerState) {
                    TimerState.IDLE -> "SYSTEM STANDBY" to TextGray
                    TimerState.RUNNING -> "TACTICAL FOCUS INTERVAL ENGAGED" to NeonCyan
                    TimerState.PAUSED -> "TACTICAL PAUSE VECTOR ACTIVE" to NeonAmber
                    TimerState.BREAK -> "SYSTEM REST MATRIX DEPLOYED" to NeonEmerald
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .border(BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f)), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(badgeColor)
                        )
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Timer Visual Progress Arc & Numeric Value
                val minutesText = String.format(Locale.getDefault(), "%02d", secondsRemaining / 60)
                val secondsText = String.format(Locale.getDefault(), "%02d", secondsRemaining % 60)
                
                Text(
                    text = "$minutesText:$secondsText",
                    color = if (timerState == TimerState.BREAK) NeonEmerald else ThemeWhite,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )

                // Subtitle showing current active vector
                Text(
                    text = selectedTask?.let { "TARGET VECTOR: ${it.title.uppercase()}" } ?: "NO ACTIVE TARGET TARGETED",
                    color = if (selectedTask?.priority == "HIGH") NeonRose else TextGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Chronometer Controller Actions Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (timerState == TimerState.RUNNING || timerState == TimerState.BREAK) {
                        // Pause Button
                        IconButton(
                            onClick = {
                                timerState = TimerState.PAUSED
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(NeonAmber.copy(alpha = 0.1f), CircleShape)
                                .border(BorderStroke(1.dp, NeonAmber), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = NeonAmber
                            )
                        }
                    } else {
                        // Play/Start Button
                        IconButton(
                            onClick = {
                                if (timerState == TimerState.PAUSED && secondsRemaining > 0) {
                                    timerState = TimerState.RUNNING
                                } else {
                                    timerState = TimerState.RUNNING
                                    secondsRemaining = workMinutes * 60
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(NeonCyan.copy(alpha = 0.15f), CircleShape)
                                .border(BorderStroke(1.5.dp, NeonCyan), CircleShape)
                                .testTag("start_timer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start",
                                tint = NeonCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Reset / Clear Button
                    IconButton(
                        onClick = {
                            timerState = TimerState.IDLE
                            secondsRemaining = workMinutes * 60
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(CyberSlateLight.copy(alpha = 0.3f), CircleShape)
                            .border(BorderStroke(1.dp, CyberSlateLight), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = ThemeWhite
                        )
                    }

                    // Skip / Force Complete Session Button
                    IconButton(
                        onClick = {
                            if (timerState == TimerState.RUNNING) {
                                // Simulate completion instantly
                                selectedTask?.let { task ->
                                    viewModel.addFocusMinutes(task.id, workMinutes.toLong())
                                }
                                timerState = TimerState.BREAK
                                secondsRemaining = breakMinutes * 60
                            } else if (timerState == TimerState.BREAK) {
                                timerState = TimerState.IDLE
                                secondsRemaining = workMinutes * 60
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(CyberSlateLight.copy(alpha = 0.3f), CircleShape)
                            .border(BorderStroke(1.dp, CyberSlateLight), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Skip",
                            tint = ThemeWhite
                        )
                    }
                }
            }
        }

        // --- SECTION 2: POLISHED INTERVAL CONFIGS ---
        if (timerState == TimerState.IDLE) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
                border = BorderStroke(1.dp, CyberSlateLight),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Work config
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "FOCUS WORK",
                            color = TextGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { if (workMinutes > 5) workMinutes -= 5 }) {
                                Icon(Icons.Default.Remove, contentDescription = "Less", tint = NeonCyan, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "$workMinutes MIN",
                                color = ThemeWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            IconButton(onClick = { if (workMinutes < 120) workMinutes += 5 }) {
                                Icon(Icons.Default.Add, contentDescription = "More", tint = NeonCyan, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(CyberSlateLight)
                    )

                    // Break config
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "REST CYCLE",
                            color = TextGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { if (breakMinutes > 1) breakMinutes -= 1 }) {
                                Icon(Icons.Default.Remove, contentDescription = "Less", tint = NeonEmerald, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "$breakMinutes MIN",
                                color = ThemeWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            IconButton(onClick = { if (breakMinutes < 30) breakMinutes += 1 }) {
                                Icon(Icons.Default.Add, contentDescription = "More", tint = NeonEmerald, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- SECTION 3: INTEGRATED TASK VECTOR MATRIX SELECTOR ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TARGET GUARDIAN VECTORS",
                color = ThemeWhite,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "${sortedTasks.size} ACTIVE",
                color = NeonCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (sortedTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CyberSlateMedium)
                    .border(BorderStroke(1.dp, CyberSlateLight), RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = TextGray.copy(alpha = 0.3f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "NO COMPROMISED GUARDIAN VECTORS",
                        color = ThemeWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Initialize active task objectives on the main vector control deck to deploy focus timers.",
                        color = TextGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(sortedTasks, key = { it.id }) { task ->
                    val isSelected = selectedTask?.id == task.id
                    val isHighPriority = task.priority == "HIGH"
                    
                    val cardBorderColor = if (isSelected) {
                        if (isHighPriority) NeonRose else NeonCyan
                    } else {
                        CyberSlateLight
                    }

                    val cardBgColor = if (isSelected) {
                        CyberSlateLight.copy(alpha = 0.15f)
                    } else {
                        CyberSlateMedium
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTask = task }
                            .testTag("pomodoro_task_item_${task.id}"),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        border = BorderStroke(1.dp, cardBorderColor),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Priority Icon Indicator
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isHighPriority) NeonRose.copy(alpha = 0.1f) else CyberSlateLight.copy(alpha = 0.3f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isHighPriority) Icons.Default.Warning else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Priority",
                                    tint = if (isHighPriority) NeonRose else TextGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    color = ThemeWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Focus Spent badge
                                    Text(
                                        text = "⏱ FOCUS: ${task.focusMinutesSpent} MIN",
                                        color = NeonCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    // Estimated Hours remaining
                                    Text(
                                        text = "EST: ${task.estimatedHours} HR",
                                        color = TextGray,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = if (isHighPriority) NeonRose else NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
