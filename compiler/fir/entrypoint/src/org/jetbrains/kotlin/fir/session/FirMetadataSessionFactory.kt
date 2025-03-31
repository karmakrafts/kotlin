/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.session

import org.jetbrains.kotlin.config.AnalysisFlags
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.SessionConfiguration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.deserialization.ModuleDataProvider
import org.jetbrains.kotlin.fir.deserialization.SingleModuleDataProvider
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.java.FirProjectSessionProvider
import org.jetbrains.kotlin.fir.resolve.providers.impl.FirBuiltinSyntheticFunctionInterfaceProvider
import org.jetbrains.kotlin.fir.resolve.providers.impl.FirCloneableSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.impl.FirFallbackBuiltinSymbolProvider
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.session.environment.AbstractProjectEnvironment
import org.jetbrains.kotlin.fir.session.environment.AbstractProjectFileSearchScope
import org.jetbrains.kotlin.library.metadata.resolver.KotlinResolvedLibrary
import org.jetbrains.kotlin.load.kotlin.PackageAndMetadataPartProvider
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.KotlinMetadataFinder
import org.jetbrains.kotlin.utils.addToStdlib.runIf
import org.jetbrains.kotlin.utils.addToStdlib.runUnless

@OptIn(SessionConfiguration::class)
object FirMetadataSessionFactory : FirAbstractSessionFactory<Nothing?, Nothing?>() {

    // ==================================== Shared library session ====================================

    /**
     * See documentation to [FirAbstractSessionFactory.createSharedLibrarySession]
     */
    fun createSharedLibrarySession(
        mainModuleName: Name,
        sessionProvider: FirProjectSessionProvider,
        languageVersionSettings: LanguageVersionSettings,
        extensionRegistrars: List<FirExtensionRegistrar>,
    ): FirSession {
        return createSharedLibrarySession(
            mainModuleName,
            context = null,
            sessionProvider,
            languageVersionSettings,
            extensionRegistrars
        ) { session, moduleData, kotlinScopeProvider, syntheticFunctionInterfaceProvider ->
            listOfNotNull(
                syntheticFunctionInterfaceProvider,
                runUnless(languageVersionSettings.getFlag(AnalysisFlags.stdlibCompilation)) {
                    FirFallbackBuiltinSymbolProvider(session, moduleData, kotlinScopeProvider)
                },
                FirBuiltinSyntheticFunctionInterfaceProvider(session, moduleData, kotlinScopeProvider),
                FirCloneableSymbolProvider(session, moduleData, kotlinScopeProvider),
            )
        }
    }

    // ==================================== Library session ====================================

    /**
     * See documentation to [FirAbstractSessionFactory.createLibrarySession]
     */
    fun createLibrarySession(
        sessionProvider: FirProjectSessionProvider,
        sharedLibrarySession: FirSession,
        moduleDataProvider: ModuleDataProvider,
        projectEnvironment: AbstractProjectEnvironment,
        extensionRegistrars: List<FirExtensionRegistrar>,
        librariesScope: AbstractProjectFileSearchScope,
        resolvedKLibs: List<KotlinResolvedLibrary>,
        packageAndMetadataPartProvider: PackageAndMetadataPartProvider,
        languageVersionSettings: LanguageVersionSettings,
    ): FirSession {
        return createLibrarySession(
            context = null,
            sharedLibrarySession,
            sessionProvider,
            moduleDataProvider,
            languageVersionSettings,
            extensionRegistrars,
            createProviders = { session, kotlinScopeProvider ->
                listOfNotNull(
                    MetadataSymbolProvider(
                        session,
                        moduleDataProvider,
                        kotlinScopeProvider,
                        packageAndMetadataPartProvider,
                        projectEnvironment.getKotlinClassFinder(librariesScope)
                    ),
                    runIf(resolvedKLibs.isNotEmpty()) {
                        KlibBasedSymbolProvider(
                            session,
                            moduleDataProvider,
                            kotlinScopeProvider,
                            resolvedKLibs.map { it.library }
                        )
                    },
                )
            }
        )
    }

    override fun createKotlinScopeProviderForLibrarySession(): FirKotlinScopeProvider {
        return FirKotlinScopeProvider()
    }

    override fun FirSession.registerLibrarySessionComponents(c: Nothing?) {
        registerDefaultComponents()
    }

    // ==================================== Platform session ====================================

    /**
     * See documentation to [FirAbstractSessionFactory.createSourceSession]
     */
    fun createSourceSession(
        moduleData: FirModuleData,
        sessionProvider: FirProjectSessionProvider,
        projectEnvironment: AbstractProjectEnvironment,
        incrementalCompilationContext: IncrementalCompilationContext?,
        extensionRegistrars: List<FirExtensionRegistrar>,
        configuration: CompilerConfiguration,
        init: FirSessionConfigurator.() -> Unit = {}
    ): FirSession {
        return createSourceSession(
            moduleData,
            context = null,
            sessionProvider,
            extensionRegistrars,
            configuration,
            init,
            createProviders = { session, kotlinScopeProvider, symbolProvider, generatedSymbolsProvider ->
                var symbolProviderForBinariesFromIncrementalCompilation: MetadataSymbolProvider? = null
                incrementalCompilationContext?.let {
                    val precompiledBinariesPackagePartProvider = it.precompiledBinariesPackagePartProvider
                    if (precompiledBinariesPackagePartProvider != null && it.precompiledBinariesFileScope != null) {
                        val moduleDataProvider = SingleModuleDataProvider(moduleData)
                        symbolProviderForBinariesFromIncrementalCompilation =
                            MetadataSymbolProvider(
                                session,
                                moduleDataProvider,
                                kotlinScopeProvider,
                                precompiledBinariesPackagePartProvider as PackageAndMetadataPartProvider,
                                projectEnvironment.getKotlinClassFinder(it.precompiledBinariesFileScope) as KotlinMetadataFinder,
                                defaultDeserializationOrigin = FirDeclarationOrigin.Precompiled
                            )
                    }
                }

                SourceProviders(
                    listOfNotNull(
                        symbolProvider,
                        *(incrementalCompilationContext?.previousFirSessionsSymbolProviders?.toTypedArray() ?: emptyArray()),
                        symbolProviderForBinariesFromIncrementalCompilation,
                        generatedSymbolsProvider,
                    )
                )
            }
        )
    }

    override fun createKotlinScopeProviderForSourceSession(
        moduleData: FirModuleData,
        languageVersionSettings: LanguageVersionSettings,
    ): FirKotlinScopeProvider {
        return if (languageVersionSettings.getFlag(AnalysisFlags.stdlibCompilation)) {
            /**
             * For stdlib and builtin compilation, we don't want to hide @PlatformDependent declarations from the metadata
             */
            FirKotlinScopeProvider { _, declaredScope, _, _, _ -> declaredScope }
        } else {
            FirKotlinScopeProvider()
        }
    }

    override fun FirSessionConfigurator.registerPlatformCheckers(c: Nothing?) {}

    override fun FirSessionConfigurator.registerExtraPlatformCheckers(c: Nothing?) {}

    override fun FirSession.registerSourceSessionComponents(c: Nothing?) {
        registerDefaultComponents()
    }

    // ==================================== Common parts ====================================

    // ==================================== Utilities ====================================

}
