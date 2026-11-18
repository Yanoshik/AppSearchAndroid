
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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
        versionCode = 2
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
