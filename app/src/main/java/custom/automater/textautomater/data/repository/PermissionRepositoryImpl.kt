package custom.automater.textautomater.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.TextUtils
import androidx.core.content.ContextCompat
import custom.automater.textautomater.domain.model.PermissionState
import custom.automater.textautomater.domain.repository.PermissionRepository
import custom.automater.textautomater.service.WhatsAppAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PermissionRepository {

    override fun getPermissionState(): PermissionState {
        return PermissionState(
            hasSmsPermission = hasSmsPermission(),
            hasAccessibilityPermission = hasAccessibilityPermission()
        )
    }

    override fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun hasAccessibilityPermission(): Boolean {
        return try {
            val accessibilityEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED,
                0
            )
            if (accessibilityEnabled != 1) {
                return false
            }

            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            val expectedService = "${context.packageName}/${WhatsAppAccessibilityService::class.java.canonicalName}"

            while (colonSplitter.hasNext()) {
                val service = colonSplitter.next()
                if (service.equals(expectedService, ignoreCase = true)) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
}
