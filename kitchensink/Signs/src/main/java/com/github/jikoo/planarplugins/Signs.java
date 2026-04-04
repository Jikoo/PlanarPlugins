package com.github.jikoo.planarplugins;

import com.github.jikoo.planarplugins.signs.Auto;
import com.github.jikoo.planarplugins.signs.CopyPaste;
import com.github.jikoo.planarplugins.signs.Edit;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public class Signs extends JavaPlugin {

  @Override
  public void onEnable() {
    Auto auto = new Auto(this);
    getServer().getPluginManager().registerEvents(auto, this);

    CopyPaste clipboard = new CopyPaste();
    getServer().getPluginManager().registerEvents(clipboard, this);

    LiteralCommandNode<CommandSourceStack> sign = Commands.literal("sign")
        .then(auto.command())
        .then(Edit.command())
        .then(clipboard.command())
        .requires(stack -> stack.getSender().hasPermission("planarplugins.command.sign"))
        .build();

    LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
    manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
      final Commands commands = event.registrar();
      commands.register(sign, "Modify a sign.");
    });
  }

  public static Component getComponent(CommandSender sender, String line) {
    if (sender.hasPermission("planarplugins.signs.minimessage.raw")) {
      return MiniMessage.miniMessage().deserialize(line);
    } else if (sender.hasPermission("planarplugins.signs.minimessage.filtered")) {
      return stripClickEvents(MiniMessage.miniMessage().deserialize(line));
    } else if (sender.hasPermission("planarplugins.signs.ampersand")) {
      return LegacyComponentSerializer.legacyAmpersand().deserialize(line);
    }
    return Component.text(line);
  }

  private static Component stripClickEvents(Component component) {
    // If there's a click event, remove it.
    if (component.clickEvent() != null) {
      component = component.clickEvent(null);
    }

    List<Component> children = component.children();

    // If there are no children, this component is fully processed.
    if (children.isEmpty()) {
      return component;
    }

    ArrayList<Component> newChildren = new ArrayList<>();
    for (int i = 0; i < children.size(); ++i) {
      // Recursively strip clicks from children.
      newChildren.add(i, stripClickEvents(children.get(i)));
    }

    return component.children(newChildren);
  }

}
