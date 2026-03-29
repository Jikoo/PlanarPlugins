package com.github.jikoo.planarplugins;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class Meta extends JavaPlugin {

  @Override
  public void onEnable() {
    LiteralCommandNode<CommandSourceStack> command = Commands.literal("meta")
        .then(Commands.argument("author", StringArgumentType.greedyString()).executes(this::author))
        .then(Commands.argument("owner", StringArgumentType.word()).executes(this::owner))
        .build();
    // TODO permissions requirement

    LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
    manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
      final Commands commands = event.registrar();
      commands.register(
          command,
          "Modify an item's meta",
          List.of("lore")
      );
    });
  }

  private int author(CommandContext<CommandSourceStack> context) {
    if (!(context.getSource().getExecutor() instanceof Player player)) {
      context.getSource().getSender().sendMessage("Target is not a player!");
      return 0;
    }

    String author = context.getArgument("author", String.class);
    Component authorComponent = MiniMessage.miniMessage().deserialize(author);

    if (player.getInventory().getItemInMainHand().editMeta(BookMeta.class, book -> book.author(authorComponent))) {
      // TODO check if need update item
      context.getSource().getSender().sendMessage("Author changed!");
      return Command.SINGLE_SUCCESS;
    }

    System.out.println("Item in hand does not have book meta!");
    return 0;
  }

  private int owner(CommandContext<CommandSourceStack> context) {
    if (!(context.getSource().getExecutor() instanceof Player player)) {
      context.getSource().getSender().sendMessage("Target is not a player!");
      return 0;
    }

    String owner = context.getArgument("owner", String.class);
    OfflinePlayer target = Bukkit.getOfflinePlayer(owner);

    if (player.getInventory().getItemInMainHand().editMeta(SkullMeta.class, skull -> skull.setOwningPlayer(target))) {
      context.getSource().getSender().sendMessage("Owner changed!");
      return Command.SINGLE_SUCCESS;
    }

    context.getSource().getSender().sendMessage("Item in hand does not have skull meta!");
    return 0;
  }

}
