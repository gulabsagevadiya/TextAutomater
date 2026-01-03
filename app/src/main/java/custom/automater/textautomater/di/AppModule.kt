package custom.automater.textautomater.di

import custom.automater.textautomater.data.repository.PermissionRepositoryImpl
import custom.automater.textautomater.data.repository.SettingsRepositoryImpl
import custom.automater.textautomater.data.repository.SmsRepositoryImpl
import custom.automater.textautomater.domain.repository.PermissionRepository
import custom.automater.textautomater.domain.repository.SettingsRepository
import custom.automater.textautomater.domain.repository.SmsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindSmsRepository(
        smsRepositoryImpl: SmsRepositoryImpl
    ): SmsRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindPermissionRepository(
        permissionRepositoryImpl: PermissionRepositoryImpl
    ): PermissionRepository
}
