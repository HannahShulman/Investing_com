package com.hanna.investing_com.di

import com.hanna.investing_com.repositories.LocationRepository
import com.hanna.investing_com.repositories.impl.LocationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@InstallIn(ActivityComponent::class)
@Module
abstract class RepositoryModule {

    @Binds
    abstract fun repository(repository: LocationRepositoryImpl): LocationRepository
}