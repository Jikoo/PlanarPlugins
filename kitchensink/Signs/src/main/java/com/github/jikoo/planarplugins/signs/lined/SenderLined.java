package com.github.jikoo.planarplugins.signs.lined;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public record SenderLined(CommandSender sender) implements Lined {

  public static final int SIGN_LINE_COUNT = 4;

  @Override
  public int size() {
    return SIGN_LINE_COUNT;
  }

  @Override
  public void line(int index, Component component) {
    sender.sendMessage(component);
  }

}
