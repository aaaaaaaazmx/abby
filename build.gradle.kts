// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven {
            setUrl("https://jitpack.io")
        }
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
        classpath(Deps.ClassPath.hiltPlugin)
        classpath(Deps.ClassPath.arouterPlugin)
        classpath(Deps.ClassPath.googleService)
        classpath(Deps.ClassPath.uploadPlugin)
        // classpath("io.github.leavesczy:trace:0.0.3")
    }
}

subprojects {

}

task("clean", Delete::class) {
    delete = setOf(rootProject.buildDir)
}