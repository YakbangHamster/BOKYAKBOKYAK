plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt") // 약 검색용
}

android {
    namespace = "com.example.yakbanghamster"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.yakbanghamster"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // --- 안드로이드X 기본 라이브러리 ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardview)
    implementation(libs.material)

    // --- 외부 라이브러리 ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // api 통신용
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")  // api 통신용
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1") // api 통신용
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.github.homayoonahmadi:CurveNavX:1.2.0") // 네비게이션
    implementation("com.google.android.flexbox:flexbox:3.0.0") // 약 검색
    implementation("com.google.mlkit:text-recognition:16.0.0-beta6") // OCR
    implementation("com.google.mlkit:text-recognition-korean:16.0.0-beta6") // OCR 한국어 인식
    implementation("com.github.bumptech.glide:glide:4.16.0") // 약 검색 사진
    kapt("com.github.bumptech.glide:compiler:4.16.0") // 약 검색 사진
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0") // 서버 응답값 자동 타입 변환
    implementation("com.github.prolificinteractive:material-calendarview:2.0.1") // 캘린더 라이브러리
    implementation ("com.jakewharton.threetenabp:threetenabp:1.4.5") // 캘린더 라이브러리 요일
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0") // 레포트 그래프 라이브러리

    // --- 테스트 ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
