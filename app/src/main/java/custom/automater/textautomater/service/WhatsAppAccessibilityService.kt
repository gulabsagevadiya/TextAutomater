package custom.automater.textautomater.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import custom.automater.textautomater.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@SuppressLint("AccessibilityPolicy")
class WhatsAppAccessibilityService : AccessibilityService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WhatsAppAccessibilityServiceEntryPoint {
        fun settingsRepository(): SettingsRepository
    }

    private val settingsRepository: SettingsRepository by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            WhatsAppAccessibilityServiceEntryPoint::class.java
        ).settingsRepository()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (settingsRepository.getWhatsAppAutomationSync()) {
            try {
                if (rootInActiveWindow == null) {
                    return
                }
                val rootNode = AccessibilityNodeInfoCompat.wrap(rootInActiveWindow)
                val sendMessageNodeInfoList = when (rootNode.packageName) {
                    WHATSAPP_PACKAGE -> {
                        rootNode.findAccessibilityNodeInfosByViewId("$WHATSAPP_PACKAGE:id/send")
                    }

                    WHATSAPP_BUSINESS_PACKAGE -> {
                        rootNode.findAccessibilityNodeInfosByViewId("$WHATSAPP_BUSINESS_PACKAGE:id/send")
                    }

                    else -> {
                        emptyList()
                    }
                }

                val dialogNodeList = when (rootNode.packageName) {
                    WHATSAPP_PACKAGE, WHATSAPP_BUSINESS_PACKAGE -> {
                        rootNode.findAccessibilityNodeInfosByText("Profile Not Found")
                    }

                    else -> {
                        emptyList()
                    }
                }

                if (dialogNodeList.isNotEmpty()) {
                    try {
                        rootNode.findAccessibilityNodeInfosByText("OK")[0]
                            .performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    } catch (_: Exception) {
                    }
                }

                if (sendMessageNodeInfoList.isEmpty()) {
                    return
                }

                val sendMessageButton = sendMessageNodeInfoList[0]
                if (!sendMessageButton.isVisibleToUser) {
                    return
                }
                sendMessageButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Thread.sleep(500)
                performGlobalAction(GLOBAL_ACTION_BACK)
                Thread.sleep(500)
                performGlobalAction(GLOBAL_ACTION_BACK)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    companion object {
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
    }
}
