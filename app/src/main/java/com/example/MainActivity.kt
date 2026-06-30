package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.data.Task
import com.example.ui.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: GuardianViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        com.example.data.FirestoreSyncManager.initialize(applicationContext)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            MyApplicationTheme(darkTheme = isDarkTheme) {
                var isWebPortalMode by remember { mutableStateOf(false) }

                if (isWebPortalMode) {
                    WebViewScreen(onBack = { isWebPortalMode = false })
                } else {
                    var currentTab by remember { mutableStateOf(0) }
                    var selectedTaskForPlanner by remember { mutableStateOf<Task?>(null) }
                    var selectedTaskForEmergency by remember { mutableStateOf<Task?>(null) }

                val snackbarHostState = remember { SnackbarHostState() }
                val uiMessage by viewModel.uiMessage.collectAsState()

                // Trigger snackbar alerts on message updates
                LaunchedEffect(uiMessage) {
                    uiMessage?.let { msg ->
                        snackbarHostState.showSnackbar(
                            message = msg,
                            duration = SnackbarDuration.Short
                        )
                        viewModel.clearUiMessage()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Guardian",
                                        tint = if (isDarkTheme) NeonCyan else BentoIndigoBg,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "DEADLINEX",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            actions = {
                                IconButton(
                                    onClick = { isWebPortalMode = true },
                                    modifier = Modifier.testTag("web_portal_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "Switch to Web Portal",
                                        tint = if (isDarkTheme) NeonCyan else BentoIndigoBg
                                    )
                                }
                                IconButton(
                                    onClick = { isDarkTheme = !isDarkTheme },
                                    modifier = Modifier.testTag("theme_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Theme Mode",
                                        tint = if (isDarkTheme) NeonCyan else BentoIndigoBg
                                    )
                                }
                                // Status light
                                Box(
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .size(8.dp)
                                        .background(NeonEmerald, shape = MaterialTheme.shapes.small)
                                )
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp,
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Dashboard"
                                    )
                                },
                                label = { Text("Dashboard", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = if (isDarkTheme) NeonCyan else BentoIndigoBg,
                                    selectedTextColor = if (isDarkTheme) NeonCyan else BentoIndigoBg,
                                    unselectedIconColor = TextGray,
                                    unselectedTextColor = TextGray,
                                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.testTag("tab_dashboard")
                            )

                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.ListAlt,
                                        contentDescription = "AI Planner"
                                    )
                                },
                                label = { Text("AI Planner", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonPurple,
                                    selectedTextColor = NeonPurple,
                                    unselectedIconColor = TextGray,
                                    unselectedTextColor = TextGray,
                                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.testTag("tab_planner")
                            )

                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Focus Timer"
                                    )
                                },
                                label = { Text("Focus Timer", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    unselectedIconColor = TextGray,
                                    unselectedTextColor = TextGray,
                                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.testTag("tab_focus_timer")
                            )

                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { currentTab = 3 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Routines"
                                    )
                                },
                                label = { Text("Routines", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonAmber,
                                    selectedTextColor = NeonAmber,
                                    unselectedIconColor = TextGray,
                                    unselectedTextColor = TextGray,
                                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.testTag("tab_habits")
                            )

                            NavigationBarItem(
                                selected = currentTab == 4,
                                onClick = { currentTab = 4 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "History"
                                    )
                                },
                                label = { Text("History", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    unselectedIconColor = TextGray,
                                    unselectedTextColor = TextGray,
                                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.testTag("tab_history")
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    val criticalAlert by viewModel.criticalRiskAlert.collectAsState()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                                        fadeOut(animationSpec = tween(90))
                            },
                            label = "TabTransition"
                        ) { targetTab ->
                            when (targetTab) {
                                0 -> DashboardScreen(
                                    viewModel = viewModel,
                                    onTaskSelected = { task ->
                                        selectedTaskForPlanner = task
                                        currentTab = 1 // Go to Planner
                                    }
                                )
                                1 -> AIPlannerScreen(
                                    viewModel = viewModel,
                                    initialSelectedTask = selectedTaskForPlanner,
                                    onTriggerEmergency = { task ->
                                        currentTab = 2 // Go to Focus Timer
                                    }
                                )
                                2 -> FocusTimerScreen(
                                    viewModel = viewModel
                                )
                                3 -> HabitsScreen(
                                    viewModel = viewModel
                                )
                                4 -> HistoryScreen(
                                    viewModel = viewModel
                                )
                            }
                        }

                        // Overlay Critical Risk Warning Banner
                        criticalAlert?.let { alertTask ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .align(Alignment.TopCenter)
                                    .background(CyberSlateMedium, shape = RoundedCornerShape(16.dp))
                                    .border(BorderStroke(2.dp, NeonRose), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                                    .testTag("critical_risk_banner")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Risk Alert",
                                        tint = NeonRose,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "🚨 CRITICAL DEADLINE RISK",
                                            color = NeonRose,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = alertTask.title,
                                            color = ThemeWhite,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Completion probability dropped to ${alertTask.completionProbability}%. Emergency action needed!",
                                            color = TextGray,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    selectedTaskForEmergency = alertTask
                                                    currentTab = 2 // Go to Overdrive
                                                    viewModel.clearCriticalRiskAlert()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = NeonRose),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.height(32.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("ACTIVATE RESCUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            }
                                            OutlinedButton(
                                                onClick = { viewModel.clearCriticalRiskAlert() },
                                                border = BorderStroke(1.dp, CyberSlateLight),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.height(32.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("DISMISS", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
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
    }
}
