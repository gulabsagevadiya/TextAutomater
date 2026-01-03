package custom.automater.textautomater

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import custom.automater.textautomater.domain.repository.SettingsRepository
import custom.automater.textautomater.presentation.main.MainScreen
import custom.automater.textautomater.presentation.main.MainViewModel
import custom.automater.textautomater.presentation.theme.TextAutomaterTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            TextAutomaterTheme {
                MainScreen(
                    viewModel = viewModel,
                    onSmsPermissionClick = { openAppSettings() },
                    onAccessibilityPermissionClick = { openAccessibilitySettings() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_SEND_TEXT -> {
                val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER)
                val message = intent.getStringExtra(EXTRA_MESSAGE)
                if (!phoneNumber.isNullOrBlank() && !message.isNullOrBlank()) {
                    sendTextMessage(phoneNumber, message)
                }
            }

            ACTION_SET_WHATSAPP_AUTOMATION -> {
                val automation = intent.getBooleanExtra(EXTRA_SET_AUTOMATION, false)
                lifecycleScope.launch {
                    settingsRepository.setWhatsAppAutomation(automation)
                    finishAffinity()
                }
            }
        }
    }

    private fun sendTextMessage(phoneNumber: String, message: String) {
        viewModel.sendSms(phoneNumber, message) { success, resultMessage ->
            Toast.makeText(
                this,
                "Result: $success\nMessage: $resultMessage",
                Toast.LENGTH_LONG
            ).show()
            finishAffinity()
        }
    }

    private fun openAppSettings() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    companion object {
        const val ACTION_SEND_TEXT = "custom.automater.textautomater.SEND_TEXT"
        const val ACTION_SET_WHATSAPP_AUTOMATION = "custom.automater.textautomater.SET_WHATSAPP_AUTOMATION"
        const val EXTRA_PHONE_NUMBER = "phoneNumber"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_SET_AUTOMATION = "setAutomation"
    }
}
