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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit
import com.example.data.JsonParser
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    viewModel: GuardianViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.allHabits.collectAsState()
    var nameInput by remember { mutableStateOf("") }
    var frequencyInput by remember { mutableStateOf("Daily") }

    // Generates the last 5 days' date strings to display checkable log blocks
    val days = remember {
        val list = mutableListOf<Date>()
        val cal = Calendar.getInstance()
        for (i in 0 until 5) {
            list.add(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        list.reverse()
        list
    }

    val dateFormats = remember {
        val sdfLabel = SimpleDateFormat("EEE", Locale.getDefault())
        val sdfValue = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        days.map { sdfLabel.format(it) to sdfValue.format(it) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Habits Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CyberSlateMedium)
                    .border(BorderStroke(1.dp, NeonPurple), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "AI ROUTINE PERSISTENCE ENGINE",
                        color = NeonPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MICRO-HABITS INTEGRITY ENGINE",
                        color = ThemeWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Solidify routines to buffer critical deadlines and secure stress buffers.",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Quick add habit form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
                border = BorderStroke(1.dp, CyberSlateLight),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ESTABLISH ROUTINE HABIT",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Habit Name (e.g., Code Study, No Gaming)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("habit_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberSlateLight
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Daily", "Weekly").forEach { freq ->
                                val isSel = frequencyInput == freq
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSel) NeonCyan.copy(alpha = 0.2f) else CyberSlateLight
                                        )
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                if (isSel) NeonCyan else Color.Transparent
                                            ),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { frequencyInput = freq }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = freq,
                                        color = if (isSel) ThemeWhite else TextGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.addHabit(nameInput, frequencyInput)
                                nameInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("add_habit_button")
                        ) {
                            Text(
                                text = "ESTABLISH",
                                color = CyberSlateDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Active listing header
        item {
            Text(
                text = "ESTABLISHED ROUTINES",
                color = ThemeWhite,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        if (habits.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = "Empty habits",
                            tint = TextGray.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No routines registered. Establish one above.",
                            color = TextGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(habits, key = { it.id }) { habit ->
                val loggedDates = JsonParser.fromJsonList(habit.logsJson, String::class.java)

                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSlateMedium),
                    border = BorderStroke(1.dp, CyberSlateLight),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_item_${habit.id}")
                        .animateItem()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = habit.name,
                                    color = ThemeWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = habit.frequency.uppercase(),
                                    color = NeonPurple,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteHabit(habit.id) },
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Grid of last 5 days
                        Text(
                            text = "COMPLETION LOG (TAP TO TOGGLE)",
                            color = TextGray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            dateFormats.forEach { (label, value) ->
                                val isLogged = loggedDates.contains(value)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = TextGray,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isLogged) NeonEmerald.copy(alpha = 0.2f) else CyberSlateLight
                                            )
                                            .border(
                                                BorderStroke(
                                                    1.dp,
                                                    if (isLogged) NeonEmerald else CyberSlateLight
                                                ),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { viewModel.toggleHabit(habit.id, value) }
                                            .testTag("habit_day_${label}_${habit.id}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isLogged) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Logged",
                                                tint = NeonEmerald,
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
        }
    }
}
