package custom.automater.textautomater

import android.app.Application
import android.content.Context
import androidx.core.content.edit

val sharePrefHelper: SharePrefHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED){
  MyApp.sharePrefs!!
}

val smsHelper: SMSHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
  MyApp.smsHelper!!
}


class MyApp: Application(){

  companion object {
    var sharePrefs: SharePrefHelper? = null
    var smsHelper: SMSHelper? = null
  }

  override fun onCreate() {
    super.onCreate()
    sharePrefs = SharePrefHelper(this)
    smsHelper = SMSHelper(this)
  }

}


class SharePrefHelper(val app: MyApp) {

  var whatsAppAutomation: Boolean
    get() {
      return app
        .getSharedPreferences("UserSharedPref", Context.MODE_PRIVATE)
        .getBoolean("isWhatsAppService", false)
    }
    set(value) {
      app
        .getSharedPreferences("UserSharedPref", Context.MODE_PRIVATE)
        .edit {
          putBoolean("isWhatsAppService", value)
        }
    }
}