package com.github.jikoo.planarplugins;

import com.github.jikoo.planarplugins.meta.Book;
import com.github.jikoo.planarplugins.meta.Head;
import com.github.jikoo.planarplugins.meta.Lore;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Meta extends JavaPlugin {

  @Override
  public void onEnable() {
    LiteralCommandNode<CommandSourceStack> lore = Lore.command();
    CommandNode<CommandSourceStack> book = Book.command();
    CommandNode<CommandSourceStack> head = Head.command();

    LiteralCommandNode<CommandSourceStack> meta = Commands.literal("meta")
        .then(lore)
        .then(book)
        .then(head)
        .then(Commands.literal("name")
            .then(Commands.argument("name", StringArgumentType.greedyString())
                .executes(this::name)))
        .then(Commands.literal("reset")
            .then(Commands.argument("type", ArgumentTypes.resource(RegistryKey.DATA_COMPONENT_TYPE))
                .executes(this::resetSingle))
            .executes(this::reset))
        .requires(stack -> stack.getSender().hasPermission("planarplugins.command.meta"))
        .build();

    LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
    manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
      final Commands commands = event.registrar();
      commands.register(meta, "Modify an item's meta");
      commands.register(lore, "Modify an item's lore");
    });
  }

  private int name(CommandContext<CommandSourceStack> context) {
    if (!(context.getSource().getExecutor() instanceof Player player)) {
      context.getSource().getSender().sendMessage("Target is not a player!");
      return 0;
    }

    String name = context.getArgument("name", String.class);
    Component nameComponent = MiniMessage.miniMessage().deserialize(name);

    if (player.getInventory().getItemInMainHand().editMeta(ItemMeta.class, meta -> meta.customName(nameComponent))) {
      context.getSource().getSender().sendMessage("Name changed!");
      return Command.SINGLE_SUCCESS;
    }

    context.getSource().getSender().sendMessage("Item in hand does not have meta!");
    return 0;
  }

  private int reset(CommandContext<CommandSourceStack> context) {
    if (!(context.getSource().getExecutor() instanceof Player player)) {
      context.getSource().getSender().sendMessage("Target is not a player!");
      return 0;
    }

    ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
    ItemMeta meta = getServer().getItemFactory().getItemMeta(itemInMainHand.getType());
    itemInMainHand.setItemMeta(meta);

    context.getSource().getSender().sendMessage("Reset item meta!");
    return Command.SINGLE_SUCCESS;
  }

  private int resetSingle(CommandContext<CommandSourceStack> context) {
    if (!(context.getSource().getExecutor() instanceof Player player)) {
      context.getSource().getSender().sendMessage("Target is not a player!");
      return 0;
    }

    DataComponentType component = context.getArgument("type", DataComponentType.class);
    ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
    itemInMainHand.resetData(component);

    context.getSource().getSender().sendMessage("Reset " + component.key());
    return Command.SINGLE_SUCCESS;
  }

}
