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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    viewBinding.isEnabled = true
    dataBinding.isEnabled = true

    /*repositories {
        flatDir {
            dirs("libs")
        }
    }*/

    hilt {
        enableExperimentalClasspathAggregation = true
        enableAggregatingTask = false
    }
}


kapt {
    correctErrorTypes = true // 这有助于更好地诊断错误
    generateStubs = true
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
    api("com.google.firebase:protolite-well-known-types:18.0.0")
    // Import the BoM for the Firebase platform
    api(platform("com.google.firebase:firebase-bom:30.5.0"))
    api("com.google.firebase:firebase-auth-ktx")
    /*{
        exclude("play-services-safetynet")
    }*/
    api("com.google.android.gms:play-services-auth:20.5.0")
    // 启动
    api("androidx.core:core-splashscreen:1.0.0-beta02")
    // bugly
    api(Deps.bugly)
    // todo 添加第三方依赖的时候，一定要注意不混淆下面依赖的类！！！！！！！ 不然正式环境会找不到混淆的类！
    api(project(mapOf("path" to ":common:BarcodeScanning")))
    /*api(project(mapOf("path" to ":common:kefu-easeui")))*/
    api(project(mapOf("path" to ":common:ipc")))
    // api(project(mapOf("path" to ":common:mylibrary")))
    api(project(mapOf("path" to ":common:ble")))
    api(project(mapOf("path" to ":common:indicatorseekbar")))
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
    // JWT解析
    implementation("com.auth0.android:jwtdecode:2.0.2")

    api("com.github.Ferfalk:SimpleSearchView:0.2.1")

    // api(Deps.chat)
    api("com.github.AAChartModel:AAChartCore-Kotlin:7.1.0")
    // switchButton
    api("com.github.iielse:switchbutton:1.0.4")
    // workManager
    api(Deps.workManager)
    // multidex 需要适配bugly
    // api(Deps.multidex)
}