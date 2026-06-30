package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.data.SubTask
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: GuardianViewModel,
    onTaskSelected: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsState()
    val activeTasks = remember(tasks) { tasks.filter { it.status != "COMPLETED" } }
    val habits by viewModel.allHabits.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showAddTaskForm by remember { mutableStateOf(false) }

    // Form inputs
    var titleInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") } // Added description state
    var priorityInput by remember { mutableStateOf("AUTO") } // Added priority state (AUTO, HIGH, MEDIUM, LOW)
    var categoryInput by remember { mutableStateOf("Assignment") }
    var difficultyInput by remember { mutableFloatStateOf(3f) }
    var importanceInput by remember { mutableFloatStateOf(3f) }
    var hoursInput by remember { mutableStateOf("") }
    var daysInput by remember { mutableStateOf("") }

    // Manual Roadmap State
    var roadmapType by remember { mutableStateOf("AI") } // "AI" or "MANUAL"
    val manualSubtasks = remember { mutableStateListOf<SubTask>() }
    var manualSubtaskTitle by remember { mutableStateOf("") }
    var manualSubtaskDuration by remember { mutableStateOf("") }
    var manualSubtaskScheduledTime by remember { mutableStateOf("") }

    val categories = listOf("Assignment", "Exam", "Interview", "Project", "Bill payment", "Lab", "Presentation", "Other")
    val categoryRowState = rememberLazyListState()

    LaunchedEffect(categoryInput) {
        val index = categories.indexOf(categoryInput)
        if (index >= 0) {
            categoryRowState.animateScrollToItem(index)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // --- BENTO GRID HEADER SECTION ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DEADLINEX",
                        color = NeonEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Dashboard",
                        color = ThemeWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (activeTasks.isNotEmpty()) {
                                activeTasks.forEach { viewModel.forceRiskRecalculation(it.id) }
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync",
                            tint = NeonEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // AI Active Indicator Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(NeonEmerald.copy(alpha = 0.1f))
                            .border(BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.3f)), RoundedCornerShape(100.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(NeonEmerald)
                            )
                            Text(
                                text = "AI ACTIVE",
                                color = NeonEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // --- BENTO GRID COMPONENT 1: PRIMARY CURRENT FOCUS CARD ---
        item {
            val currentFocusTask = activeTasks.minByOrNull { (it.deadline - System.currentTimeMillis()) }
            
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
                border = BorderStroke(1.dp, CyberSlateLight),
                shape = RoundedCornerShape(24.dp), // Bento Rounded Corners
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { currentFocusTask?.let { onTaskSelected(it) } }
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeonEmerald.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "CURRENT FOCUS",
                                    color = NeonEmerald,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (currentFocusTask != null) {
                                val focusPriorityColor = when (currentFocusTask.priority) {
                                    "HIGH" -> NeonRose
                                    "MEDIUM" -> NeonAmber
                                    else -> NeonEmerald
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(focusPriorityColor.copy(alpha = 0.15f))
                                        .border(BorderStroke(1.dp, focusPriorityColor.copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(focusPriorityColor)
                                        )
                                        Text(
                                            text = currentFocusTask.priority,
                                            color = focusPriorityColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = if (currentFocusTask != null) "#DG-${currentFocusTask.id.toString().takeLast(3).uppercase()}" else "#DG-000",
                            color = TextGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = currentFocusTask?.title ?: "Initialize First Sector",
                        color = ThemeWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val focusDaysLeft = currentFocusTask?.let { ((it.deadline - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt() } ?: 0
                    val focusSubtitleText = when {
                        currentFocusTask == null -> "No vectors deployed in shield grid"
                        focusDaysLeft <= 0 -> "Deadline exceeded / Action required"
                        else -> "Final submission required in $focusDaysLeft days"
                    }

                    Text(
                        text = focusSubtitleText,
                        color = TextGray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val focusPercent = currentFocusTask?.completionPercentage?.toFloat() ?: 0f
                    LinearProgressIndicator(
                        progress = { focusPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonEmerald,
                        trackColor = CyberSlateLight
                    )
                }
            }
        }

        // --- BENTO GRID COMPONENT 2: RISK LEVEL & PRODUCTIVITY SCORE SIDE-BY-SIDE ---
        item {
            val highRiskCount = activeTasks.count { it.riskLevel == "HIGH" }
            val hasHighRisk = highRiskCount > 0
            val riskCardBg = if (hasHighRisk) NeonRose.copy(alpha = 0.08f) else NeonEmerald.copy(alpha = 0.08f)
            val riskCardBorder = if (hasHighRisk) NeonRose.copy(alpha = 0.25f) else NeonEmerald.copy(alpha = 0.25f)
            val riskLabelColor = if (hasHighRisk) NeonRose else NeonEmerald
            val riskTitle = if (hasHighRisk) "HIGH" else "SECURE"
            
            val delayProb = if (activeTasks.isNotEmpty()) activeTasks.map { 100 - it.completionProbability }.maxOrNull() ?: 0 else 0
            val riskDesc = if (hasHighRisk) "AI predicting $delayProb% delay probability" else "All vectors running on schedule"

            // Compute Focus Score
            val focusScore = if (activeTasks.isNotEmpty()) {
                (activeTasks.map { it.completionProbability }.average() * 0.85 + (habits.size * 3).coerceAtMost(15)).toInt().coerceIn(0, 100)
            } else {
                100
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Risk Level Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = riskCardBg),
                    border = BorderStroke(1.dp, riskCardBorder),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = if (hasHighRisk) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = "Risk State",
                            tint = riskLabelColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "RISK LEVEL",
                                color = riskLabelColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = riskTitle,
                                color = riskLabelColor,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = riskDesc,
                            color = riskLabelColor.copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Card 2: Focus Score Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = NeonAmber.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "FOCUS SCORE",
                            color = NeonAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$focusScore",
                                color = NeonAmber,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "/100",
                                color = NeonAmber.copy(alpha = 0.4f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        // Small focus visualizer segment line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(4) { idx ->
                                val active = focusScore >= (idx + 1) * 25
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (active) NeonAmber else NeonAmber.copy(alpha = 0.15f)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- BENTO GRID COMPONENT 3: AI GUARDIAN INTEL SUGGESTION ---
        item {
            val latestTaskWithSuggestions = activeTasks.firstOrNull { it.riskSuggestionsJson.isNotEmpty() }
            val latestSuggestion = if (latestTaskWithSuggestions != null) {
                val suggestionsList = com.example.data.JsonParser.fromJsonList(latestTaskWithSuggestions.riskSuggestionsJson, String::class.java)
                suggestionsList.firstOrNull() ?: "AI analyzing deadline vector... Work schedule secure."
            } else {
                "No deadline alert active. Clear outstanding routines to maintain peak stress safety reserves."
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = BentoIndigoBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Robot Intel",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GUARDIAN INTEL",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "\"$latestSuggestion\"",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Step 3: Fast Task Creator Toggle
        item {
            Button(
                onClick = { showAddTaskForm = !showAddTaskForm },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("toggle_add_task_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showAddTaskForm) CyberSlateLight else CyberSlateMedium
                ),
                border = BorderStroke(1.dp, NeonCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (showAddTaskForm) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Toggle add",
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showAddTaskForm) "COLLAPSE CREATOR" else "DEPLOY NEW TASK GUARDIAN",
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Form expandable block
        item {
            AnimatedVisibility(
                visible = showAddTaskForm,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
                    border = BorderStroke(1.dp, CyberSlateLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "INITIALIZE AUTONOMOUS AGENT",
                            color = NeonPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = { Text("Task Name / Objective") },
                            modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("task_title_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberSlateLight
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Description Field
                        OutlinedTextField(
                            value = descriptionInput,
                            onValueChange = { descriptionInput = it },
                            label = { Text("Task Description / Details") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("task_description_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberSlateLight
                            ),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Priority Selector
                        Text(
                            text = "Priority Level",
                            color = TextSilver,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val priorities = listOf("AUTO", "HIGH", "MEDIUM", "LOW")
                            priorities.forEach { prio ->
                                val isSelected = priorityInput == prio
                                val borderColor = when (prio) {
                                    "AUTO" -> NeonCyan
                                    "HIGH" -> NeonRose
                                    "MEDIUM" -> NeonAmber
                                    else -> NeonEmerald
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) borderColor.copy(alpha = 0.2f)
                                            else CyberSlateLight
                                        )
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                if (isSelected) borderColor else Color.Transparent
                                            ),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { priorityInput = prio }
                                        .padding(vertical = 8.dp)
                                        .testTag("priority_btn_$prio"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = prio,
                                        color = if (isSelected) borderColor else TextGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Dropdown
                        Text(
                            text = "Category: $categoryInput",
                            color = TextSilver,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(
                            state = categoryRowState,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            items(categories) { cat ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (categoryInput == cat) NeonPurple.copy(alpha = 0.3f)
                                            else CyberSlateLight
                                        )
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                if (categoryInput == cat) NeonPurple else Color.Transparent
                                            ),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { categoryInput = cat }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (categoryInput == cat) ThemeWhite else TextGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Difficulty rating slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Difficulty Rating (1-5)",
                                color = TextSilver,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${difficultyInput.toInt()}/5",
                                color = NeonAmber,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = difficultyInput,
                            onValueChange = { difficultyInput = it },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = CyberSlateLight
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Importance Level slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Importance Level (1-5)",
                                color = TextSilver,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${importanceInput.toInt()}/5",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = importanceInput,
                            onValueChange = { importanceInput = it },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonPurple,
                                activeTrackColor = NeonPurple,
                                inactiveTrackColor = CyberSlateLight
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = hoursInput,
                                onValueChange = { hoursInput = it },
                                label = { Text("Est. Hours") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("task_hours_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = CyberSlateLight
                                )
                            )

                            val context = LocalContext.current
                            val calendar = Calendar.getInstance()
                            val datePickerDialog = remember {
                                android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val selectedCal = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, year)
                                            set(Calendar.MONTH, month)
                                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                            set(Calendar.HOUR_OF_DAY, 23)
                                            set(Calendar.MINUTE, 59)
                                        }
                                        val diffMs = selectedCal.timeInMillis - System.currentTimeMillis()
                                        val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                                        daysInput = diffDays.toString()
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                )
                            }

                            OutlinedTextField(
                                value = daysInput,
                                onValueChange = { daysInput = it },
                                label = { Text("Days Left") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = {
                                    IconButton(
                                        onClick = { datePickerDialog.show() },
                                        modifier = Modifier.testTag("select_due_date_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Select due date from calendar",
                                            tint = NeonCyan
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("task_days_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = CyberSlateLight
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ROADMAP TYPE SELECTOR
                        Text(
                            text = "Roadmap Creation Option",
                            color = TextSilver,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // AI OPTION
                            val isAi = roadmapType == "AI"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isAi) NeonCyan.copy(alpha = 0.15f)
                                        else CyberSlateLight
                                    )
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (isAi) NeonCyan else Color.Transparent
                                        ),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { roadmapType = "AI" }
                                    .padding(vertical = 10.dp)
                                    .testTag("roadmap_type_AI"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = if (isAi) NeonCyan else TextGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "AI GENERATED",
                                        color = if (isAi) NeonCyan else TextGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // MANUAL OPTION
                            val isManual = roadmapType == "MANUAL"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isManual) NeonEmerald.copy(alpha = 0.15f)
                                        else CyberSlateLight
                                    )
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (isManual) NeonEmerald else Color.Transparent
                                        ),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { roadmapType = "MANUAL" }
                                    .padding(vertical = 10.dp)
                                    .testTag("roadmap_type_MANUAL"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = if (isManual) NeonEmerald else TextGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "MANUAL ROADMAP",
                                        color = if (isManual) NeonEmerald else TextGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (roadmapType == "AI") {
                            Text(
                                text = "DeadlineX will automatically analyze your inputs and break down the task into an optimized day-by-day subtask roadmap.",
                                color = TextSilver.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        } else {
                            // MANUAL SUBTASKS BUILDER
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CyberSlateLight.copy(alpha = 0.5f)),
                                border = BorderStroke(1.dp, CyberSlateLight),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "MANUAL SUBTASKS BUILDER",
                                        color = NeonEmerald,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = manualSubtaskTitle,
                                        onValueChange = { manualSubtaskTitle = it },
                                        label = { Text("Subtask Title") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("manual_subtask_title"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonEmerald,
                                            unfocusedBorderColor = CyberSlateLight
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = manualSubtaskDuration,
                                            onValueChange = { manualSubtaskDuration = it },
                                            label = { Text("Duration (min)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("manual_subtask_duration"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = NeonEmerald,
                                                unfocusedBorderColor = CyberSlateLight
                                            )
                                        )

                                        OutlinedTextField(
                                            value = manualSubtaskScheduledTime,
                                            onValueChange = { manualSubtaskScheduledTime = it },
                                            label = { Text("Schedule (e.g. Day 1)") },
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .testTag("manual_subtask_schedule"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = NeonEmerald,
                                                unfocusedBorderColor = CyberSlateLight
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            if (manualSubtaskTitle.isNotBlank()) {
                                                val dur = manualSubtaskDuration.toIntOrNull() ?: 30
                                                val sched = manualSubtaskScheduledTime.ifBlank { "Day 1" }
                                                manualSubtasks.add(
                                                    SubTask(
                                                        title = manualSubtaskTitle,
                                                        durationMinutes = dur,
                                                        status = "PENDING",
                                                        scheduledTime = sched
                                                    )
                                                )
                                                // Clear inputs
                                                manualSubtaskTitle = ""
                                                manualSubtaskDuration = ""
                                                manualSubtaskScheduledTime = ""
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("add_manual_subtask_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = CyberSlateDark,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ADD SUBTASK",
                                            color = CyberSlateDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    if (manualSubtasks.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        HorizontalDivider(color = CyberSlateLight)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Roadmap Items (${manualSubtasks.size}):",
                                            color = TextSilver,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            manualSubtasks.forEachIndexed { idx, sub ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(CyberSlateLight.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = sub.title,
                                                            color = ThemeWhite,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        Text(
                                                            text = "${sub.durationMinutes} min • ${sub.scheduledTime}",
                                                            color = TextSilver.copy(alpha = 0.7f),
                                                            fontSize = 10.sp
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { manualSubtasks.removeAt(idx) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Remove subtask",
                                                            tint = NeonRose,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isRefreshing) {
                            CircularProgressIndicator(
                                color = NeonCyan,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            Button(
                                onClick = {
                                    val hours = hoursInput.toDoubleOrNull() ?: 5.0
                                    val days = daysInput.toIntOrNull() ?: 3
                                    val subtasksToPass = if (roadmapType == "MANUAL") manualSubtasks.toList() else null

                                    viewModel.addTask(
                                        title = titleInput,
                                        description = descriptionInput,
                                        category = categoryInput,
                                        difficulty = difficultyInput.toInt(),
                                        estimatedHours = hours,
                                        daysRemaining = days,
                                        userPriority = priorityInput,
                                        importanceLevel = importanceInput.toInt(),
                                        manualSubtasks = subtasksToPass
                                    )

                                    // Reset inputs
                                    titleInput = ""
                                    descriptionInput = ""
                                    priorityInput = "AUTO"
                                    hoursInput = ""
                                    daysInput = ""
                                    roadmapType = "AI"
                                    manualSubtasks.clear()
                                    difficultyInput = 3f
                                    importanceInput = 3f
                                    manualSubtaskTitle = ""
                                    manualSubtaskDuration = ""
                                    manualSubtaskScheduledTime = ""
                                    showAddTaskForm = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("deploy_guardian_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "DEPLOY ACTIVE GUARDIAN",
                                    color = CyberSlateDark,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Step 4: Top AI Priorities Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE DEADLINE DEFENDERS",
                    color = ThemeWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${activeTasks.size} Active",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    if (tasks.isNotEmpty()) {
                        Text(
                            text = "CLEAR ALL",
                            color = NeonRose,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .clickable { viewModel.clearAllTasks() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("clear_all_tasks_button")
                        )
                    }
                }
            }
        }

        if (activeTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Empty",
                            tint = TextGray.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No active defense sectors. Add a task above.",
                            color = TextGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(activeTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onClick = { onTaskSelected(task) },
                    onDelete = { viewModel.deleteTask(task.id) },
                    onToggleComplete = { viewModel.toggleTask(task.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleComplete: (() -> Unit)? = null
) {
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

    val daysLeft = ((task.deadline - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
    val daysText = if (daysLeft <= 0) "OVERDUE" else "$daysLeft Days Left"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("task_item_${task.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
        border = BorderStroke(1.dp, CyberSlateLight),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category & Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "Category",
                        tint = NeonPurple,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.category.uppercase(),
                        color = NeonPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

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

            Spacer(modifier = Modifier.height(10.dp))

            // Task Title Row with Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (onToggleComplete != null) {
                    IconButton(
                        onClick = onToggleComplete,
                        modifier = Modifier.size(24.dp).testTag("task_check_${task.id}")
                    ) {
                        Icon(
                            imageVector = if (task.status == "COMPLETED") Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Complete Task",
                            tint = if (task.status == "COMPLETED") NeonEmerald else TextGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = task.title,
                    color = if (task.status == "COMPLETED") TextGray else ThemeWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            // Task Description (if present)
            if (task.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    color = ThemeWhite.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // AI Reasoning
            if (task.reasoning.isNotEmpty()) {
                Text(
                    text = task.reasoning,
                    color = TextGray,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PROGRESS: ${task.completionPercentage}%",
                    color = TextSilver,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "PROBABILITY: ${task.completionProbability}%",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { task.completionPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NeonCyan,
                trackColor = CyberSlateLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Days Left, Difficulty, and Delete Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Days Left",
                        tint = if (daysLeft <= 2) NeonRose else TextGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = daysText,
                        color = if (daysLeft <= 2) NeonRose else TextSilver,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "DIFF: ${task.difficulty}/5",
                        color = NeonAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "IMP: ${task.importanceLevel}/5",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = TextGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
