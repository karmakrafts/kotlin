/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.session

import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.checkers.registerCommonCheckers
import org.jetbrains.kotlin.fir.deserialization.ModuleDataProvider
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirSwitchableExtensionDeclarationsSymbolProvider
import org.jetbrains.kotlin.fir.java.FirCliSession
import org.jetbrains.kotlin.fir.java.FirProjectSessionProvider
import org.jetbrains.kotlin.fir.resolve.providers.DEPENDENCIES_SYMBOL_PROVIDER_QUALIFIED_KEY
import org.jetbrains.kotlin.fir.resolve.providers.FirProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.impl.*
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.plusIfNotNull

/**
 * This is the base class for factories, which create various sessions for compilation.
 * Some details are different between platforms, but the main session structure is the same
 * for all platforms and looks like the following:
 *
 * There are three different kinds of sessions:
 * - Source session contains sources which are going to be analyzed with all corresponding components.
 * - Library session contains regular dependencies of the compilation (classpath)
 * - Shared library session contains some special dependencies, like builtins, synthetic kotlin.FunctionN interfaces, etc
 *
 * These three kinds of sessions are used in different schemes, depending on the compilation scheme.
 * There are also three different compilation schemes:
 *
 * ### Regular non-mpp compilation
 *
 * In this scheme all sources are compiled together against fixed dependencies.
 * Sessions layout is the following:
 *
 *
 *  Source session ───────────► Libraries session ───────────► Shared libraries session
 *
 *
 * ### Old mpp compilation
 *
 * This is the MPP compilation scheme, which was used from 2.0 to 2.2.
 * In this scheme all sources are split by source sets, but all sources depend on the same dependencies
 *
 *
 *  Source session (common)   ─────┐
 *            ▲                    │
 *            │                    ├─────► Libraries session ───────────► Shared libraries session
 *            │                    │
 *  Source session (platform) ─────┘
 *
 *
 * ### Hierarchical mpp compilation
 *
 * This is the new MPP compilation scheme, in which each source set depend on it's own set of dependencies
 *
 *
 *  Source session (common)   ───────────►  Libraries session (common)  ─────┐
 *            ▲                                                              │
 *            │                                                              ├─────► Shared libraries session
 *            │                                                              │
 *  Source session (platform) ───────────► Libraries session (platform) ─────┘
 */
@OptIn(PrivateSessionConstructor::class, SessionConfiguration::class)
abstract class FirAbstractSessionFactory<LIBRARY_CONTEXT, SOURCE_CONTEXT> {

    // ==================================== Shared library session ====================================

    /**
     * Creates session which contains synthetic symbol providers, like builtins provider, synthetic function interface provider, etc.
     *
     * For more information see KDoc to [FirAbstractSessionFactory]
     */
    protected fun createSharedLibrarySession(
        mainModuleName: Name,
        context: LIBRARY_CONTEXT,
        sessionProvider: FirProjectSessionProvider,
        languageVersionSettings: LanguageVersionSettings,
        extensionRegistrars: List<FirExtensionRegistrar>,
        createSharedProviders: (FirSession, FirModuleData, FirKotlinScopeProvider, FirExtensionSyntheticFunctionInterfaceProvider?) -> List<FirSymbolProvider>
    ): FirSession {
        return FirCliSession(sessionProvider, FirSession.Kind.Library).apply session@{
            registerCliCompilerOnlyComponents(languageVersionSettings)
            registerCommonComponents(languageVersionSettings)
            registerLibrarySessionComponents(context)

            val kotlinScopeProvider = createKotlinScopeProviderForLibrarySession()
            register(FirKotlinScopeProvider::class, kotlinScopeProvider)

            val moduleData = FirBinaryDependenciesModuleData(
                Name.special("<shared dependencies of ${mainModuleName.asString()}>")
            )
            moduleData.bindSession(this)

            FirSessionConfigurator(this).apply {
                for (extensionRegistrar in extensionRegistrars) {
                    registerExtensions(extensionRegistrar.configure())
                }
            }.configure()
            registerCommonComponentsAfterExtensionsAreConfigured()

            val syntheticFunctionInterfaceProvider = FirExtensionSyntheticFunctionInterfaceProvider.createIfNeeded(
                this,
                moduleData,
                kotlinScopeProvider
            )
            val providers = createSharedProviders(
                this,
                moduleData,
                kotlinScopeProvider,
                syntheticFunctionInterfaceProvider
            )
            val symbolProvider = FirCachingCompositeSymbolProvider(this, providers)
            register(FirSymbolProvider::class, symbolProvider)
            register(FirProvider::class, FirLibrarySessionProvider(symbolProvider))
        }
    }

    // ==================================== Library session ====================================

    /**
     * Creates session which contains symbol providers for regular dependencies (classpath)
     *
     * For more information see KDoc to [FirAbstractSessionFactory]
     */
    protected fun createLibrarySession(
        context: LIBRARY_CONTEXT,
        sharedLibrarySession: FirSession,
        sessionProvider: FirProjectSessionProvider,
        moduleDataProvider: ModuleDataProvider,
        languageVersionSettings: LanguageVersionSettings,
        extensionRegistrars: List<FirExtensionRegistrar>,
        createProviders: (FirSession, FirKotlinScopeProvider) -> List<FirSymbolProvider>
    ): FirSession {
        return FirCliSession(sessionProvider, FirSession.Kind.Library).apply session@{
            moduleDataProvider.allModuleData.forEach {
                sessionProvider.registerSession(it, this)
                it.bindSession(this)
            }

            registerCliCompilerOnlyComponents(languageVersionSettings)
            registerCommonComponents(languageVersionSettings)
            registerLibrarySessionComponents(context)
            register(FirBuiltinSyntheticFunctionInterfaceProvider::class, sharedLibrarySession.syntheticFunctionInterfacesSymbolProvider)

            val kotlinScopeProvider = createKotlinScopeProviderForLibrarySession()
            register(FirKotlinScopeProvider::class, kotlinScopeProvider)

            FirSessionConfigurator(this).apply {
                for (extensionRegistrar in extensionRegistrars) {
                    registerExtensions(extensionRegistrar.configure())
                }
            }.configure()
            registerCommonComponentsAfterExtensionsAreConfigured()

            val providers = createProviders(this, kotlinScopeProvider)
            register(
                StructuredProviders::class,
                StructuredProviders(
                    sourceProviders = emptyList(),
                    librariesProviders = providers,
                    sharedProvider = sharedLibrarySession.symbolProvider,
                )
            )

            val providersWithShared = providers + sharedLibrarySession.symbolProvider.flatten()

            val symbolProvider = FirCachingCompositeSymbolProvider(this, providersWithShared)
            register(FirSymbolProvider::class, symbolProvider)
            register(FirProvider::class, FirLibrarySessionProvider(symbolProvider))
        }
    }

    protected abstract fun createKotlinScopeProviderForLibrarySession(): FirKotlinScopeProvider
    protected abstract fun FirSession.registerLibrarySessionComponents(c: LIBRARY_CONTEXT)

    // ==================================== Platform session ====================================

    /**
     * Creates session for source files
     *
     * For more information see KDoc to [FirAbstractSessionFactory]
     */
    protected fun createSourceSession(
        moduleData: FirModuleData,
        context: SOURCE_CONTEXT,
        sessionProvider: FirProjectSessionProvider,
        extensionRegistrars: List<FirExtensionRegistrar>,
        configuration: CompilerConfiguration,
        init: FirSessionConfigurator.() -> Unit,
        createProviders: (
            FirSession, FirKotlinScopeProvider, FirSymbolProvider,
            FirSwitchableExtensionDeclarationsSymbolProvider?,
        ) -> SourceProviders
    ): FirSession {
        val languageVersionSettings = configuration.languageVersionSettings
        return FirCliSession(sessionProvider, FirSession.Kind.Source).apply session@{
            moduleData.bindSession(this@session)
            sessionProvider.registerSession(moduleData, this@session)
            registerModuleData(moduleData)
            registerCliCompilerOnlyComponents(languageVersionSettings)
            registerCommonComponents(languageVersionSettings)
            registerResolveComponents(
                configuration.lookupTracker,
                configuration.enumWhenTracker,
                configuration.importTracker
            )
            registerSourceSessionComponents(context)

            val kotlinScopeProvider = createKotlinScopeProviderForSourceSession(moduleData, languageVersionSettings)
            register(FirKotlinScopeProvider::class, kotlinScopeProvider)

            val firProvider = FirProviderImpl(this, kotlinScopeProvider)
            register(FirProvider::class, firProvider)

            FirSessionConfigurator(this).apply {
                registerCommonCheckers()
                registerPlatformCheckers(context)
                if (configuration.useFirExtraCheckers) {
                    registerExtraPlatformCheckers(context)
                }

                for (extensionRegistrar in extensionRegistrars) {
                    registerExtensions(extensionRegistrar.configure())
                }
                init()
            }.configure()
            registerCommonComponentsAfterExtensionsAreConfigured()

            val structuredDependencyProvidersWithoutSource = computeDependencyProviderList(moduleData)
            val generatedSymbolsProvider = FirSwitchableExtensionDeclarationsSymbolProvider.createIfNeeded(this)

            val (sourceProviders, additionalOptionalAnnotationsProvider) = createProviders(
                this,
                kotlinScopeProvider,
                firProvider.symbolProvider,
                generatedSymbolsProvider,
            )

            val structuredProvidersForModule = StructuredProviders(
                sourceProviders = sourceProviders,
                librariesProviders = structuredDependencyProvidersWithoutSource.librariesProviders.plusIfNotNull(additionalOptionalAnnotationsProvider),
                sharedProvider = structuredDependencyProvidersWithoutSource.sharedProvider,
            ).also {
                register(StructuredProviders::class, it)
            }

            val providersListWithoutSources = buildList {
                structuredProvidersForModule.librariesProviders.flatMapTo(this) { it.flatten() }
                addAll(structuredProvidersForModule.sharedProvider.flatten())
            }.distinct()

            val providersList = structuredProvidersForModule.sourceProviders + providersListWithoutSources

            register(
                FirSymbolProvider::class,
                FirCachingCompositeSymbolProvider(
                    this, providersList,
                    expectedCachesToBeCleanedOnce = generatedSymbolsProvider != null
                )
            )

            generatedSymbolsProvider?.let { register(FirSwitchableExtensionDeclarationsSymbolProvider::class, it) }
            register(
                DEPENDENCIES_SYMBOL_PROVIDER_QUALIFIED_KEY,
                FirCachingCompositeSymbolProvider(this, providersListWithoutSources)
            )
        }
    }

    protected data class SourceProviders(
        val sourceProviders: List<FirSymbolProvider>,
        val additionalOptionalAnnotationsProvider: FirSymbolProvider? = null,
    )

    protected abstract fun createKotlinScopeProviderForSourceSession(
        moduleData: FirModuleData, languageVersionSettings: LanguageVersionSettings
    ): FirKotlinScopeProvider

    protected abstract fun FirSessionConfigurator.registerPlatformCheckers(c: SOURCE_CONTEXT)
    protected abstract fun FirSessionConfigurator.registerExtraPlatformCheckers(c: SOURCE_CONTEXT)
    protected abstract fun FirSession.registerSourceSessionComponents(c: SOURCE_CONTEXT)

    // ==================================== Common parts ====================================

    // ==================================== Utilities ====================================

    private fun computeDependencyProviderList(moduleData: FirModuleData): StructuredProviders {
        // dependsOnDependencies can actualize declarations from their dependencies. Because actual declarations can be more specific
        // (e.g. have additional supertypes), the modules must be ordered from most specific (i.e. actual) to most generic (i.e. expect)
        // to prevent false positive resolution errors (see KT-57369 for an example).

        val providersFromDependencies = (moduleData.dependencies + moduleData.friendDependencies + moduleData.allDependsOnDependencies)
            .sortedBy { it.session.kind }
            .map { it to it.session.structuredProviders }

        val dependencyProviders = providersFromDependencies.flatMap { (dependencyModuleData, providers) ->
            when (dependencyModuleData.session.kind) {
                FirSession.Kind.Library -> providers.librariesProviders.also { check(providers.sourceProviders.isEmpty()) }

                // Dependency providers for common and platform modules are basically the same, so there is no need in duplicating them.
                FirSession.Kind.Source -> providers.sourceProviders
            }
        }

        // Here we are looking for the first binary dependency to support source-source dependency between
        // regular modules.
        // TODO(KT-75896): Such dependencies might occur only in old tests, which are not migrated to CLI facades yet.
        val sharedProvider = providersFromDependencies
            .first { (moduleData, _) -> moduleData.session.kind == FirSession.Kind.Library }
            .second.sharedProvider

        return StructuredProviders(
            sourceProviders = emptyList(),
            dependencyProviders,
            sharedProvider
        )
    }

    /* It eliminates dependency and composite providers since the current dependency provider is composite in fact.
    *  To prevent duplications and resolving errors, library or source providers from other modules should be filtered out during flattening.
    *  It depends on the session's kind of the top-level provider */
    protected fun FirSymbolProvider.flatten(): List<FirSymbolProvider> {
        val originalSession = session.takeIf { it.kind == FirSession.Kind.Source }
        val result = mutableListOf<FirSymbolProvider>()

        fun FirSymbolProvider.collectProviders() {
            when {
                // When provider is composite, unwrap all contained providers and recurse.
                this is FirCachingCompositeSymbolProvider -> {
                    for (provider in providers) {
                        provider.collectProviders()
                    }
                }

                // Make sure only source symbol providers from the same session as the original symbol provider are flattened. A composite
                // symbol provider can contain source symbol providers from multiple sessions that may represent dependency symbol providers
                // which should not be propagated transitively.
                originalSession != null && session.kind == FirSession.Kind.Source && session == originalSession ||
                        originalSession == null && session.kind == FirSession.Kind.Library -> {
                    result.add(this)
                }
            }
        }

        collectProviders()

        return result
    }
}
