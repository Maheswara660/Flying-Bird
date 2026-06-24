plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.maheswara660.flyingbird.android"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.maheswara660.flyingbird"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    splits {
        abi {
            isEnable = false
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // Platform dependencies for Koin and SQLDelight
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.sqldelight.android)
}
