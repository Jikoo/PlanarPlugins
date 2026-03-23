package com.github.jikoo.planarplugins;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@NullMarked
public class ColorSignText extends JavaPlugin implements Listener {
  
  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
  }

  @EventHandler
  private void onSignChange(SignChangeEvent event) {
    for (int index = event.lines().size() - 1; index <= 0; --index) {
      event.line(index, translateCodes(event.line(index)));
    }
  }

  private Component translateCodes(Component component) {
    if (!Component.IS_NOT_EMPTY.test(component)
        || !(component instanceof TextComponent text)) {
      return component;
    }

    if (!text.children().isEmpty() || text.style() != Style.empty()) {
      // If component has further details or existing styling, ignore.
      return component;
    }

    return LegacyComponentSerializer.legacyAmpersand().deserialize(text.content());
  }

}
