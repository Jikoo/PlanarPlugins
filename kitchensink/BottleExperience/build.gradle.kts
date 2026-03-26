plugins {
  alias(libs.plugins.shadow)
}

version = "1.0.0"

dependencies {
  implementation(libs.planarwrappers)
}

tasks.jar {
  enabled = false
}

tasks.shadowJar {
  dependsOn(tasks.classes)
  archiveClassifier.set("")

  relocate("com.github.jikoo.planarwrappers", "com.github.jikoo.planarplugins.bottleexp.lib.wrappers")
  minimize()
}

tasks.assemble {
  dependsOn(tasks.shadowJar)
}

artifacts {
  add("default", tasks.shadowJar)
}
