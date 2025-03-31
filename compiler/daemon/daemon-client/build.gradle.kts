description = "Kotlin Daemon Client"

plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    api(kotlinStdlib())
    compileOnly(project(":daemon-common"))
    compileOnly(project(":js:js.config"))

    embedded(project(":daemon-common")) { isTransitive = false }
    testCompileOnly(project(":daemon-common"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

projectTest(jUnitMode = JUnitMode.JUnit5) {
    useJUnitPlatform()
}

configureKotlinCompileTasksGradleCompatibility()

publish()

runtimeJar()
sourcesJar()
javadocJar()
