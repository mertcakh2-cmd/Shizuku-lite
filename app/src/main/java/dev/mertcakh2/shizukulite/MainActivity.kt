package dev.mertcakh2.shizukulite

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF8AB4FF),
                    secondary = Color(0xFF7DE1C3),
                    background = Color(0xFF0B1020),
                    surface = Color(0xFF111827)
                )
            ) {
                LiteDashboardApp()
            }
        }
    }
}

@Composable
fun LiteDashboardApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    var status by remember { mutableStateOf(ShizukuBridge.currentStatus()) }
    var command by remember { mutableStateOf("id && echo ShizukuLite") }
    var output by remember { mutableStateOf("Tap Run adb shell to test the remote shell.") }
    var isRunning by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val binderListener = Shizuku.OnBinderReceivedListener {
            status = ShizukuBridge.currentStatus()
        }
        val deadListener = Shizuku.OnBinderDeadListener {
            status = ShizukuBridge.currentStatus()
        }

        Shizuku.addBinderReceivedListener(binderListener)
        Shizuku.addBinderDeadListener(deadListener)

        onDispose {
            Shizuku.removeBinderReceivedListener(binderListener)
            Shizuku.removeBinderDeadListener(deadListener)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B1020),
                        Color(0xFF111827),
                        Color(0xFF172033)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Shizuku Lite",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Real Shizuku + ADB shell support with Material 3 styling.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFCBD5E1)
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Shizuku status",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF93C5FD)
                    )

                    Text(
                        text = status.label,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = status.detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    title = "Binder",
                    subtitle = if (status.binderReady) "Connected" else "Waiting",
                    accent = if (status.binderReady) Color(0xFF7DE1C3) else Color(0xFFFBBF24),
                    modifier = Modifier.weight(1f)
                )

                FeatureCard(
                    title = "Permission",
                    subtitle = if (status.permissionGranted) "Granted" else "Missing",
                    accent = if (status.permissionGranted) Color(0xFF8AB4FF) else Color(0xFFF87171),
                    modifier = Modifier.weight(1f)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ADB shell command",
                        color = Color(0xFFBFDBFE),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = command,
                        onValueChange = { command = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        label = { Text("command") }
                    )

                    Button(
                        onClick = {
                            if (!status.permissionGranted || !status.binderReady) {
                                ShizukuBridge.requestPermission()
                                Toast.makeText(context, "Requesting Shizuku permission", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            scope.launch {
                                isRunning = true
                                try {
                                    val result = withContext(Dispatchers.IO) { AdbShellHelper.execute(command) }
                                    output = if (result.success) {
                                        result.output.ifBlank { "Command executed successfully (exit ${result.exitCode})" }
                                    } else {
                                        "${result.output.ifBlank { "Command failed" }}\nExit: ${result.exitCode}"
                                    }
                                } catch (t: Throwable) {
                                    output = "Execution failed: ${t.message}"
                                    Log.e("ShizukuLite", "ADB shell failed", t)
                                } finally {
                                    isRunning = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isRunning
                    ) {
                        Text(text = if (isRunning) "Running..." else "Run adb shell")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Output",
                        color = Color(0xFF7DE1C3),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = output,
                        color = Color(0xFFF8FAFC),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (status.permissionGranted && status.binderReady) {
                Button(
                    onClick = {
                        activity?.let {
                            NotificationDecorator.postStatus(it, true, true)
                            Toast.makeText(context, "Status notification sent", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Send status notification")
                }
            } else {
                TextButton(
                    onClick = {
                        ShizukuBridge.requestPermission()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Enable Shizuku permission", color = Color(0xFFBFDBFE))
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material3.Surface(
                color = accent.copy(alpha = 0.18f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0)
            )
        }
    }
}

object ShizukuBridge {
    const val REQUEST_CODE = 1001

    data class Status(
        val binderReady: Boolean,
        val permissionGranted: Boolean,
        val uid: Int,
        val label: String,
        val detail: String
    )

    fun currentStatus(): Status {
        val binderReady = Shizuku.pingBinder()
        val permissionGranted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        val uid = if (permissionGranted && binderReady) {
            try {
                Shizuku.getUid()
            } catch (_: Throwable) {
                -1
            }
        } else {
            -1
        }

        val mode = when {
            uid == 0 -> "Root"
            uid == 2000 -> "ADB shell"
            uid > 0 -> "UID $uid"
            else -> "Unavailable"
        }

        val label = when {
            !binderReady -> "Waiting for binder"
            !permissionGranted -> "Permission missing"
            uid == 0 -> "Root access ready"
            uid == 2000 -> "ADB shell ready"
            uid > 0 -> "Shizuku connected"
            else -> "Shizuku available"
        }

        val detail = when {
            !binderReady -> "Shizuku binder is not connected yet. Open Shizuku and allow the app to bind."
            !permissionGranted -> "The app still needs the Shizuku permission before it can run shell commands."
            uid == 0 -> "Shizuku is running with root privileges."
            uid == 2000 -> "Shizuku is running under adb shell privileges."
            uid > 0 -> "Connected using a non-root UID. Some privileged operations may be unavailable."
            else -> "Permission granted but the UID could not be resolved."
        }

        return Status(binderReady, permissionGranted, uid, label, detail)
    }

    fun requestPermission() {
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(REQUEST_CODE)
        }
    }
}

object AdbShellHelper {
    data class ShellResult(
        val success: Boolean,
        val output: String,
        val exitCode: Int
    )

    fun execute(command: String): ShellResult {
        val normalized = command.trim()
        if (normalized.isEmpty()) {
            return ShellResult(false, "Command is empty", -1)
        }

        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED || !Shizuku.pingBinder()) {
            return ShellResult(false, "Shizuku permission or binder is unavailable", -1)
        }

        return try {
            val shizukuClass = Class.forName("rikka.shizuku.Shizuku")
            val method = shizukuClass.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(null, arrayOf("/system/bin/sh", "-c", normalized), null, null) as Process
            readProcess(process)
        } catch (t: Throwable) {
            val process = Runtime.getRuntime().exec(arrayOf("/system/bin/sh", "-c", normalized))
            readProcess(process)
        }
    }

    private fun readProcess(process: Process): ShellResult {
        val stdout = StringBuilder()
        val stderr = StringBuilder()

        val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
        val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

        try {
            var line: String?
            while (stdoutReader.readLine().also { line = it } != null) {
                stdout.append(line).append('\n')
            }
            while (stderrReader.readLine().also { line = it } != null) {
                stderr.append(line).append('\n')
            }
        } finally {
            stdoutReader.close()
            stderrReader.close()
        }

        val exitCode = process.waitFor()
        val combined = (stdout.toString() + stderr.toString()).trim()
        return ShellResult(exitCode == 0, combined, exitCode)
    }
}
