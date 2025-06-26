plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mailyapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mailyapp"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Room dependencies
    implementation ("androidx.room:room-runtime:2.6.1")
    implementation(libs.adapters)
    annotationProcessor ("androidx.room:room-compiler:2.6.1")

    // LiveData
    implementation ("androidx.lifecycle:lifecycle-livedata:2.6.2")

    // Retrofit core
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")

    // Retrofit + Gson
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")


    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}