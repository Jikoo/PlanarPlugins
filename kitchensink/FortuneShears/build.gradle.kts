plugins {
  alias(libs.plugins.shadow)
}

version = "1.0.0"

dependencies {
  implementation(libs.planarenchanting)
}

tasks.shadowJar {
  relocate("com.github.jikoo.planarenchanting", "com.github.jikoo.planarplugins.fortuneshears.lib.enchanting")
  mergeServiceFiles()
}
