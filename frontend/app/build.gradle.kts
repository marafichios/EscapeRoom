plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "1.9.22"
}

android {
    namespace = "com.ar.escaperoom"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ar.escaperoom"
        minSdk = 29
        targetSdk = 36
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //LOGIN
    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Unity as a Library — modulul exportat din Unity Editor (File > Build Settings > Export Project)
    // Copiază folderul unityLibrary/ lângă app/ după fiecare export Unity
    implementation(project(":unityLibrary"))

    // ── Necesare ca MyUnityActivity sa poata EXTINDE UnityPlayerGameActivity ──────────
    // Pentru a subclasa, compilatorul :app are nevoie de TOATA ierarhia de supertipuri,
    // pe care unityLibrary o expune doar ca 'implementation' (deci nu tranzitiv):
    //   - GameActivity            -> androidx.games:games-activity
    //   - AppCompatActivity       -> androidx.appcompat:appcompat
    //   - IUnityPlayer* + UnityPlayerForGameActivity -> unityLibrary/libs/unity-classes.jar
    // unity-classes.jar e adaugat ca 'compileOnly': clasele vin la runtime din :unityLibrary,
    // deci NU vrem sa le impachetam a doua oara (ar da "duplicate class").
    implementation("androidx.games:games-activity:3.0.5")
    implementation("androidx.appcompat:appcompat:1.6.1")
    compileOnly(files("../unityLibrary/libs/unity-classes.jar"))

    val ktor_version = "2.3.7"
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-okhttp:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-client-logging:$ktor_version")

    // Coil — async image loading in Compose
    implementation("io.coil-kt:coil-compose:2.5.0")

}