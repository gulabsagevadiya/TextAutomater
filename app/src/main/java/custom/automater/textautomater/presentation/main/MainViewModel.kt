package custom.automater.textautomater.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import custom.automater.textautomater.domain.usecase.CheckPermissionsUseCase
import custom.automater.textautomater.domain.usecase.GetSettingsUseCase
import custom.automater.textautomater.domain.usecase.SendSmsUseCase
import custom.automater.textautomater.domain.usecase.UpdateWhatsAppAutomationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val checkPermissionsUseCase: CheckPermissionsUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateWhatsAppAutomationUseCase: UpdateWhatsAppAutomationUseCase,
    private val sendSmsUseCase: SendSmsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            getSettingsUseCase().collect { automationEnabled ->
                _uiState.update { it.copy(whatsAppAutomationEnabled = automationEnabled) }
            }
        }
    }

    fun refreshPermissions() {
        val permissionState = checkPermissionsUseCase()
        _uiState.update {
            it.copy(
                hasSmsPermission = permissionState.hasSmsPermission,
                hasAccessibilityPermission = permissionState.hasAccessibilityPermission
            )
        }
    }

    fun setWhatsAppAutomation(enabled: Boolean) {
        viewModelScope.launch {
            updateWhatsAppAutomationUseCase(enabled)
        }
    }

    fun sendSms(phoneNumber: String, message: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val result = sendSmsUseCase(phoneNumber, message)) {
                is custom.automater.textautomater.domain.model.SmsResult.Success -> {
                    onResult(true, result.message)
                }
                is custom.automater.textautomater.domain.model.SmsResult.Error -> {
                    onResult(false, result.message)
                }
            }
        }
    }
}
