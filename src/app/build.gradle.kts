plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.locket"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.locket"
        minSdk = 24
        targetSdk = 34
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
            buildConfigField("String", "HUGGINGFACE_API_KEY", "\"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\"")
        }
        debug {
            buildConfigField("String", "HUGGINGFACE_API_KEY", "\"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\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.cameraxCore)
    implementation(libs.cameraxCamera2)
    implementation(libs.cameraxLifecycle)
    implementation(libs.cameraxView)

    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")

    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.arthenica:ffmpeg-kit-full:6.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}