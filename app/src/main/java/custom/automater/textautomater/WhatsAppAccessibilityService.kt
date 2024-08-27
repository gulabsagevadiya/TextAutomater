package custom.automater.textautomater

import android.accessibilityservice.AccessibilityService
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

class WhatsAppAccessibilityService : AccessibilityService() {

    companion object {
        private fun isAccessibilityOn(clazz: Class<out AccessibilityService?>): Boolean {
            with(sharePrefHelper.app){
                var accessibilityEnabled = 0
                val service = packageName + "/" + clazz.canonicalName
                try {
                    accessibilityEnabled = Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
                } catch (ignored: Settings.SettingNotFoundException) {}

                val colonSplitter = TextUtils.SimpleStringSplitter(':')
                if (accessibilityEnabled == 1) {
                    val settingValue = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
                    if (settingValue != null) {
                        colonSplitter.setString(settingValue)
                        while (colonSplitter.hasNext()) {
                            val accessibilityService = colonSplitter.next()
                            if (accessibilityService.equals(service, ignoreCase = true)) {
                                return true
                            }
                        }
                    }
                }
                return false
            }
        }


        val checkAccessibilityService: Boolean get() {
            return isAccessibilityOn(WhatsAppAccessibilityService::class.java)
        }

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (sharePrefHelper.whatsAppAutomation) {
            try {
                if (rootInActiveWindow == null) {
                    return
                }
                val rootInActiveWindow = AccessibilityNodeInfoCompat.wrap(rootInActiveWindow)
                val sendMessageNodeInfoList = when(rootInActiveWindow.packageName){
                    "com.whatsapp" -> {
                        rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send")
                    }

                    "com.whatsapp.w4b" -> {
                        rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.whatsapp.w4b:id/send")
                    }
                    else -> {
                        emptyList()
                    }
                }

                val dialogNodeList = when(rootInActiveWindow.packageName){
                    "com.whatsapp","com.whatsapp.w4b" -> {
                        rootInActiveWindow.findAccessibilityNodeInfosByText("Profile Not Found")
                    }
                    else -> {
                        emptyList()
                    }
                }

                if (dialogNodeList.isNotEmpty()){
                    try {
                        rootInActiveWindow.findAccessibilityNodeInfosByText("OK")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }catch (_: Exception){}
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
                performGlobalAction(GLOBAL_ACTION_BACK)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onInterrupt() {
        println("Whatsapp Accessibility Service onInterrupt")
    }
}