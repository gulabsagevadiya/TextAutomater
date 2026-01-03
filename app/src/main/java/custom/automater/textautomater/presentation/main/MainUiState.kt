package custom.automater.textautomater.presentation.main

data class MainUiState(
    val hasSmsPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val whatsAppAutomationEnabled: Boolean = false
)
