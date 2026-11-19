
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "rs.appsearch.simple"
    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    defaultConfig {
        applicationId = "rs.appsearch.simple"
        minSdk = 23
        targetSdk = 35
        versionCode = 3
        versionName = "2.0"
    }

    val testKeystorePath = System.getenv("APPSEARCH_TEST_KEYSTORE_PATH")
    if (!testKeystorePath.isNullOrBlank()) {
        signingConfigs {
            create("testStable") {
                storeFile = file(testKeystorePath)
                storePassword = System.getenv("APPSEARCH_TEST_STORE_PASSWORD")
                    ?: error("Missing APPSEARCH_TEST_STORE_PASSWORD")
                keyAlias = System.getenv("APPSEARCH_TEST_KEY_ALIAS")
                    ?: error("Missing APPSEARCH_TEST_KEY_ALIAS")
                keyPassword = System.getenv("APPSEARCH_TEST_KEY_PASSWORD")
                    ?: error("Missing APPSEARCH_TEST_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-TEST"
            if (!testKeystorePath.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("testStable")
            }
        }
    }
}


dependencies {
    val roomVersion = "2.7.2"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
