package custom.automater.textautomater.domain.model

sealed class SmsResult {
    data class Success(val message: String) : SmsResult()
    data class Error(val message: String) : SmsResult()
}
