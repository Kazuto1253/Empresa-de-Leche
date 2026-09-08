plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ktor) apply false
}

// Both browser targets use Yarn's shared cache. Serialize their first installs so
// the Wasm lockfile exists before its lock verification task runs on Windows.
tasks.matching { it.name == "kotlinWasmNpmInstall" }.configureEach {
    mustRunAfter(tasks.matching { it.name == "kotlinNpmInstall" })
}
