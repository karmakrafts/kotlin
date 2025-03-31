/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.frontend.fir

import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.deserialization.ModuleDataProvider
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.java.FirProjectSessionProvider
import org.jetbrains.kotlin.fir.session.FirSessionConfigurator
import org.jetbrains.kotlin.fir.session.FirWasmSessionFactory
import org.jetbrains.kotlin.library.metadata.resolver.KotlinResolvedLibrary
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.wasm.WasmTarget
import org.jetbrains.kotlin.test.model.DependencyRelation
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.configuration.WasmEnvironmentConfigurator
import org.jetbrains.kotlin.test.services.configuration.getKlibDependencies
import org.jetbrains.kotlin.wasm.config.WasmConfigurationKeys
import java.io.File

object TestFirWasmSessionFactory {
    fun createLibrarySession(
        mainModuleName: Name,
        sessionProvider: FirProjectSessionProvider,
        moduleDataProvider: ModuleDataProvider,
        module: TestModule,
        testServices: TestServices,
        configuration: CompilerConfiguration,
        extensionRegistrars: List<FirExtensionRegistrar>,
    ): FirSession {
        val target = configuration.get(WasmConfigurationKeys.WASM_TARGET, WasmTarget.JS)
        val resolvedLibraries = resolveLibraries(
            configuration = configuration,
            paths = getAllWasmDependenciesPaths(module, testServices, target)
        )

        val sharedLibrarySession = FirWasmSessionFactory.createSharedLibrarySession(
            mainModuleName,
            sessionProvider,
            configuration,
            extensionRegistrars
        )

        return FirWasmSessionFactory.createLibrarySession(
            resolvedLibraries.map { it.library },
            sessionProvider,
            sharedLibrarySession,
            moduleDataProvider,
            extensionRegistrars,
            configuration,
        )
    }

    fun createModuleBasedSession(
        mainModuleData: FirModuleData,
        sessionProvider: FirProjectSessionProvider,
        extensionRegistrars: List<FirExtensionRegistrar>,
        configuration: CompilerConfiguration,
        sessionConfigurator: FirSessionConfigurator.() -> Unit,
    ): FirSession =
        FirWasmSessionFactory.createSourceSession(
            mainModuleData,
            sessionProvider,
            extensionRegistrars,
            configuration,
            icData = null,
            init = sessionConfigurator
        )
}

fun resolveWasmLibraries(
    module: TestModule,
    testServices: TestServices,
    configuration: CompilerConfiguration
): List<KotlinResolvedLibrary> {
    val paths = getAllWasmDependenciesPaths(
        module = module,
        testServices = testServices,
        target = configuration.get(WasmConfigurationKeys.WASM_TARGET, WasmTarget.JS)
    )
    return resolveLibraries(configuration, paths)
}

fun getAllWasmDependenciesPaths(
    module: TestModule,
    testServices: TestServices,
    target: WasmTarget,
): List<String> {
    val (runtimeKlibsPaths, transitiveLibraries, friendLibraries) = getWasmDependencies(module, testServices, target)
    return runtimeKlibsPaths + transitiveLibraries.map { it.path } + friendLibraries.map { it.path }
}

fun getWasmDependencies(
    module: TestModule,
    testServices: TestServices,
    target: WasmTarget,
): Triple<List<String>, List<File>, List<File>> {
    val runtimeKlibsPaths = WasmEnvironmentConfigurator.getRuntimePathsForModule(target)
    val transitiveLibraries = getKlibDependencies(module, testServices, DependencyRelation.RegularDependency)
    val friendLibraries = getKlibDependencies(module, testServices, DependencyRelation.FriendDependency)
    return Triple(runtimeKlibsPaths, transitiveLibraries, friendLibraries)
}
