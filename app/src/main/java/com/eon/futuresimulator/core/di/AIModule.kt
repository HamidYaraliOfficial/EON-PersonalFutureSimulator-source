package com.eon.futuresimulator.core.di

import com.eon.futuresimulator.ai.AIProvider
import com.eon.futuresimulator.ai.LocalHeuristicAIProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {
    /**
     * The default AI provider is always the on-device [LocalHeuristicAIProvider] — the
     * whole app, including the Simulation Engine, works with zero network access.
     * A future settings-driven qualifier can swap this for CloudAIProvider once the
     * user opts in via the Privacy Center; see ai/CloudAIProvider.kt.
     */
    @Provides
    @Singleton
    fun provideAIProvider(local: LocalHeuristicAIProvider): AIProvider = local
}
