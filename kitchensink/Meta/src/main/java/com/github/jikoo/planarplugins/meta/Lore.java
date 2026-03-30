package com.github.jikoo.planarplugins.meta;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public final class Lore {

  private static final String INDEX = "index";
  private static final String CONTENT = "content";

  private enum IndexSource {
    PARAM,
    SIZE
  }

  private enum ListModifier {
    ADD,
    SET,
    DELETE
  }

  public static LiteralCommandNode<CommandSourceStack> command() {
    return Commands.literal("lore")
        .then(Commands.literal("add")
            .then(Commands.argument(CONTENT, StringArgumentType.greedyString())
                .executes(ctx -> modifyList(ctx, IndexSource.SIZE, ListModifier.ADD))))
        .then(Commands.literal("set")
            .then(Commands.argument(INDEX, IntegerArgumentType.integer(1, 255))
                .then(Commands.argument(CONTENT, StringArgumentType.greedyString())
                    .executes(ctx -> modifyList(ctx, IndexSource.PARAM, ListModifier.SET)))))
        .then(Commands.literal("insert")
            .then(Commands.argument(INDEX, IntegerArgumentType.integer(1, 255))
                .then(Commands.argument(CONTENT, StringArgumentType.greedyString())
                    .executes(ctx -> modifyList(ctx, IndexSource.PARAM, ListModifier.ADD)))))
        .then(Commands.literal("delete")
            .then(Commands.argument(INDEX, IntegerArgumentType.integer(1, 255))
                .executes(ctx -> modifyList(ctx, IndexSource.PARAM, ListModifier.DELETE))))
        .requires(stack -> stack.getSender().hasPermission("planarplugins.command.meta"))
        .build();
  }

  private static int modifyList(CommandContext<CommandSourceStack> context, IndexSource source, ListModifier mod) {
    if (!(context.getSource().getExecutor() instanceof Player player)) {
      context.getSource().getSender().sendMessage("Target is not a player!");
      return 0;
    }

    ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();

    if (meta == null) {
      context.getSource().getSender().sendMessage("Item in hand does not have meta!");
      return 0;
    }

    Component content = switch (mod) {
      case DELETE -> Component.empty();
      case SET, ADD -> MiniMessage.miniMessage().deserialize(context.getArgument(CONTENT, String.class));
    };

    List<Component> lore = new ArrayList<>();
    List<Component> existing = meta.lore();
    if (existing != null) {
      lore.addAll(existing);
    }

    int index = switch (source) {
      case SIZE -> lore.size();
      case PARAM -> context.getArgument(INDEX, Integer.class) - 1;
    };

    if (mod == ListModifier.SET && index == lore.size()) {
      // If the index is the final one, do add op rather than error.
      mod = ListModifier.ADD;
    }

    if (index < 0
        || index > lore.size()
        || (index == lore.size() && mod != ListModifier.ADD)
    ) {
      context.getSource().getSender().sendMessage("Index out of bounds! There are only " + lore.size() + " lines!");
      return 0;
    }

    switch (mod) {
      case ADD -> {
        lore.add(index, content);
        context.getSource().getSender().sendMessage("Line added!");
      }
      case SET -> {
        lore.set(index, content);
        context.getSource().getSender().sendMessage("Line changed!");
      }
      case DELETE -> {
        lore.remove(index);
        context.getSource().getSender().sendMessage("Line removed!");
      }
    }
    meta.lore(lore);
    player.getInventory().getItemInMainHand().setItemMeta(meta);

    return Command.SINGLE_SUCCESS;
  }

  private Lore() {}

}
