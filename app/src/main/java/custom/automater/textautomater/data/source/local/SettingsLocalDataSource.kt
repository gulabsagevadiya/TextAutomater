package custom.automater.textautomater.data.source.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    val whatsAppAutomation: Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_WHATSAPP_AUTOMATION) {
                trySend(getWhatsAppAutomation())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(getWhatsAppAutomation())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun getWhatsAppAutomation(): Boolean {
        return prefs.getBoolean(KEY_WHATSAPP_AUTOMATION, false)
    }

    fun setWhatsAppAutomation(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_WHATSAPP_AUTOMATION, enabled) }
    }

    companion object {
        private const val PREFS_NAME = "UserSharedPref"
        private const val KEY_WHATSAPP_AUTOMATION = "isWhatsAppService"
    }
}
