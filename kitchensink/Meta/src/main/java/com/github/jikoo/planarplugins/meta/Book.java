package com.github.jikoo.planarplugins.meta;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class Book {

  public static LiteralCommandNode<CommandSourceStack> command() {
    return Commands.literal("book")
        .then(Commands.literal("author")
            .then(Commands.argument("author", StringArgumentType.greedyString())
                .executes(Book::author)))
        .then(Commands.literal("title")
            .then(Commands.argument("title", StringArgumentType.greedyString())
                .executes(Book::title)))
        // TODO pages
        // TODO generation
        .requires(stack -> stack.getSender().hasPermission("planarplugins.command.meta"))
        .build();
  }

  private static int author(CommandContext<CommandSourceStack> context) {
    if (!(context.getSource().getExecutor() instanceof Player player)) {
      context.getSource().getSender().sendMessage("Target is not a player!");
      return 0;
    }

    String author = context.getArgument("author", String.class);
    Component authorComponent = MiniMessage.miniMessage().deserialize(author);

    if (player.getInventory().getItemInMainHand().editMeta(BookMeta.class, book -> book.author(authorComponent))) {
      context.getSource().getSender().sendMessage("Author changed!");
      return Command.SINGLE_SUCCESS;
    }

    context.getSource().getSender().sendMessage("Item in hand does not have book meta!");
    return 0;
  }

  private static int title(CommandContext<CommandSourceStack> context) {
    if (!(context.getSource().getExecutor() instanceof Player player)) {
      context.getSource().getSender().sendMessage("Target is not a player!");
      return 0;
    }

    String title = context.getArgument("title", String.class);
    Component titleComponent = MiniMessage.miniMessage().deserialize(title);

    if (player.getInventory().getItemInMainHand().editMeta(BookMeta.class, book -> book.title(titleComponent))) {
      context.getSource().getSender().sendMessage("Title changed!");
      return Command.SINGLE_SUCCESS;
    }

    context.getSource().getSender().sendMessage("Item in hand does not have book meta!");
    return 0;
  }

  private Book() {}

}
