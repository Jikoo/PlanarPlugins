rootProject.name = "planarplugins"

val plugins = listOf(
  "BottleExperience",
  "ColorSignText",
  "FortuneShears",
  "FreeCart",
  "WitherFacts",
)

for (plugin in plugins) {
  val lowerName = plugin.lowercase()
  include(lowerName)
  val proj = project(":$lowerName")
  proj.name = plugin
  proj.projectDir = file("kitchensink/$plugin")
}
