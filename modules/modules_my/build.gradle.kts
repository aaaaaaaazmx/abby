
plugins {
    if(IsModules.isModulesControl) id("com.android.library") else id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id ("dagger.hilt.android.plugin")
}

android {
    compileSdk = Version.compileSdk
    resourcePrefix = "my_"

    defaultConfig {
        minSdk = Version.minSdk
        targetSdk = Version.targetSdk
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

    sourceSets {
        if (IsModules.isModulesControl) {
            getByName("main").manifest.srcFile("src/main/module/AndroidManifest.xml")
        }
//        else {
//            getByName("main").manifest.srcFile("src/main/module/AndroidManifest.xml")
//            getByName("main").java.exclude("*module")
//        }
    }

    viewBinding.isEnabled = true
    dataBinding.isEnabled = true
}

kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", project.name)
    }
    generateStubs = true
}

dependencies {
    // 路由
    kapt(Deps.arouterKapt)
    implementation(project(mapOf("path" to ":common:common_base")))

    implementation(Deps.hiltAndroid)
    kapt(Deps.kaptHiltAndroidCompiler)
    kapt(Deps.kaptHiltCompiler)
}