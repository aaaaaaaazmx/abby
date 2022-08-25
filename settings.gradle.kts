pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            setUrl("https://jitpack.io")
        }
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven {
            setUrl("https://maven-other.tuya.com/repository/maven-releases/")
        }
        maven {
            setUrl("https://maven.google.com")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            setUrl("https://jitpack.io")
        }
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven {
            setUrl("https://maven-other.tuya.com/repository/maven-releases/")
        }
    }
}
rootProject.name = "abby"
include(":app")
include(":modules:modules_home")
include(":modules:modules_login")
include(":modules:modules_contact")
include(":modules:modules_my")
include(":common:common_service")
include(":common:common_base")
include(":common:BarcodeScanning")
include(":modules:modules_pairing_connection")
