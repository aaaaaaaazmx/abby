plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = Version.compileSdk
    resourcePrefix = "service_"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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

    hilt {
        enableExperimentalClasspathAggregation = true
        enableAggregatingTask = false
    }
}

kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", project.name)
    }
    generateStubs = true
    correctErrorTypes = true // 这有助于更好地诊断错误
}

dependencies {
    // 路由
    kapt(Deps.arouterKapt)
    implementation(project(mapOf("path" to ":common:common_base")))

    implementation(Deps.hiltAndroid)
    kapt(Deps.kaptHiltAndroidCompiler)
    kapt(Deps.kaptHiltCompiler)
}