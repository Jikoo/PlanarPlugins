plugins {
  `java-library`
  alias(libs.plugins.shadow) apply false
  alias(libs.plugins.errorprone.gradle)
}

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
}

java.disableAutoTargetJvm()

subprojects {
  apply(plugin = "java-library")
  apply(plugin = "net.ltgt.errorprone")

  repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io/")
  }

  dependencies {
    compileOnly(rootProject.libs.jspecify)
    compileOnly(rootProject.libs.jetbrains.annotations)
    compileOnly(rootProject.libs.paper.api)

    errorprone(rootProject.libs.errorprone.core)
  }

  java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
  }

  tasks.withType<JavaCompile>().configureEach {
    options.release = 21
    options.encoding = Charsets.UTF_8.name()
    options.isFork = true
  }
  tasks.withType<Javadoc>().configureEach {
    options.encoding = Charsets.UTF_8.name()
  }

  tasks.withType<ProcessResources>().configureEach {
    filteringCharset = Charsets.UTF_8.name()

    var apiVersion = rootProject.libs.paper.api.get().version!!
    var end = apiVersion.indexOf('-')
    if (end >= 0) {
      apiVersion = apiVersion.substring(0, apiVersion.indexOf('-'))
    }

    expand(
      "name" to project.name,
      "version" to version,
      "apiVersion" to apiVersion,
    )
  }

  tasks.withType<Jar>().configureEach {
    manifest.attributes("paperweight-mappings-namespace" to "mojang")
  }

  tasks.register<Delete>("removePreviousDistribution") {
    delete(rootProject.layout.projectDirectory.dir("dist").files("${project.name}-.*\\.jar"))
  }

  tasks.register<Copy>("distribute") {
    dependsOn("removePreviousDistribution")

    var shadow: Task? = tasks.findByName("shadowJar")
    if (shadow != null && shadow is Jar) {
      from(shadow)
    } else {
      from(tasks.jar)
    }

    into(rootProject.layout.projectDirectory.dir("dist").toString())
  }

  tasks.build {
    dependsOn("distribute")
  }

}
