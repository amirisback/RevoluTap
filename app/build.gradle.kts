plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}


android {
    compileSdk = ProjectSetting.PROJECT_COMPILE_SDK
    namespace = ProjectSetting.PROJECT_NAME_SPACE

    defaultConfig {
        applicationId = ProjectSetting.PROJECT_APP_ID
        minSdk = ProjectSetting.PROJECT_MIN_SDK
        targetSdk = ProjectSetting.PROJECT_TARGET_SDK
        versionCode = ProjectSetting.PROJECT_VERSION_CODE
        versionName = ProjectSetting.PROJECT_VERSION_NAME

        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Naming APK // AAB
        setProperty("archivesBaseName", "${ProjectSetting.NAME_APK}-${versionCode}")

        // Inject app name for debug
        resValue("string", "app_name", ProjectSetting.NAME_APP_DEBUG)

        // Inject admob id for debug
        resValue("string", "admob_app_id", ProjectAds.Admob.Debug.APP_ID)

        // Inject admob interstitial id for debug
        resValue("string", "admob_interstitial_id", ProjectAds.Admob.Debug.INTERSTITIAL_ID)


    }

    buildTypes {
        getByName("release") {
            isDebuggable = false
            isJniDebuggable = false
            isRenderscriptDebuggable = false
            isPseudoLocalesEnabled = false
            isMinifyEnabled = false

            /** Still not working

            // Enables code shrinking, obfuscation, and optimization for only your project's release build type.
            // isMinifyEnabled = true

            // Enables resource shrinking, which is performed by the Android Gradle plugin.
            // isShrinkResources = true

             **/

            // Inject app name for release
            resValue("string", "app_name", ProjectSetting.NAME_APP)

            // Inject admob id for release
            resValue("string", "admob_app_id", ProjectAds.Admob.APP_ID)

            // Inject admob interstitial id for release
            resValue("string", "admob_interstitial_id", ProjectAds.Admob.INTERSTITIAL_ID)

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of("17"))
        }
    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.annotation:annotation:1.6.0")
    implementation("androidx.preference:preference:1.2.0")
}