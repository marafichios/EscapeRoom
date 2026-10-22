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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Repository local pentru .aar-urile Unity din unityLibrary/libs/
        flatDir {
            dirs("unityLibrary/libs")
        }
    }
}

rootProject.name = "frontend_ar"
include(":app")

// Modulul Unity — copiat din UnityExport/unityLibrary după export din Unity Editor
include(":unityLibrary")
project(":unityLibrary").projectDir = file("unityLibrary")

// Submodul XR necesar de unityLibrary
include(":unityLibrary:xrmanifest.androidlib")
