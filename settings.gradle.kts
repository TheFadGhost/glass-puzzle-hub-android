pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Glass Puzzle Hub"
include(
    ":app",
    ":core-model",
    ":core-ui",
    ":core-storage",
    ":game-shikaku",
    ":game-sudoku",
    ":game-variety",
)
