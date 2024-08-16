import java.io.DataInputStream
import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    //    id("kotlin-parcelize")
        id("center.uploadpgy.plugin")
    // id("io.github.leavesczy.trace")
}
apply("thingMapping.gradle")

/*viewClickTrace {
    onClickClass = "com.cl.abby.click.ViewClickMonitor"
    onClickMethodName = "onClick"
    uncheckViewOnClickAnnotation = "com.cl.common_base.click.UncheckViewOnClick"
    include = setOf()
    exclude = setOf()
}*/

uploadPgyParams {
     apiKey = readProperties("PgyApiKey")
    //暂时无用
    appName = "TestGradlePlugin"
    buildTypeName = "Release"
    buildInstallType = 1
    buildPassword = "zx"
}

buildFeiShuParams {
     webHookHostUrl = readProperties("FeiShuWebHookHostUrl")
    contentTitle = "测试包"
    contentText = "最新开发测试包已经上传至蒲公英, 可以下载使用了"
    //富文本消息（post）、消息卡片（interactive），默认post
    msgtype = "post"
    //是否@全体群人员，默认false：isAtAll = true
    isAtAll = true
    clickTxt = "点击进行下载"
    //是否单独支持发送Git记录数据，在配置了buildGitLogParams前提下有效，默认为true
    isSupportGitLog = true
}

buildGitLogParams {
    //是否发送消息是携带Git记录日志，如果配置了这块参数才会携带Git记录，消息里面可以单独设置是否携带Git日志数据

    //获取以当前时间为基准至N天之前的Git记录（限定时间范围），不填或小于等于0为全部记录，会结合数量进行获取
    gitLogHistoryDayTime = 1
    //显示Git记录的最大数量，值范围1~50，不填默认是10条，最大数量50条
    gitLogMaxCount = 10
}


android {
    compileSdk = Version.compileSdk

    /*dexOptions {
        preDexLibraries = false
    }*/

    kapt {
        generateStubs = true
        correctErrorTypes = true // 这有助于更好地诊断错误
    }

    signingConfigs {
        create("abby") {
            keyAlias = readProperties("KEY_ALIAS")
            keyPassword = readProperties("KEY_PASSWORD")
            storeFile = readProperties("KEYSTORE_FILE")?.let { file(it) }
            storePassword = readProperties("KEYSTORE_PASSWORD")
        }
    }

    defaultConfig {
        applicationId = Version.applicationId
        minSdk = Version.minSdk
        targetSdk = Version.targetSdk
        versionCode = Version.versionCode
        versionName = Version.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // multiDexEnabled = true

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

        // IPC
        pickFirst("lib/*/libc++_shared.so")
        pickFirst("lib/*/libyuv.so")
        pickFirst("lib/*/libopenh264.so")

        // ffmpeg
        pickFirst("lib/*/libavcodec.so")
        pickFirst("lib/*/libavfilter.so")
        pickFirst("lib/*/libavformat.so")
        pickFirst("lib/*/libavutil.so")
        pickFirst("lib/*/libswresample.so")
        pickFirst("lib/*/libswscale.so")
        pickFirst("lib/*/libpostproc.so")


        // 报错的直接排除
        exclude("lib/*/liblog.so")
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
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
            /*isDebuggable = true*/
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    sourceSets {
        getByName("main"){
            jniLibs.srcDirs("libs")
        }
    }

    viewBinding.isEnabled = true
    dataBinding.isEnabled = true
    buildFeatures.dataBinding = true

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
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar", "*.aar")))
    implementation(project(mapOf("path" to ":common:common_base")))
//    implementation(project(mapOf("path" to ":common:common_service")))
    implementation(project(mapOf("path" to ":modules:modules_home")))
    implementation(project(mapOf("path" to ":modules:modules_login")))
    implementation(project(mapOf("path" to ":modules:modules_contact")))
    implementation(project(mapOf("path" to ":modules:modules_my")))
    implementation(project(mapOf("path" to ":modules:modules_pairing_connection")))
    implementation(project(mapOf("path" to ":modules:modules_planting_log")))


    // 路由
    kapt(Deps.arouterKapt)

    // hilt
    implementation(Deps.hiltAndroid)
    kapt(Deps.kaptHiltAndroidCompiler)
    kapt(Deps.kaptHiltCompiler)
}

fun readProperties(key: String): String? {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        val inputStream = DataInputStream(FileInputStream(file))
        val properties = Properties()
        properties.load(inputStream)
        if (properties.containsKey(key)) {
            return properties.getProperty(key)
        }
    }
    return ""
}