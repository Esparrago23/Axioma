import java.util.Properties
import java.io.FileInputStream
import org.gradle.authentication.http.BasicAuthentication

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

val localProperties = Properties().apply {
    val f = File(rootDir, "local.properties")
    if (f.exists()) load(FileInputStream(f))
}
val mapboxDownloadToken: String = localProperties.getProperty("MAPBOX_DOWNLOADS_TOKEN") ?: ""

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()

        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox"
                password = mapboxDownloadToken
            }
        }
    }


}

rootProject.name = "Axioma"
include(":app")