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

        maven { setUrl("https://maven-other.tuya.com/repository/maven-commercial-releases/") }
        /*maven { setUrl("https://central.maven.org/maven2/") }*/
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
        /*maven { setUrl("https://developer.huawei.com/repo/") }*/
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
        maven { setUrl("https://maven-other.tuya.com/repository/maven-commercial-releases/") }
        /*maven { setUrl("https://central.maven.org/maven2/") }*/
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
        /*maven { setUrl("https://developer.huawei.com/repo/") }*/
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
include(":common:ble")
include(":common:ipc")
include(":modules:modules_pairing_connection")
include(":modules:modules_planting_log")
include(":common:mylibrary")
include(":modules:modules_planting_log")
