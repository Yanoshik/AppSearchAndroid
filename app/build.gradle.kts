plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "rs.appsearch.simple"
    compileSdk = 36

    defaultConfig {
        applicationId = "rs.appsearch.simple"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
