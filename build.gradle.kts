plugins {
  `java-library`
  alias(libs.plugins.shadow) apply false
}

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
}

java.disableAutoTargetJvm()

subprojects {
  apply(plugin = "java-library")

  repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io/")
  }

  dependencies {
    compileOnly(rootProject.libs.jspecify)
    compileOnly(rootProject.libs.jetbrains.annotations)
    compileOnly(rootProject.libs.paper.api)
  }

  tasks.withType<JavaCompile>().configureEach {
    options.release = 21
    options.encoding = Charsets.UTF_8.name()
    options.isFork = true
  }
  tasks.withType<Javadoc>().configureEach {
    options.encoding = Charsets.UTF_8.name()
  }

  val projectName = name;

  tasks.withType<ProcessResources>().configureEach {
    filteringCharset = Charsets.UTF_8.name()

    expand(
      "name" to projectName,
      "version" to version,
      "paper" to (rootProject.libs.paper.api.get().version ?: "1.21.11")
    )
  }

  tasks.withType<Jar>().configureEach {
    manifest.attributes("paperweight-mappings-namespace" to "mojang")
  }

}
