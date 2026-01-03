package custom.automater.textautomater.domain.usecase

import custom.automater.textautomater.domain.model.SmsResult
import custom.automater.textautomater.domain.repository.SmsRepository
import javax.inject.Inject

class SendSmsUseCase @Inject constructor(
    private val smsRepository: SmsRepository
) {
    suspend operator fun invoke(phoneNumber: String, message: String): SmsResult {
        return smsRepository.sendSms(phoneNumber, message)
    }
}
