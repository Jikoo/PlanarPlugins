package com.github.jikoo.planarplugins;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Coords extends JavaPlugin implements BasicCommand {

  @Override
  public void onEnable() {
    registerCommand("coords", this);
  }

  @Override
  public void execute(CommandSourceStack commandSourceStack, String[] args) {
    Entity executor = commandSourceStack.getExecutor();
    if (executor == null) {
      commandSourceStack.getSender().sendMessage("Who?");
      return;
    }

    Location location = executor.getLocation();
    String locString = String.format("%.1f, %.1f, %.1f", location.getX(), location.getY(), location.getZ());
    TextColor text = TextColor.color(255, 180, 50);
    TextColor value = TextColor.color(0, 255, 180);

    // On world at X, Y, Z with W pitch and V yaw
    TextComponent.Builder coordBuilder = Component.text().content("On ").color(text)
        .append(Component.text(executor.getWorld().getName()).color(value))
        .append(Component.text(" at ").color(text))
        .append(Component.text(locString).color(value))
        .append(Component.text(" with ").color(text))
        .append(Component.text(String.format("%.1f", location.getPitch())).color(value))
        .append(Component.text(" pitch and ").color(text))
        .append(Component.text(String.format("%.1f", location.getYaw())).color(value))
        .append(Component.text(" yaw").color(text));

    coordBuilder.clickEvent(
        ClickEvent.clickEvent(
            ClickEvent.Action.SUGGEST_COMMAND,
            ClickEvent.Payload.string(
                String.format(
                    "%s %.0f %.0f %.0f %.0f %.0f",
                    executor.getWorld().getName(),
                    location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch()
                )
            )
        )
    );

    commandSourceStack.getSender().sendMessage(coordBuilder.build());

  }

}
