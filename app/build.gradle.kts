plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    //    id("kotlin-parcelize")
}

android {
    compileSdk = Version.compileSdk

    kapt {
        generateStubs = true
    }

    signingConfigs {
        create("abby") {
            keyAlias = "abby.keystore"
            keyPassword = "BaypacClub666"
            storeFile = file("../abby.keystore")
            storePassword = "BaypacClub666"
        }
    }

    defaultConfig {
        applicationId = Version.applicationId
        minSdk = Version.minSdk
        targetSdk = Version.targetSdk
        versionCode = Version.versionCode
        versionName = Version.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            "armeabi-v7a"
            "arm64-v8a"
            "armeabi"
        }

        // 极光配置
        manifestPlaceholders["JPUSH_PKGNAME"] = Version.applicationId
        manifestPlaceholders["JPUSH_APPKEY"] = "eeb6646c16149a470f5264b3"
        manifestPlaceholders["JPUSH_CHANNEL"] = "developer-default"
    }


    packagingOptions {
        // tuya
        pickFirst("lib/*/libc++_shared.so") // 多个aar存在此so，需要选择第一个
        pickFirst("lib/*/libgnustl_shared.so")//业务包需要
        // jpush
        doNotStrip("*/mips/*.so")
        doNotStrip("*/mips64/*.so")
    }

    applicationVariants.all {
        outputs.all {
            (this as? com.android.build.gradle.internal.api.ApkVariantOutputImpl)?.outputFileName =
                "Design_Abby-${Version.versionName}-${name}.apk"
        }
    }


    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true // 移除无用的resource文件
            isZipAlignEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 签名
            signingConfig = signingConfigs.getByName("abby")
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false // 移除无用的resource文件
            proguardFiles.clear() // 不混淆
            // 签名
            signingConfig = signingConfigs.getByName("abby")
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
        getByName("main"){
            jniLibs.srcDirs("libs")
        }
    }

    viewBinding.isEnabled = true
    dataBinding.isEnabled = true
    buildFeatures.dataBinding = true
}

kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", project.name)
    }
    generateStubs = true
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar", "*.aar")))
    implementation(project(mapOf("path" to ":common:common_base")))
//    implementation(project(mapOf("path" to ":common:common_service")))
    implementation(project(mapOf("path" to ":modules:modules_home")))
    implementation(project(mapOf("path" to ":modules:modules_login")))
    implementation(project(mapOf("path" to ":modules:modules_contact")))
    implementation(project(mapOf("path" to ":modules:modules_my")))
    implementation(project(mapOf("path" to ":modules:modules_pairing_connection")))


    // 路由
    kapt(Deps.arouterKapt)

    // hilt
    implementation(Deps.hiltAndroid)
    kapt(Deps.kaptHiltAndroidCompiler)
    kapt(Deps.kaptHiltCompiler)
}