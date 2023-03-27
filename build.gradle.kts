// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven {
            setUrl("https://jitpack.io")
        }
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
        classpath(Deps.ClassPath.hiltPlugin)
        classpath(Deps.ClassPath.arouterPlugin)
        classpath(Deps.ClassPath.googleService)
        classpath(Deps.ClassPath.uploadPlugin)
    }
}

subprojects {

}

task("clean", Delete::class) {
    delete = setOf(rootProject.buildDir)
}