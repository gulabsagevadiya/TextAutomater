package custom.automater.textautomater.domain.usecase

import custom.automater.textautomater.domain.model.PermissionState
import custom.automater.textautomater.domain.repository.PermissionRepository
import javax.inject.Inject

class CheckPermissionsUseCase @Inject constructor(
    private val permissionRepository: PermissionRepository
) {
    operator fun invoke(): PermissionState {
        return permissionRepository.getPermissionState()
    }
}
