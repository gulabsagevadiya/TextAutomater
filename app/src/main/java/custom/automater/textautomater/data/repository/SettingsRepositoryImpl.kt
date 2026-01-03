package custom.automater.textautomater.data.repository

import custom.automater.textautomater.data.source.local.SettingsLocalDataSource
import custom.automater.textautomater.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val localDataSource: SettingsLocalDataSource
) : SettingsRepository {

    override val whatsAppAutomation: Flow<Boolean>
        get() = localDataSource.whatsAppAutomation

    override suspend fun setWhatsAppAutomation(enabled: Boolean) {
        localDataSource.setWhatsAppAutomation(enabled)
    }

    override fun getWhatsAppAutomationSync(): Boolean {
        return localDataSource.getWhatsAppAutomation()
    }
}
