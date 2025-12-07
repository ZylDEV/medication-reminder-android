plugins {
    id("com.google.gms.google-services") version "4.4.0" // Ganti dengan versi terbaru yang sesuai
    id("com.android.application") version "8.3.1" // Ganti dengan versi AGP yang kamu gunakan
}

android {
    namespace = "com.example.pengingatobat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pengingatobat"
        minSdk = 28
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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-auth:23.2.0")
    implementation ("com.google.firebase:firebase-database:21.0.0")
    implementation ("com.itextpdf:itext7-core:7.1.15")
    




    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}