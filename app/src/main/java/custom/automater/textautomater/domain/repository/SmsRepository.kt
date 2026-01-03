package custom.automater.textautomater.domain.repository

import custom.automater.textautomater.domain.model.SmsResult

interface SmsRepository {
    suspend fun sendSms(phoneNumber: String, message: String): SmsResult
    fun hasSmsPermission(): Boolean
    fun isDeviceSmsCapable(): Boolean
}
