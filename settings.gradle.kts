pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        ivy {
            name = "KotlinNative"
            setUrl("https://download.jetbrains.com/kotlin/native/builds/releases")
            patternLayout {
                artifact("[revision]/[classifier]/[artifact]-[classifier]-[revision].[ext]")
            }
            metadataSources {
                artifact()
            }
        }
    }
}

rootProject.name = "Sarah"
include(":app")
include(":shared")
