package com.example.leveluplife

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.leveluplife.notifications.TaskNotifier
import com.example.leveluplife.ui.navigation.AppNavigation
import com.example.leveluplife.ui.theme.LevelUpLifeTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val pendingTaskId = mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationsPermissionIfNeeded()
        pendingTaskId.value = taskIdFromIntent(intent)
        val container = (application as LevelUpLifeApp).container
        setContent {
            val themeMode by container.themeController.mode.collectAsState()
            LevelUpLifeTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        container = container,
                        deepLinkTaskId = pendingTaskId.value,
                        onDeepLinkHandled = { pendingTaskId.value = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        taskIdFromIntent(intent)?.let { pendingTaskId.value = it }
    }

    private fun taskIdFromIntent(intent: Intent?): Int? =
        intent?.getIntExtra(TaskNotifier.EXTRA_TASK_ID, -1)?.takeIf { it > 0 }

    private fun requestNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestNotificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
