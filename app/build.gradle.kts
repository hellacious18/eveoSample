plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.eveosample"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.eveosample"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.base)
    implementation(libs.play.services.base)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")

    implementation ("com.amplifyframework:aws-auth-cognito:2.24.0")

    implementation("com.amplifyframework.ui:authenticator:1.4.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.2.0")

    implementation("androidx.fragment:fragment-ktx:1.8.5")

    implementation ("com.google.android.gms:play-services-auth:20.7.0")

    implementation ("com.amazonaws:aws-android-sdk-auth-google:2.66.0")
    implementation ("com.amazonaws:aws-android-sdk-auth-core:2.66.0")
//    implementation ("com.amazonaws:aws-android-sdk-cognitoidentityprovider:2.66.0")

    implementation("androidx.credentials:credentials:1.5.0-rc01")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0-rc01")
//    implementation("com.google.android.libraries.identity.googleid:googleid:1.3.0")

    implementation("software.amazon.awssdk:aws-core:2.30.17")

    implementation("com.amazonaws:aws-android-sdk-rekognition:2.79.0")
    implementation("com.amazonaws:aws-android-sdk-mobile-client:2.79.0")
}