package custom.automater.textautomater

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

class SMSHelper(private val app: MyApp) {

  companion object {
    private const val TAG = "SMSHelper"
    private const val SMS_SENT_ACTION = "SMS_SENT"
    private const val SMS_DELIVERED_ACTION = "SMS_DELIVERED"
    private const val MAX_SMS_LENGTH = 160
  }

  // Static receivers that can work with application context
  private val sentReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val message = when (resultCode) {
        Activity.RESULT_OK -> {
          Log.d(TAG, "SMS sent successfully")
          "Message sent successfully"
        }
        SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
          Log.e(TAG, "SMS failed: Generic failure")
          "Failed to send: Generic error"
        }
        SmsManager.RESULT_ERROR_NO_SERVICE -> {
          Log.e(TAG, "SMS failed: No service")
          "Failed to send: No network service"
        }
        SmsManager.RESULT_ERROR_NULL_PDU -> {
          Log.e(TAG, "SMS failed: Null PDU")
          "Failed to send: Invalid format"
        }
        SmsManager.RESULT_ERROR_RADIO_OFF -> {
          Log.e(TAG, "SMS failed: Radio off")
          "Failed to send: Radio is off"
        }
        SmsManager.RESULT_ERROR_LIMIT_EXCEEDED -> {
          Log.e(TAG, "SMS failed: Limit exceeded")
          "Failed to send: SMS limit exceeded"
        }
        else -> {
          Log.e(TAG, "SMS failed: Unknown error code $resultCode")
          "Failed to send: Unknown error"
        }
      }

      // Show toast using application context
      showToast(message)

      // Notify callback if available
      currentCallback?.invoke(resultCode == Activity.RESULT_OK, message)
    }
  }

  private val deliveredReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val message = when (resultCode) {
        Activity.RESULT_OK -> {
          Log.d(TAG, "SMS delivered successfully")
          "Message delivered"
        }
        Activity.RESULT_CANCELED -> {
          Log.w(TAG, "SMS delivery cancelled or failed")
          "Message delivery failed"
        }
        else -> {
          Log.w(TAG, "SMS delivery status unknown")
          "Message delivery status unknown"
        }
      }
      showToast(message)
    }
  }

  // Store current callback for the receivers
  private var currentCallback: ((Boolean, String) -> Unit)? = null
  private var receiversRegistered = false

  fun sendTextMessage(
    phoneNumber: String,
    message: String,
    callback: ((Boolean, String) -> Unit)? = null,
  ) {
    // Store callback for receivers
    currentCallback = callback

    // Input validation
    if (!isValidPhoneNumber(phoneNumber)) {
      val error = "Invalid phone number format"
      Log.e(TAG, error)
      callback?.invoke(false, error)
      showToast(error)
      return
    }

    if (message.isBlank()) {
      val error = "Message cannot be empty"
      Log.e(TAG, error)
      callback?.invoke(false, error)
      showToast(error)
      return
    }

    // Check permissions
    if (!hasRequiredPermissions()) {
      val error = "SMS permission not granted"
      Log.e(TAG, error)
      callback?.invoke(false, error)
      openAppSettings()
      return
    }

    // Check if SMS functionality is available
    if (!isSMSCapable()) {
      val error = "Device doesn't support SMS"
      Log.e(TAG, error)
      callback?.invoke(false, error)
      fallbackToSMSApp(phoneNumber, message)
      return
    }

    try {
      val smsManager = getSMSManager()
      if (smsManager == null) {
        val error = "SMS Manager not available"
        Log.e(TAG, error)
        callback?.invoke(false, error)
        fallbackToSMSApp(phoneNumber, message)
        return
      }

      // Register broadcast receivers for delivery confirmation
      registerReceivers()

      // Send SMS based on message length
      if (needsMultipartSending(message)) {
        sendMultipartSMS(smsManager, phoneNumber, message)
      } else {
        sendSingleSMS(smsManager, phoneNumber, message)
      }

    } catch (e: Exception) {
      val error = "Failed to send SMS: ${e.message}"
      Log.e(TAG, error, e)
      callback?.invoke(false, error)
      showToast(error)

      // Fallback to SMS app
      fallbackToSMSApp(phoneNumber, message)
    }
  }

  private fun getSMSManager(): SmsManager? {
    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12+ (API 31+)
        app.getSystemService(SmsManager::class.java)
      } else {
        // Older versions
        @Suppress("DEPRECATION")
        SmsManager.getDefault()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to get SMS Manager", e)
      null
    }
  }

  private fun sendSingleSMS(smsManager: SmsManager, phoneNumber: String, message: String) {
    try {
      val sentIntent = PendingIntent.getBroadcast(
        app, 0, Intent(SMS_SENT_ACTION),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      val deliveredIntent = PendingIntent.getBroadcast(
        app, 0, Intent(SMS_DELIVERED_ACTION),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, deliveredIntent)
      Log.d(TAG, "Single SMS sent to $phoneNumber")

    } catch (e: Exception) {
      val error = "Failed to send single SMS: ${e.message}"
      Log.e(TAG, error, e)
      throw e
    }
  }

  private fun sendMultipartSMS(smsManager: SmsManager, phoneNumber: String, message: String) {
    try {
      val parts = smsManager.divideMessage(message)
      val sentIntents = ArrayList<PendingIntent>()
      val deliveredIntents = ArrayList<PendingIntent>()

      // Create pending intents for each part
      for (i in parts.indices) {
        sentIntents.add(PendingIntent.getBroadcast(
          app, i, Intent(SMS_SENT_ACTION),
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ))
        deliveredIntents.add(PendingIntent.getBroadcast(
          app, i, Intent(SMS_DELIVERED_ACTION),
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ))
      }

      smsManager.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, deliveredIntents)
      Log.d(TAG, "Multipart SMS sent to $phoneNumber (${parts.size} parts)")

    } catch (e: Exception) {
      val error = "Failed to send multipart SMS: ${e.message}"
      Log.e(TAG, error, e)
      throw e
    }
  }

  private fun needsMultipartSending(message: String): Boolean {
    return message.length > MAX_SMS_LENGTH
  }

  private fun hasRequiredPermissions(): Boolean {
    return ContextCompat.checkSelfPermission(app, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
  }

  private fun isSMSCapable(): Boolean {
    return try {
      val telephonyManager = app.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
      telephonyManager?.isSmsCapable ?: false
    } catch (e: Exception) {
      Log.e(TAG, "Failed to check SMS capability", e)
      false
    }
  }

  private fun isValidPhoneNumber(phoneNumber: String): Boolean {
    return try {
      val cleanNumber = phoneNumber.replace(Regex("[^+\\d]"), "")
      cleanNumber.isNotEmpty() && (cleanNumber.length >= 7) &&
          (cleanNumber.startsWith("+") || cleanNumber.all { it.isDigit() })
    } catch (e: Exception) {
      false
    }
  }

  private fun openAppSettings() {
    try {
      val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", app.packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      app.startActivity(intent)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to open app settings", e)
    }
  }

  private fun fallbackToSMSApp(phoneNumber: String, message: String) {
    try {
      val smsIntent = Intent(Intent.ACTION_VIEW).apply {
        data = "smsto:$phoneNumber".toUri()
        putExtra("sms_body", message)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }

      if (smsIntent.resolveActivity(app.packageManager) != null) {
        app.startActivity(smsIntent)
        showToast("Opening SMS app...")
      } else {
        showToast("No SMS app available on this device")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to open SMS app", e)
      showToast("Failed to open SMS app")
    }
  }

  private fun registerReceivers() {
    if (receiversRegistered) return

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        app.registerReceiver(sentReceiver, IntentFilter(SMS_SENT_ACTION), Context.RECEIVER_NOT_EXPORTED)
        app.registerReceiver(deliveredReceiver, IntentFilter(SMS_DELIVERED_ACTION), Context.RECEIVER_NOT_EXPORTED)
      } else {
        ContextCompat.registerReceiver(app, sentReceiver, IntentFilter(SMS_SENT_ACTION), ContextCompat.RECEIVER_NOT_EXPORTED)
        ContextCompat.registerReceiver(app, deliveredReceiver, IntentFilter(SMS_DELIVERED_ACTION), ContextCompat.RECEIVER_NOT_EXPORTED)
      }
      receiversRegistered = true
    } catch (e: Exception) {
      Log.e(TAG, "Failed to register receivers", e)
    }
  }

  private fun showToast(message: String) {
    try {
      Handler(Looper.getMainLooper()).post {
        Toast.makeText(app, message, Toast.LENGTH_SHORT).show()
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to show toast", e)
    }
  }

  fun hasPermission(): Boolean = hasRequiredPermissions()

  fun isDeviceSMSCapable(): Boolean = isSMSCapable()

  fun cleanup() {
    if (receiversRegistered) {
      try {
        app.unregisterReceiver(sentReceiver)
        app.unregisterReceiver(deliveredReceiver)
        receiversRegistered = false
      } catch (e: Exception) {
        Log.e(TAG, "Failed to unregister receivers", e)
      }
    }
  }
}