plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	kotlin("plugin.serialization") version "1.9.10"
}

android {
	namespace = "com.example.myfinances"
	compileSdk = 34

	defaultConfig {
		applicationId = "com.example.myfinances"
		minSdk = 23
		targetSdk = 34
		versionCode = 2
		versionName = "2.0"

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

	buildFeatures {
		viewBinding = true
	}
}

dependencies {
	implementation("io.coil-kt:coil:0.12.0")
	implementation("io.coil-kt:coil-svg:0.12.0")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
	implementation("androidx.core:core-ktx:1.13.1")
	implementation("androidx.appcompat:appcompat:1.7.0")
	implementation("com.google.android.material:material:1.12.0")
	implementation("androidx.constraintlayout:constraintlayout:2.1.4")
	implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
	implementation("com.squareup.okhttp3:okhttp:4.12.0")
	implementation("com.squareup.moshi:moshi:1.12.0")
	implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
    testImplementation("junit:junit:4.13.2")
	androidTestImplementation("androidx.test.ext:junit:1.1.5")
	androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

}