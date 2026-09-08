plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    js {
        browser { commonWebpackConfig { outputFileName = "ecolactea.js" } }
        binaries.executable()
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser { commonWebpackConfig { outputFileName = "ecolactea.js" } }
        binaries.executable()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":app:shared"))
            implementation(libs.compose.ui)
            implementation(libs.compose.runtime)
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
        }
    }
}
