package dev.mertcakh2.shizukulite

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.InvocationTargetException
import kotlin.concurrent.thread

private const val TAG = "ShizukuLite"

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

    var status by remember {
        mutableStateOf(ShizukuBridge.currentStatus())
    }

    var command by remember {
        mutableStateOf("id && echo ShizukuLite")
    }

    var output by remember {
        mutableStateOf("Ready. Enter a shell command.")
    }

    var isRunning by remember {
        mutableStateOf(false)
    }

    fun refreshStatus() {
        status = ShizukuBridge.currentStatus()
    }

    DisposableEffect(Unit) {

        val binderReceived =
            Shizuku.OnBinderReceivedListener {
                refreshStatus()
            }

        val binderDead =
            Shizuku.OnBinderDeadListener {
                refreshStatus()
            }

        val permissionResult =
            Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->

                if (requestCode == ShizukuBridge.REQUEST_CODE) {
                    refreshStatus()

                    output = if (
                        grantResult == PackageManager.PERMISSION_GRANTED
                    ) {
                        "Shizuku permission granted successfully."
                    } else {
                        "Shizuku permission denied."
                    }
                }
            }

        Shizuku.addBinderReceivedListener(binderReceived)
        Shizuku.addBinderDeadListener(binderDead)
        Shizuku.addRequestPermissionResultListener(permissionResult)

        refreshStatus()

        onDispose {
            Shizuku.removeBinderReceivedListener(binderReceived)
            Shizuku.removeBinderDeadListener(binderDead)
            Shizuku.removeRequestPermissionResultListener(permissionResult)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0B1020),
                        Color(0xFF111827),
                        Color(0xFF172033)
                    )
                )
            ),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Text(
                "Shizuku Lite",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                "Shizuku control dashboard",
                color = Color(0xFFCBD5E1)
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color(0xFF1E293B)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Text(
                        "SHIZUKU STATUS",
                        color = Color(0xFF93C5FD),
                        style = MaterialTheme.typography.labelLarge
                    )

                    Text(
                        status.label,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        status.detail,
                        color = Color(0xFFE2E8F0)
                    )

                    OutlinedButton(
                        onClick = { refreshStatus() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Refresh status")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    title = "Binder",
                    subtitle = if (status.binderReady) "Connected" else "Disconnected",
                    accent = if (status.binderReady)
                        Color(0xFF7DE1C3)
                    else
                        Color(0xFFFBBF24),
                    modifier = Modifier.weight(1f)
                )

                FeatureCard(
                    title = "Permission",
                    subtitle = if (status.permissionGranted) "Granted" else "Missing",
                    accent = if (status.permissionGranted)
                        Color(0xFF8AB4FF)
                    else
                        Color(0xFFF87171),
                    modifier = Modifier.weight(1f)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF111827)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        "ADB SHELL",
                        color = Color(0xFFBFDBFE),
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = command,
                        onValueChange = { command = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Shell command") },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White
                        )
                    )

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isRunning,
                        onClick = {

                            if (!status.binderReady) {
                                output = "Shizuku binder is not connected."
                                return@Button
                            }

                            if (!status.permissionGranted) {
                                ShizukuBridge.requestPermission()
                                output = "Requesting Shizuku permission..."
                                return@Button
                            }

                            scope.launch {
                                isRunning = true
                                output = "Executing command..."

                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        AdbShellHelper.execute(command)
                                    }

                                    output = buildString {
                                        append(result.output.ifBlank {
                                            "No output returned."
                                        })
                                        append("\n\nExit code: ")
                                        append(result.exitCode)
                                    }

                                } catch (error: Throwable) {
                                    output = "Execution failed: ${error.message}"
                                    Log.e(TAG, "Shell execution error", error)
                                } finally {
                                    isRunning = false
                                    refreshStatus()
                                }
                            }
                        }
                    ) {
                        Text(
                            if (isRunning) "Running..." else "Run shell command"
                        )
                    }

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            command = "id"
                        }
                    ) {
                        Text("Test UID")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F172A)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        "OUTPUT",
                        color = Color(0xFF7DE1C3),
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        output,
                        color = Color(0xFFF8FAFC)
                    )
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    ShizukuBridge.requestPermission()
                }
            ) {
                Text("Request Shizuku permission")
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    activity?.let {
                        val sent = NotificationDecorator.postStatus(
                            it,
                            status.binderReady,
                            status.permissionGranted
                        )

                        Toast.makeText(
                            context,
                            if (sent) "Notification sent"
                            else "Notification permission required",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Text("Send status notification")
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111827)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = accent.copy(alpha = 0.18f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    title,
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 6.dp
                    ),
                    color = accent,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                subtitle,
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

        val binderReady = try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }

        val permissionGranted = try {
            Shizuku.checkSelfPermission() ==
                PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }

        val uid = if (binderReady && permissionGranted) {
            try {
                Shizuku.getUid()
            } catch (_: Throwable) {
                -1
            }
        } else {
            -1
        }

        val label = when {
            !binderReady -> "Waiting for binder"
            !permissionGranted -> "Permission missing"
            uid == 0 -> "Root access ready"
            uid == 2000 -> "ADB shell ready"
            uid > 0 -> "Shizuku connected"
            else -> "Connected"
        }

        val detail = when {
            !binderReady ->
                "Open Shizuku and start its service."

            !permissionGranted ->
                "Grant Shizuku permission to this application."

            uid == 0 ->
                "Connected with root privileges."

            uid == 2000 ->
                "Connected with ADB shell privileges."

            uid > 0 ->
                "Connected using UID $uid."

            else ->
                "Binder connected."
        }

        return Status(
            binderReady,
            permissionGranted,
            uid,
            label,
            detail
        )
    }

    fun requestPermission() {
        try {
            if (Shizuku.pingBinder() &&
                Shizuku.checkSelfPermission() !=
                PackageManager.PERMISSION_GRANTED
            ) {
                Shizuku.requestPermission(REQUEST_CODE)
            }
        } catch (error: Throwable) {
            Log.e(TAG, "Permission request failed", error)
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
            return ShellResult(false, "Command is empty.", -1)
        }

        if (!Shizuku.pingBinder()) {
            return ShellResult(false, "Shizuku binder unavailable.", -1)
        }

        if (Shizuku.checkSelfPermission() !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return ShellResult(false, "Shizuku permission missing.", -1)
        }

        var process: Process? = null

        return try {

            val method = Shizuku::class.java.getMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )

            process = method.invoke(
                null,
                arrayOf("/system/bin/sh", "-c", normalized),
                null,
                null
            ) as Process

            readProcess(process)

        } catch (error: InvocationTargetException) {

            process?.destroy()

            val cause = error.targetException ?: error

            ShellResult(
                false,
                "Shizuku execution error: ${cause.message}",
                -1
            )

        } catch (error: Throwable) {

            process?.destroy()

            Log.e(TAG, "Shizuku shell failed", error)

            ShellResult(
                false,
                "Shizuku shell failed: ${error.message}",
                -1
            )
        }
    }

    private fun readProcess(process: Process): ShellResult {

        val stdout = StringBuilder()
        val stderr = StringBuilder()

        val stdoutThread = thread {
            BufferedReader(
                InputStreamReader(process.inputStream)
            ).use {
                stdout.append(it.readText())
            }
        }

        val stderrThread = thread {
            BufferedReader(
                InputStreamReader(process.errorStream)
            ).use {
                stderr.append(it.readText())
            }
        }

        val exitCode = process.waitFor()

        stdoutThread.join()
        stderrThread.join()

        val result = buildString {
            append(stdout)

            if (stderr.isNotEmpty()) {
                if (isNotEmpty()) append("\n")
                append(stderr)
            }
        }.trim()

        return ShellResult(
            exitCode == 0,
            result,
            exitCode
        )
    }
}
