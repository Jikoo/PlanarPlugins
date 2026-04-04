package com.github.jikoo.planarplugins.signs.lined;

import net.kyori.adventure.text.Component;
import org.bukkit.event.block.SignChangeEvent;

public record ChangeEventLined(SignChangeEvent event) implements Lined {

  @Override
  public int size() {
    return event.lines().size();
  }

  @Override
  public void line(int index, Component component) {
    event.line(index, component);
  }

}
