@Suppress("DSL_SCOPE_VIOLATION") // Remove when fixed https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "omg.lol.pastebin.core.ui"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.androidMinSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
        aidl = false
        buildConfig = false
        renderScript = false
        shaders = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidxComposeCompiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core-model"))

    // Core Android dependencies
    implementation(libs.androidx.core.ktx)

    // Compose
    with(libs.androidx.compose) {
        val composeBom = platform(bom)
        implementation(composeBom)
        androidTestImplementation(composeBom)
        implementation(ui)
        implementation(ui.tooling.preview)
        implementation(material3)
    }

    // Kotlinx
    implementation(libs.kotlinx.serialization.json)

    // Tooling
    debugImplementation(libs.androidx.compose.ui.tooling)
}
