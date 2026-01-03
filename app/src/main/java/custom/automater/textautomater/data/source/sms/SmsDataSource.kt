package custom.automater.textautomater.data.source.sms

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import custom.automater.textautomater.domain.model.SmsResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class SmsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SmsDataSource"
        private const val SMS_SENT_ACTION = "SMS_SENT"
        private const val MAX_SMS_LENGTH = 160
    }

    suspend fun sendSms(phoneNumber: String, message: String): SmsResult {
        if (!isValidPhoneNumber(phoneNumber)) {
            return SmsResult.Error("Invalid phone number format")
        }

        if (message.isBlank()) {
            return SmsResult.Error("Message cannot be empty")
        }

        if (!hasPermission()) {
            return SmsResult.Error("SMS permission not granted")
        }

        if (!isSmsCapable()) {
            return SmsResult.Error("Device doesn't support SMS")
        }

        return suspendCancellableCoroutine { continuation ->
            try {
                val smsManager = getSmsManager()
                if (smsManager == null) {
                    continuation.resume(SmsResult.Error("SMS Manager not available"))
                    return@suspendCancellableCoroutine
                }

                val sentReceiver = object : BroadcastReceiver() {
                    override fun onReceive(ctx: Context, intent: Intent) {
                        try {
                            ctx.unregisterReceiver(this)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to unregister receiver", e)
                        }

                        val result = when (resultCode) {
                            Activity.RESULT_OK -> SmsResult.Success("Message sent successfully")
                            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> SmsResult.Error("Generic failure")
                            SmsManager.RESULT_ERROR_NO_SERVICE -> SmsResult.Error("No network service")
                            SmsManager.RESULT_ERROR_NULL_PDU -> SmsResult.Error("Invalid format")
                            SmsManager.RESULT_ERROR_RADIO_OFF -> SmsResult.Error("Radio is off")
                            SmsManager.RESULT_ERROR_LIMIT_EXCEEDED -> SmsResult.Error("SMS limit exceeded")
                            else -> SmsResult.Error("Unknown error")
                        }

                        if (continuation.isActive) {
                            continuation.resume(result)
                        }
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(
                        sentReceiver,
                        IntentFilter(SMS_SENT_ACTION),
                        Context.RECEIVER_NOT_EXPORTED
                    )
                } else {
                    ContextCompat.registerReceiver(
                        context,
                        sentReceiver,
                        IntentFilter(SMS_SENT_ACTION),
                        ContextCompat.RECEIVER_NOT_EXPORTED
                    )
                }

                val sentIntent = PendingIntent.getBroadcast(
                    context, 0, Intent(SMS_SENT_ACTION),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (message.length > MAX_SMS_LENGTH) {
                    val parts = smsManager.divideMessage(message)
                    val sentIntents = ArrayList<PendingIntent>()
                    for (i in parts.indices) {
                        sentIntents.add(
                            PendingIntent.getBroadcast(
                                context, i, Intent(SMS_SENT_ACTION),
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                        )
                    }
                    smsManager.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, null)
                } else {
                    smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, null)
                }

                Log.d(TAG, "SMS sent to $phoneNumber")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to send SMS", e)
                if (continuation.isActive) {
                    continuation.resume(SmsResult.Error("Failed to send SMS: ${e.message}"))
                }
            }
        }
    }

    private fun getSmsManager(): SmsManager? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get SMS Manager", e)
            null
        }
    }

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isSmsCapable(): Boolean {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            @Suppress("DEPRECATION")
            telephonyManager?.isSmsCapable ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check SMS capability", e)
            false
        }
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return try {
            val cleanNumber = phoneNumber.replace(Regex("[^+\\d]"), "")
            cleanNumber.isNotEmpty() && cleanNumber.length >= 7 &&
                    (cleanNumber.startsWith("+") || cleanNumber.all { it.isDigit() })
        } catch (e: Exception) {
            false
        }
    }
}
