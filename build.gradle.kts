// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        maven { setUrl("https://maven-other.tuya.com/repository/maven-releases/") }
        maven { setUrl("https://maven-other.tuya.com/repository/maven-commercial-releases/") }
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven {
            setUrl("https://maven.google.com")
        }
        maven {
            setUrl("https://jitpack.io")
        }
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
        classpath(Deps.ClassPath.hiltPlugin)
        classpath(Deps.ClassPath.arouterPlugin)
        classpath(Deps.ClassPath.googleService)
        classpath(Deps.ClassPath.uploadPlugin)
        // classpath("io.github.leavesczy:trace:0.0.3")
    }
}

allprojects {
    configurations.all {
        exclude(group = "com.thingclips.smart", module = "thingsmart-modularCampAnno")
    }
}

subprojects {

}

task("clean", Delete::class) {
    delete = setOf(rootProject.buildDir)
}