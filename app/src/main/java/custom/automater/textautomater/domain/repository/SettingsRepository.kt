package custom.automater.textautomater.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val whatsAppAutomation: Flow<Boolean>
    suspend fun setWhatsAppAutomation(enabled: Boolean)
    fun getWhatsAppAutomationSync(): Boolean
}
