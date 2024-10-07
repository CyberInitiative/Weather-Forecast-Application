plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.weather"
    compileSdk = 34

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.weather"
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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // https://mvnrepository.com/artifact/io.insert-koin/koin-android
    implementation("io.insert-koin:koin-android:3.5.6")
    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.10.1")
    // https://mvnrepository.com/artifact/com.squareup.retrofit2/retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // https://mvnrepository.com/artifact/com.squareup.retrofit2/converter-gson
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-android
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    // https://mvnrepository.com/artifact/com.google.android.gms/play-services-location
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("androidx.room:room-ktx:2.6.1")
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.work.testing)
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // https://mvnrepository.com/artifact/org.mockito/mockito-core
    testImplementation("org.mockito:mockito-core:5.4.0")
    // https://mvnrepository.com/artifact/org.mockito.kotlin/mockito-kotlin
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    // https://mvnrepository.com/artifact/org.mockito/mockito-android
    androidTestImplementation("org.mockito:mockito-android:5.4.0")

    // https://mvnrepository.com/artifact/org.hamcrest/hamcrest
    testImplementation("org.hamcrest:hamcrest:2.2")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}