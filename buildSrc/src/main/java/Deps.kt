object Version {
    object ClassPathVersion {
        const val kotlinVersion = "1.7.0"
        const val hiltPluginVersion = "2.51.1"
        const val hiltCompilerVersion = "1.0.0"
        const val arouterPluginVersion = "1.0.2"
        const val googleServicePluginVersion = "4.3.8"
    }

    object ComposeVersion {
        const val composeVersion = "1.1.1"
        const val composeMaterial3Version = "1.0.0-alpha14"
    }

    const val compileSdk = 34
    const val applicationId = "com.cl.abby"
    const val minSdk = 26
    const val targetSdk = 34
    val versionCode = 69
    const val versionName = "3.9.4"

    const val coreKtxVersion = "1.9.0"
    const val appCompatVersion = "1.6.0"
    const val fragment = "1.7.1"
    const val activity = "1.7.1"
    const val materialVersion = "1.12.0"
    const val constraintLayoutVersion = "2.1.4"
    const val lifecycleVersion = "2.7.0"
    const val navigationVersion = "2.7.7"
    const val swipeRefreshLayoutVersion = "1.1.0"
    const val recyclerViewVersion = "1.3.0-alpha02"
    const val preferenceVersion = "1.2.0"

    // paging3
    const val pagingVersion = "3.1.1"

    // 网络
    const val retrofitVersion = "2.11.0"
    const val okHttp3Version = "4.12.0"
    const val gsonVersion = "2.11.0"
    const val frescoVersion = "3.2.0"

    const val testJunitVersion = "4.13.2"
    const val androidTestJunitAndroidExt = "1.1.3"
    const val androidTestEspressoCore = "3.4.0"

    // ui
    const val bannerVersion = "2.2.2"
    const val flexboxVersion = "3.0.0"

    const val dataStoreVersion = "1.1.0"

    const val kotlinSerialVersion = "1.7.1"

    const val leakcanaryVersion = "2.9.1"

    const val arouterVersion = "1.5.2"

    const val loggerVersion = "2.2.0"

    const val mmkv = "1.3.7"

    const val rvHelp = "3.0.10"

    const val xpopup = "2.10.0"

    const val web = "v5.0.0-alpha.1"

    const val appStartup = "1.1.1"

    const val glide = "4.16.0"

    const val permission = "1.8.0"

    const val choosePhoto = "v3.11.2"

    const val choosePhotoCamera = "v3.11.2"

    const val choosePhotoComporess = "v3.11.2"

    const val epMedia = "v1.0.1"

    const val luban = "1.1.8"

    const val tuya = "5.14.0"

    const val tuyaIpc = "5.14.1"

    const val jPushChina = "5.4.0"
    const val push = "5.4.0"
    const val fcm = "5.4.0"
    const val firBaseMessgae = "23.1.2"
    const val stepView = "1.0.1"
    const val cardSwip = "1.0.0"

    const val lottieVersion = "6.4.1"

    const val videoPlayer = "v8.6.0"

    const val interCome = "15.7.1"

    const val chart = "v3.1.0"

    const val workManager = "2.9.0"

    const val multiDex = "2.0.1"

    const val bugly = "4.1.9.3"
}

object Deps {

    object DebugDependency {
        const val debugLeakCanary =
            "com.squareup.leakcanary:leakcanary-android:${Version.leakcanaryVersion}"
    }

    object ClassPath {
        const val hiltPlugin =
            "com.google.dagger:hilt-android-gradle-plugin:${Version.ClassPathVersion.hiltPluginVersion}"

        const val arouterPlugin =
            "com.alibaba:arouter-register:${Version.ClassPathVersion.arouterPluginVersion}"

        const val googleService = "com.google.gms:google-services:${Version.ClassPathVersion.googleServicePluginVersion}"

        // 打包
        const val uploadPlugin = "com.github.centerzx:UploadApkPlugin:v1.0.6"
    }

    object ComposeDependency {
        const val composeActivity = "androidx.activity:activity-compose:${Version.activity}"
        const val composeUI = "androidx.compose.ui:ui:${Version.ComposeVersion.composeVersion}"
        const val composeTool =
            "androidx.compose.ui:ui-tooling:${Version.ComposeVersion.composeVersion}"
        const val composeFoundation =
            "androidx.compose.foundation:foundation:${Version.ComposeVersion.composeVersion}"
        const val composeMaterial3 =
            "androidx.compose.material3:material3:${Version.ComposeVersion.composeMaterial3Version}"
        const val composeMaterial3Window =
            "androidx.compose.material3:material3-window-size-class:${Version.ComposeVersion.composeMaterial3Version}"
    }

    const val coreKtx = "androidx.core:core-ktx:${Version.coreKtxVersion}"
    const val appcompat = "androidx.appcompat:appcompat:${Version.appCompatVersion}"
    const val activity = "androidx.fragment:fragment-ktx:${Version.fragment}"
    const val fragment = "androidx.activity:activity-ktx:${Version.activity}"
    const val material = "com.google.android.material:material:${Version.materialVersion}"
    const val constraintlayout =
        "androidx.constraintlayout:constraintlayout:${Version.constraintLayoutVersion}"
    const val lifecycleLiveDataKtx =
        "androidx.lifecycle:lifecycle-livedata-ktx:${Version.lifecycleVersion}"
    const val lifecycleViewModelKtx =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${Version.lifecycleVersion}"
    const val lifecucleRuntimeKtx =
        "androidx.lifecycle:lifecycle-runtime-ktx:${Version.lifecycleVersion}"
    const val navigationFragmentKtx =
        "androidx.navigation:navigation-fragment-ktx:${Version.navigationVersion}"
    const val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:${Version.navigationVersion}"
    const val swiperefreshlayout =
        "androidx.swiperefreshlayout:swiperefreshlayout:${Version.swipeRefreshLayoutVersion}"
    const val recyclerview = "androidx.recyclerview:recyclerview:${Version.recyclerViewVersion}"
    const val preferences = "androidx.preference:preference:${Version.preferenceVersion}"
    const val testJunit = "junit:junit:${Version.testJunitVersion}"
    const val androidTestJunit = "androidx.test.ext:junit:${Version.androidTestJunitAndroidExt}"
    const val androidTestEspresso =
        "androidx.test.espresso:espresso-core:${Version.androidTestEspressoCore}"

    const val okhttp = "com.squareup.okhttp3:okhttp:${Version.okHttp3Version}"
    const val okhttpLoggingInterceptor =
        "com.squareup.okhttp3:logging-interceptor:${Version.okHttp3Version}"
    const val retrofit = "com.squareup.retrofit2:retrofit:${Version.retrofitVersion}"
    const val retrofitGsonConverter =
        "com.squareup.retrofit2:converter-gson:${Version.retrofitVersion}"
    const val gson = "com.google.code.gson:gson:${Version.gsonVersion}"
    const val banner = "io.github.youth5201314:banner:${Version.bannerVersion}"
    const val flexbox = "com.google.android.flexbox:flexbox:${Version.flexboxVersion}"
    const val paging = "androidx.paging:paging-runtime:${Version.pagingVersion}"
    const val pagingKtx = "androidx.paging:paging-runtime-ktx:${Version.pagingVersion}"
    const val dataStore = "androidx.datastore:datastore-preferences:${Version.dataStoreVersion}"
    const val hiltAndroid =
        "com.google.dagger:hilt-android:${Version.ClassPathVersion.hiltPluginVersion}"
    const val kotlinSerial =
        "org.jetbrains.kotlinx:kotlinx-serialization-json:${Version.kotlinSerialVersion}"
    const val kaptHiltAndroidCompiler =
        "com.google.dagger:hilt-android-compiler:${Version.ClassPathVersion.hiltPluginVersion}"
    const val kaptHiltCompiler =
        "androidx.hilt:hilt-compiler:${Version.ClassPathVersion.hiltCompilerVersion}"

    // 路由
    const val arouter = "com.alibaba:arouter-api:${Version.arouterVersion}"
    const val arouterKapt = "com.alibaba:arouter-compiler:${Version.arouterVersion}"

    // logger 日志
    const val logger = "com.orhanobut:logger:${Version.loggerVersion}"

    // mmkv
    const val mmkv = "com.tencent:mmkv:${Version.mmkv}"

    // Rvhelp
    const val rvHelp = "com.github.CymChad:BaseRecyclerViewAdapterHelper:${Version.rvHelp}"

    // xpopup
    const val xpopUp = "com.github.li-xiaojun:XPopup:${Version.xpopup}"

    // web
    const val web = "com.github.Justson.AgentWeb:agentweb-core:${Version.web}-androidx"

    // startUp
    const val startUp = "androidx.startup:startup-runtime:${Version.appStartup}"

    //Glide
    const val glide = "com.github.bumptech.glide:glide:${Version.glide}"
    const val glideKapt = "com.github.bumptech.glide:compiler:${Version.glide}"

    // 权限
    const val permission = "com.guolindev.permissionx:permissionx:${Version.permission}"

    // chooserPhoto
    const val choosePhoto = "io.github.lucksiege:pictureselector:${Version.choosePhoto}"
    // camera
    const val choosePhotoCamera =  "io.github.lucksiege:camerax:${Version.choosePhotoCamera}"
    // 压缩
    const val choosePhotoComproress =  "io.github.lucksiege:compress:${Version.choosePhotoComporess}"

    // ffmpeg
    const val epMedia = "com.github.yangjie10930:EpMedia:${Version.epMedia}"

    //luban
    const val luban = "top.zibin:Luban:${Version.luban}"

    // 涂鸦
    const val tuya = "com.thingclips.smart:thingsmart:${Version.tuya}"

    const val tuyaIpc = "com.thingclips.smart:thingsmart-ipcsdk:${Version.tuyaIpc}"

    // fastJson for tuya
    const val fastJson = "com.alibaba:fastjson:1.1.67.android"
    const val okHttpUrl = "com.squareup.okhttp3:okhttp-urlconnection:3.14.9"


    // cardSwipe
    const val cardSwipe = "me.yuqirong:cardswipelayout:${Version.cardSwip}"
    const val stepView = "com.joketng:TimeLineStepView:${Version.stepView}"

    // Jpush
    const val firBaseMessage = "com.google.firebase:firebase-messaging:${Version.firBaseMessgae}"
    const val fcm = "cn.jiguang.sdk.plugin:fcm:${Version.fcm}"
    const val jpushGoogle = "cn.jiguang.sdk:jpush-google:${Version.push}"

    // lottie
    const val lottie = "com.airbnb.android:lottie:${Version.lottieVersion}"

    // VideoPlay
    const val videoPlayer = "com.github.CarGuo.GSYVideoPlayer:GSYVideoPlayer:${Version.videoPlayer}-release-jitpack"

    // interCome
    const val interCome = "io.intercom.android:intercom-sdk:${Version.interCome}"

    // mpachart
    const val chat = "com.github.AAChartModel:AAChartCore-Kotlin:-SNAPSHOT"

    // workMagner
    const val workManager = "androidx.work:work-runtime-ktx:${Version.workManager}"

    // 合并文件
    const val multidex = "androidx.multidex:multidex:${Version.multiDex}"

    // bugly
    const val bugly = "com.tencent.bugly:crashreport:${Version.bugly}"
}