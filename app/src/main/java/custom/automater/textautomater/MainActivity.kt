package custom.automater.textautomater

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.telephony.SmsManager
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

  private lateinit var smsSwitch: SwitchCompat
  private lateinit var whatsAppSwitch: SwitchCompat
  private lateinit var whatsAppAutomationSwitch: SwitchCompat

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_user_screen)
    handleIntent(intent)
    smsSwitch = findViewById(R.id.sms_switch)
    whatsAppSwitch = findViewById(R.id.whats_app_switch)
    whatsAppAutomationSwitch = findViewById(R.id.whats_app_automation_switch)

    smsSwitch.setOnCheckedChangeListener { _, isChecked ->
      if (isChecked && (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)){
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
          data = Uri.fromParts("package", packageName, null)
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
      }
    }

    whatsAppSwitch.setOnCheckedChangeListener { _, isChecked ->
      if(isChecked && !WhatsAppAccessibilityService.checkAccessibilityService){
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
      }
    }

    whatsAppAutomationSwitch.setOnCheckedChangeListener { _, isChecked ->
      if(!isChecked){
        sharePrefHelper.whatsAppAutomation = false
      }
    }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    handleIntent(intent)
  }

  private fun handleIntent(intent: Intent?) {
    when (intent?.action) {
      "custom.automater.textautomater.SEND_TEXT" -> {
        val phoneNumber = intent.getStringExtra("phoneNumber")
        val message = intent.getStringExtra("message")
        sendTextMessage(phoneNumber!!, message!!)
      }
      "custom.automater.textautomater.SET_WHATSAPP_AUTOMATION" -> {
        val automation = intent.getBooleanExtra("setAutomation", false)
        sharePrefHelper.whatsAppAutomation = automation
        onBackPressedDispatcher.onBackPressed()
      }
    }
  }

  private fun sendTextMessage(phoneNumber: String, message: String) {
    if (ActivityCompat.checkSelfPermission(
        this,
        android.Manifest.permission.SEND_SMS
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      val smsManager: SmsManager = applicationContext.getSystemService(SmsManager::class.java)
      if (message.length > 160) {
        val parts = smsManager.divideMessage(message)
        smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
      } else {
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
      }
      onBackPressedDispatcher.onBackPressed()
    } else {
      startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      })
    }
  }

  override fun onResume() {
    super.onResume()
    smsSwitch.isChecked = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    whatsAppSwitch.isChecked = WhatsAppAccessibilityService.checkAccessibilityService
    whatsAppAutomationSwitch.isChecked = sharePrefHelper.whatsAppAutomation
  }
}