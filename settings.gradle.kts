// settings.gradle.kts (Root Project)
pluginManagement {
    repositories {
        google() // Nécessaire pour les plugins Google/Firebase
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google() // Nécessaire pour les bibliothèques Google/Firebase
        mavenCentral()
    }
}
rootProject.name = "makeitmeme" // Votre nom de projet
include(":app") // Inclut votre module d'application