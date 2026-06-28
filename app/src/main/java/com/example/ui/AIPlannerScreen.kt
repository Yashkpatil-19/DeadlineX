package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.JsonParser
import com.example.data.SubTask
import com.example.data.Task
import com.example.data.EmergencyPlan
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIPlannerScreen(
    viewModel: GuardianViewModel,
    initialSelectedTask: Task?,
    onTriggerEmergency: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsState()
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    // Sync state with selected or list updates
    LaunchedEffect(tasks, initialSelectedTask) {
        if (selectedTask == null && tasks.isNotEmpty()) {
            selectedTask = initialSelectedTask ?: tasks.first()
        } else if (selectedTask != null) {
            selectedTask = tasks.firstOrNull { it.id == selectedTask!!.id }
        }
    }

    val tasksRowState = rememberLazyListState()
    var selectedSubTab by remember { mutableStateOf(0) }
    var hoursLeftInput by remember { mutableStateOf("8") }
    var blink by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(800)
            blink = !blink
        }
    }

    LaunchedEffect(selectedTask) {
        selectedSubTab = 0
        selectedTask?.let { task ->
            val index = tasks.indexOfFirst { it.id == task.id }
            if (index >= 0) {
                tasksRowState.animateScrollToItem(index)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Step 1: Horizontal Selector of Tasks
        if (tasks.isNotEmpty()) {
            Text(
                text = "SELECT WORK VECTOR",
                color = TextGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                state = tasksRowState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tasks, key = { it.id }) { task ->
                    val isSelected = selectedTask?.id == task.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) NeonPurple.copy(alpha = 0.25f) else CyberSlateMedium
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isSelected) NeonPurple else CyberSlateLight
                                ),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedTask = task }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = task.title,
                            color = if (isSelected) ThemeWhite else TextGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 120.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Selected Task Roadmap Detail View
        if (selectedTask != null) {
            val task = selectedTask!!
            val subtasks = JsonParser.fromJsonList(task.subtasksJson, SubTask::class.java)
            val suggestions = JsonParser.fromJsonList(task.riskSuggestionsJson, String::class.java)

            val riskColor = when (task.riskLevel) {
                "HIGH" -> NeonRose
                "MEDIUM" -> NeonAmber
                else -> NeonEmerald
            }

            val priorityColor = when (task.priority) {
                "HIGH" -> NeonRose
                "MEDIUM" -> NeonAmber
                else -> NeonEmerald
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Task Card Summary and AI Recommendation
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
                        border = BorderStroke(1.dp, CyberSlateLight),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI ANALYTICS ENGINE",
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Priority Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(priorityColor.copy(alpha = 0.15f))
                                            .border(BorderStroke(1.dp, priorityColor.copy(alpha = 0.6f)), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(priorityColor)
                                            )
                                            Text(
                                                text = "PRIORITY ${task.priority}",
                                                color = priorityColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }

                                    // Risk Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(riskColor.copy(alpha = 0.15f))
                                            .border(BorderStroke(1.dp, riskColor.copy(alpha = 0.6f)), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(riskColor)
                                            )
                                            Text(
                                                text = "RISK: ${task.riskLevel}",
                                                color = riskColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = task.title,
                                color = ThemeWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "COMPLETION CHANCE", color = TextGray, fontSize = 10.sp)
                                    Text(
                                        text = "${task.completionProbability}%",
                                        color = if (task.completionProbability < 50) NeonRose else NeonCyan,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "PROGRESS VECTOR", color = TextGray, fontSize = 10.sp)
                                    Text(
                                        text = "${task.completionPercentage}%",
                                        color = ThemeWhite,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            if (suggestions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = CyberSlateLight)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "GUARDIAN DIRECTIVES:",
                                    color = TextSilver,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                suggestions.forEach { suggestion ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Adjust,
                                            contentDescription = "Bullet",
                                            tint = riskColor,
                                            modifier = Modifier
                                                .padding(top = 3.dp)
                                                .size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = suggestion,
                                            color = TextSilver,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Inner detail tabs
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ROADMAP & MILESTONES", "EMERGENCY OVERDRIVE").forEachIndexed { index, title ->
                            val isSelected = selectedSubTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) {
                                            if (index == 1) NeonRose.copy(alpha = 0.2f) else NeonPurple.copy(alpha = 0.2f)
                                        } else {
                                            CyberSlateMedium
                                        }
                                    )
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (isSelected) {
                                                if (index == 1) NeonRose else NeonPurple
                                            } else {
                                                CyberSlateLight
                                            }
                                        ),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedSubTab = index }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    color = if (isSelected) ThemeWhite else TextGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                if (selectedSubTab == 0) {
                    // --- SUB-TAB 0: ROADMAP & DIRECTIVES ---
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AI DAY-BY-DAY ROADMAP",
                                color = ThemeWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${subtasks.count { it.status == "COMPLETED" }}/${subtasks.size} Done",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (subtasks.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
                                border = BorderStroke(1.dp, CyberSlateLight)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = NeonCyan)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Synthesizing strategic breakdown milestones...",
                                            color = TextGray,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(subtasks, key = { it.title }) { subtask ->
                            val isDone = subtask.status == "COMPLETED"

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleSubTask(task.id, subtask.title) }
                                    .testTag("subtask_item_${subtask.title}")
                                    .animateItem(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDone) CyberSlateMedium.copy(alpha = 0.5f) else CyberSlateMedium
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isDone) NeonEmerald.copy(alpha = 0.3f) else CyberSlateLight
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Check state",
                                        tint = if (isDone) NeonEmerald else TextGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = subtask.title,
                                            color = if (isDone) TextGray else ThemeWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = subtask.scheduledTime,
                                                color = NeonPurple,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "⏱ ${subtask.durationMinutes} MIN",
                                                color = TextGray,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Trigger emergency button switches to tab
                    if (task.status != "COMPLETED") {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { selectedSubTab = 1 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("emergency_trigger_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonRose),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Emergency",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ACTIVATE EMERGENCY RESCUE OVERDRIVE",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else {
                    // --- SUB-TAB 1: EMERGENCY RESCUE OVERDRIVE ---
                    val plan = JsonParser.fromJson<EmergencyPlan>(task.emergencyScheduleJson)

                    if (plan != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
                                border = BorderStroke(1.dp, NeonRose),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Alert",
                                            tint = if (blink) NeonRose else Color.Transparent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "🚨 EMERGENCY OVERDRIVE DEPLOYED 🚨",
                                            color = NeonRose,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "TARGET CRISIS: ${task.title.uppercase()}",
                                        color = ThemeWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "CRITICAL LIMIT: 08:00:00 HOURS REMAINING",
                                        color = TextSilver,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
                                border = BorderStroke(1.dp, NeonRose.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "IMMEDIATE COMMITS TO CANCEL (TEMPORARY ELIMINATION)",
                                        color = NeonRose,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (plan.activitiesToCancel.isEmpty()) {
                                        Text(
                                            text = "No specified distractions. Unplug all devices.",
                                            color = TextSilver,
                                            fontSize = 12.sp
                                        )
                                    } else {
                                        plan.activitiesToCancel.forEach { activity ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Cancel,
                                                    contentDescription = "Cancel",
                                                    tint = NeonRose,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = activity,
                                                    color = TextSilver,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "STEPS TO SURVIVAL // HOUR-BY-HOUR TIMELINE",
                                color = ThemeWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }

                        if (plan.rescueSchedule.isEmpty()) {
                            item {
                                Text(
                                    text = "Emergency sequence contains no steps.",
                                    color = TextGray,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            items(plan.rescueSchedule, key = { it.timeSlot }) { slot ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
                                    border = BorderStroke(1.dp, CyberSlateLight),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItem()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(NeonRose.copy(alpha = 0.2f))
                                                .border(BorderStroke(0.5.dp, NeonRose), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = slot.timeSlot,
                                                color = NeonRose,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = slot.activity,
                                            color = TextSilver,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    viewModel.forceRiskRecalculation(task.id)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, TextGray.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray)
                            ) {
                                Text(
                                    text = "STAND DOWN // DEACTIVATE CRISIS MODE",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(CyberSlateMedium)
                                    .border(BorderStroke(1.dp, NeonRose.copy(alpha = 0.3f)), RoundedCornerShape(24.dp))
                                    .padding(24.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(36.dp))
                                            .background(NeonRose.copy(alpha = 0.1f))
                                            .border(BorderStroke(1.dp, NeonRose), RoundedCornerShape(36.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FlashOff,
                                            contentDescription = "Secure",
                                            tint = NeonRose,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "EMERGENCY ENGAGEMENT MATRIX",
                                        color = ThemeWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "If this deadline is entering a critical crisis, activate Overdrive. The AI will immediately discard secondary objectives and generate a high-intensity schedule.",
                                        color = TextGray,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    OutlinedTextField(
                                        value = hoursLeftInput,
                                        onValueChange = { hoursLeftInput = it },
                                        label = { Text("Hours left until deadline") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("emergency_hours_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonRose,
                                            unfocusedBorderColor = CyberSlateLight
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            val hours = hoursLeftInput.toIntOrNull() ?: 8
                                            viewModel.triggerRescue(task.id, hours)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("execute_rescue_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonRose)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = "Bolt",
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "TRIGGER CRISIS SEQUENCE NOW",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Placeholder view
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "No active vectors",
                        tint = TextGray.copy(alpha = 0.2f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "NO VECTOR TARGETED",
                        color = ThemeWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "To generate an AI roadmap, please return to the Dashboard and deploy an active task guardian.",
                        color = TextGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
