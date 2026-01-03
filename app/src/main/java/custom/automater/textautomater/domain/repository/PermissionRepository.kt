package custom.automater.textautomater.domain.repository

import custom.automater.textautomater.domain.model.PermissionState

interface PermissionRepository {
    fun getPermissionState(): PermissionState
    fun hasSmsPermission(): Boolean
    fun hasAccessibilityPermission(): Boolean
}
