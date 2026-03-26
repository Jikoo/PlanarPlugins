plugins {
  alias(libs.plugins.shadow)
}

version = "1.0.0"

dependencies {
  implementation(libs.planarenchanting)
}

tasks.jar {
  enabled = false
}

tasks.shadowJar {
  dependsOn(tasks.classes)
  archiveClassifier.set("")

  relocate("com.github.jikoo.planarenchanting", "com.github.jikoo.planarplugins.fortuneshears.lib.enchanting")
  mergeServiceFiles()
}

tasks.assemble {
  dependsOn(tasks.shadowJar)
}

artifacts {
  add("default", tasks.shadowJar)
}
