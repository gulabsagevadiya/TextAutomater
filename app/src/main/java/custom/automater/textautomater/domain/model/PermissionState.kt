package custom.automater.textautomater.domain.model

data class PermissionState(
    val hasSmsPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false
)
