plugins { id("com.android.application") }

android {
    namespace = "il.co.drivingscreenguard"
    compileSdk = 36
    defaultConfig {
        applicationId = "il.co.drivingscreenguard"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
}
dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("com.google.android.gms:play-services-location:21.3.0")
}
