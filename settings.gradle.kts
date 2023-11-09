pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Pastebin"

include(":app")
include(":core-model")
include(":core-l10n")
include(":core-util")
include(":core-platform")
include(":core-data")
include(":core-database")
include(":core-network")
include(":core-testing")
include(":core-ui")
include(":feature-paste")
include(":feature-login")
include(":test-app")
