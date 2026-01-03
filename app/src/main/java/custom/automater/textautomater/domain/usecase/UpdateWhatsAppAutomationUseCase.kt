package custom.automater.textautomater.domain.usecase

import custom.automater.textautomater.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateWhatsAppAutomationUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        settingsRepository.setWhatsAppAutomation(enabled)
    }
}
