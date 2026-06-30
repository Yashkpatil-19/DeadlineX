package com.example.ui

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.CyberSlateMedium
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ThemeWhite

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "DEADLINEX WEB PORTAL",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = ThemeWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(NeonCyan.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ONLINE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = NeonCyan
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("web_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Native Mode",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSlateMedium,
                    titleContentColor = ThemeWhite
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF020617)) // Match slate-950 in Tailwind
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                return false // Handle navigation inside WebView itself
                            }
                        }
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true
                            databaseEnabled = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                        }
                        loadUrl("file:///android_asset/web/index.html")
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("webview_portal_content")
            )
        }
    }
}
