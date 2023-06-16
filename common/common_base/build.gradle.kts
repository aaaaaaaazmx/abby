plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = Version.compileSdk

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

    viewBinding.isEnabled = true
    dataBinding.isEnabled = true

    /*repositories {
        flatDir {
            dirs("libs")
        }
    }*/
}


kapt {
    arguments {
        arg("AROUTER_MODULE_NAME", project.name)
    }
}

dependencies {
    implementation(fileTree("libs").include("*.jar", "*.aar"))
    // 必需
    api(Deps.coreKtx)
    api(Deps.appcompat)
    api(Deps.material)
    api(Deps.constraintlayout)
    api(Deps.testJunit)
    api(Deps.androidTestEspresso)

    api(Deps.activity)
    api(Deps.fragment)
    api(Deps.lifecycleLiveDataKtx)
    api(Deps.lifecycleViewModelKtx)
    api(Deps.lifecucleRuntimeKtx)
//    api(Deps.navigationFragmentKtx)
//    api(Deps.navigationUiKtx)
    api(Deps.recyclerview)
//    api(Deps.dataStore)
    api(Deps.preferences)
    api(Deps.hiltAndroid)
    kapt(Deps.kaptHiltAndroidCompiler)
    kapt(Deps.kaptHiltCompiler)

    api(Deps.okhttp)
    api(Deps.okhttpLoggingInterceptor)
    api(Deps.retrofit)
    api(Deps.retrofitGsonConverter)
    api(Deps.gson)

//    api(Deps.kotlinSerial)
    debugImplementation(Deps.DebugDependency.debugLeakCanary)
    // 路由
    api(Deps.arouter)
    kapt(Deps.arouterKapt)
    // 日志
    api(Deps.logger)
    // 存储
    api(Deps.mmkv)
    // Rv
    api(Deps.rvHelp)
    // xpop
    api(Deps.xpopUp)
    // web
    api(Deps.web)
    // startUp
    api(Deps.startUp)
    // glide
    api(Deps.glide)
    kapt(Deps.glideKapt)
    // permisslion
    api(Deps.permission)
    // t涂鸦
    api(Deps.tuya)
    api(Deps.fastJson)
    api(Deps.okHttpUrl)
    // 探探卡片滑动布局
//    api(Deps.cardSwipe)
    api("lin.jerrylin0322.reswipecard:reswipecard:1.0.1")
    // 时间轴，整体就几个布局，不会占很大的空间
    api(Deps.stepView)
    // FCM推送通道
    api(Deps.firBaseMessage)
    api(Deps.fcm)
    api(Deps.jpushGoogle)
    api(Deps.jPushCodeGoogle)
    // Import the BoM for the Firebase platform
    api(platform("com.google.firebase:firebase-bom:30.5.0"))
    api("com.google.firebase:firebase-auth-ktx")
    api("com.google.android.gms:play-services-auth:20.5.0")
    // 启动
    api("androidx.core:core-splashscreen:1.0.0-beta02")
    // bugly
    api("com.tencent.bugly:crashreport:latest.release")
    api(project(mapOf("path" to ":common:BarcodeScanning")))
    /*api(project(mapOf("path" to ":common:kefu-easeui")))*/
    api(project(mapOf("path" to ":common:mylibrary")))
    // lottie
    api(Deps.lottie)
    // viewPager指示器
    api("com.github.hackware1993:MagicIndicator:1.7.0")
    // snapHelp
    api("com.github.rubensousa:gravitysnaphelper:2.2.2")
    api("com.github.limuyang2:LShadowLayout:1.0.3")

    // 图片选择
    api(Deps.choosePhoto)
    // 拍摄视频
    api(Deps.choosePhotoCamera)
    // 视频压缩
    // api(Deps.epMedia)
    // 视频播放
    api(Deps.videoPlayer)

    // interCome
    api(Deps.interCome)

    // refresh
    api("io.github.scwang90:refresh-layout-kernel:2.0.5")      //核心必须依赖
    api("io.github.scwang90:refresh-header-classics:2.0.5")//经典刷新头
    api("io.github.scwang90:refresh-footer-classics:2.0.5")    //经典加载

    // 轮播图
    api("io.github.youth5201314:banner:2.2.2")

    // ffmpeg
    api(Deps.epMedia)
    // 图片压缩
    api(Deps.choosePhotoComproress)
}