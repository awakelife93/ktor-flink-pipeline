rootProject.name = "data-pipeline"

include(":data-pipeline-api")
include(":data-pipeline-flink")

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
}

buildCache {
  local {
    isEnabled = true
  }
}