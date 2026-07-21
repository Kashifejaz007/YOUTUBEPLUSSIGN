package com.example

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.White
                ) { innerPadding ->
                    YouTubeLauncherScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun YouTubeLauncherScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    // Live state tracking the enabled status of the YouTube Clicker Accessibility Service
    var isServiceEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    
    // Sync state on resume whenever the user navigates back from system settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isServiceEnabled = isAccessibilityServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .background(Color.White)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "YouTube Launcher",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A), // Slate 900
                letterSpacing = (-0.5).sp
            )
        }

        // Main Expressive Layout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // Expressive Typography Header (Bold Typography Theme)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = "STAY",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A), // Slate 900
                    lineHeight = 40.sp,
                    letterSpacing = (-1.5).sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "CONNECTED.",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF0000), // Vibrant Red
                    lineHeight = 40.sp,
                    letterSpacing = (-1.5).sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "OFFICIAL CHANNEL ACCESS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B), // Slate 500
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Primary Call to Action & Channel Badge
            Column(
                modifier = Modifier.widthIn(max = 320.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Red Visit Button with Bold Typography styling
                Button(
                    onClick = {
                        // Mark the launched flag so the service triggers once YouTube loads
                        YouTubeClickService.markAppLaunched()
                        launchYouTubeChannel(context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF0000),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 2.dp
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("visit_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "▶",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "VISIT OUR YOUTUBE CHANNEL",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Accessibility helper panel based on active service state
                if (isServiceEnabled) {
                    // Service is active status badge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0FDF4), shape = RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFBBF7D0), shape = RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active Icon",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Auto-Clicker Active: The app will automatically click the '+' button inside YouTube on launch.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF15803D),
                            lineHeight = 15.sp
                        )
                    }
                } else {
                    // Service is inactive setup action card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEFF6FF), shape = RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFBFDBFE), shape = RoundedCornerShape(16.dp))
                            .clickable {
                                try {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                    Toast.makeText(context, "Please find and enable '${context.getString(R.string.app_name)}'", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open settings.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info Icon",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Auto-Clicker Feature Available",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enable the launcher service in Settings to automatically click the Create (+) button inside YouTube. Tap here to set up.",
                            fontSize = 11.sp,
                            color = Color(0xFF1E3A8A),
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current Channel Information Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "CURRENT CHANNEL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8), // Slate 400
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "@GoogleNews-jamesbond",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569) // Slate 600
                        )
                    }
                }
            }
        }

        // Bottom Info Footer with Separator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            Spacer(
                modifier = Modifier
                    .width(48.dp)
                    .height(1.dp)
                    .background(Color(0xFFE2E8F0)) // Slate 200
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SECURE ANDROID LINK • V1.0.4\nOFFICIAL MEDIA PORTAL",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8), // Slate 400
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

/**
 * Checks if our custom Accessibility Service is currently enabled in settings.
 */
fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedComponentName = ComponentName(context, YouTubeClickService::class.java)
    val enabledServicesSetting = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServicesSetting)
    while (colonSplitter.hasNext()) {
        val componentNameString = colonSplitter.next()
        val enabledService = ComponentName.unflattenFromString(componentNameString)
        if (enabledService != null && enabledService == expectedComponentName) {
            return true
        }
    }
    return false
}

/**
 * Executes the YouTube channel launch intent logic with robust fallback options.
 */
fun launchYouTubeChannel(context: Context) {
    val channelUrl = "https://www.youtube.com/@GoogleNews-jamesbond"
    val deepLinkUri = "vnd.youtube://www.youtube.com/@GoogleNews-jamesbond"

    // Primary attempt: launch the vnd.youtube:// deep link scheme (handles official app redirection)
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUri)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return
    } catch (e: Exception) {
        // Proceed to fallback
    }

    // Secondary attempt: launch via package-specific intent to guarantee official app if registered
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(channelUrl)).apply {
            setPackage("com.google.android.youtube")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return
    } catch (e: Exception) {
        // Proceed to fallback
    }

    // Tertiary attempt: fallback to general web URL (automatically uses browser or default system handler)
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(channelUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Exception-free fallback Toast
        Toast.makeText(context, "Could not open channel link.", Toast.LENGTH_LONG).show()
    }
}

@Preview(showBackground = true)
@Composable
fun YouTubeLauncherScreenPreview() {
    MyApplicationTheme {
        YouTubeLauncherScreen()
    }
}
