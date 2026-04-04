package com.github.jikoo.planarplugins.signs.lined;

import net.kyori.adventure.text.Component;
import org.bukkit.block.sign.SignSide;

public record SideLined(SignSide signSide) implements Lined {

  @Override
  public int size() {
    return signSide.lines().size();
  }

  @Override
  public void line(int index, Component component) {
    signSide.line(index, component);
  }

}
