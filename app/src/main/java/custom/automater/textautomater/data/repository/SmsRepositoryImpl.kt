package custom.automater.textautomater.data.repository

import custom.automater.textautomater.data.source.sms.SmsDataSource
import custom.automater.textautomater.domain.model.SmsResult
import custom.automater.textautomater.domain.repository.SmsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsRepositoryImpl @Inject constructor(
    private val smsDataSource: SmsDataSource
) : SmsRepository {

    override suspend fun sendSms(phoneNumber: String, message: String): SmsResult {
        return smsDataSource.sendSms(phoneNumber, message)
    }

    override fun hasSmsPermission(): Boolean {
        return smsDataSource.hasPermission()
    }

    override fun isDeviceSmsCapable(): Boolean {
        return smsDataSource.isSmsCapable()
    }
}
