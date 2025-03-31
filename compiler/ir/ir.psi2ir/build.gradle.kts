plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    api(project(":compiler:util"))
    api(project(":compiler:frontend"))
    api(project(":compiler:backend-common"))
    api(project(":compiler:ir.tree"))
    implementation(project(":compiler:ir.serialization.common"))
    compileOnly(intellijCore())
}

optInToUnsafeDuringIrConstructionAPI()
optInToObsoleteDescriptorBasedAPI()

sourceSets {
    "main" { projectDefault() }
    "test" {}
}
