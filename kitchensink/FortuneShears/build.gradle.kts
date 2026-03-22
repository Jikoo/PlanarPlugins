plugins {
  alias(libs.plugins.shadow)
}

dependencies {
  implementation(libs.planarenchanting)
}

tasks.shadowJar {
  relocate("com.github.jikoo.planarenchanting", "com.github.jikoo.planarplugins.fortuneshears.lib.enchanting")
  minimize()
}
