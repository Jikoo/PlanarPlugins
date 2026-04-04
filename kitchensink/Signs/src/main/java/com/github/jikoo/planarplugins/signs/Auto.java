package com.github.jikoo.planarplugins.signs;

import com.github.jikoo.planarplugins.Signs;
import com.github.jikoo.planarplugins.signs.arguments.EnumArgument;
import com.github.jikoo.planarplugins.signs.lined.ChangeEventLined;
import com.github.jikoo.planarplugins.signs.lined.Lined;
import com.github.jikoo.planarplugins.signs.lined.SenderLined;
import com.github.jikoo.planarplugins.signs.lined.SideLined;
import com.google.errorprone.annotations.Keep;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

import java.util.Locale;

@NullMarked
public final class Auto implements Listener {

  private static final String CMD_SIDE = "side";
  private static final String CMD_LINE = "line";
  private static final String CMD_CONTENT = "content";

  private final Plugin plugin;
  private final NamespacedKey autoEditing;
  private final String autoLine = "auto_%s_line_%s";

  public Auto(Plugin plugin) {
    this.plugin = plugin;
    this.autoEditing = new NamespacedKey(plugin, "auto_editing");
  }

  @EventHandler
  @Keep
  private void onBlockPlace(BlockPlaceEvent event) {
    if (!Tag.ALL_SIGNS.isTagged(event.getBlock().getType())) {
      return;
    }

    BlockState state = event.getBlock().getState(false);

    if (!(state instanceof Sign sign)) {
      return;
    }

    boolean changed = false;
    for (Side side : Side.values()) {
      Lined lined = new SideLined(sign.getSide(side));
      changed |= applyAutoLines(event.getPlayer(), lined, side, false);
    }

    if (changed) {
      state.update();
    }
  }

  @EventHandler
  @Keep
  private void onSignOpen(PlayerOpenSignEvent event) {
    // Tag player as editing a fresh sign.
    // Note that we don't cancel the event because we want
    if (event.getCause() == PlayerOpenSignEvent.Cause.PLACE) {
      event.getPlayer().getPersistentDataContainer().set(autoEditing, PersistentDataType.BOOLEAN, true);
    }
  }

  @EventHandler
  @Keep
  private void onSignChange(SignChangeEvent event) {
    PersistentDataContainer persistentData = event.getPlayer().getPersistentDataContainer();

    if (!persistentData.has(autoEditing)) {
      return;
    }

    persistentData.remove(autoEditing);

    for (Component line : event.lines()) {
      if (Component.IS_NOT_EMPTY.test(line)) {
        return;
      }
    }

    applyAutoLines(event.getPlayer(), new ChangeEventLined(event), event.getSide(), false);
  }

  public CommandNode<CommandSourceStack> command() {
    return Commands.literal("auto")
        // auto show [side]
        .then(Commands.literal("show")
            .executes(ctx -> autoShow(ctx, Side.FRONT) + autoShow(ctx, Side.BACK))
            .then(Commands.argument(CMD_SIDE, new EnumArgument<>(Side.class))
                .executes(ctx -> autoShow(ctx, ctx.getArgument(CMD_SIDE, Side.class)))
            )
        )
        // auto clear [side] [line]
        .then(Commands.literal("clear")
            // auto clear
            .executes(ctx -> clearAutoSide(ctx, Side.FRONT) + clearAutoSide(ctx, Side.BACK))
            .then(Commands.argument(CMD_SIDE, new EnumArgument<>(Side.class))
                // auto clear <side>
                .executes(ctx -> clearAutoSide(ctx, ctx.getArgument(CMD_SIDE, Side.class)))
                // auto clear <side> <line>
                .then(Commands.argument(CMD_LINE, IntegerArgumentType.integer(1, SenderLined.SIGN_LINE_COUNT))
                    .executes(ctx -> clearAutoIndex(ctx, ctx.getArgument(CMD_SIDE, Side.class)))
                )
            )
            // auto clear <line>
            .then(Commands.argument(CMD_LINE, IntegerArgumentType.integer(1, SenderLined.SIGN_LINE_COUNT))
                .executes(ctx -> clearAutoIndex(ctx, Side.FRONT))
            )
        )
        .then(Commands.literal("set")
            // auto set <side> <line> <content>
            .then(Commands.argument(CMD_SIDE, new EnumArgument<>(Side.class))
                .then(Commands.argument(CMD_LINE, IntegerArgumentType.integer(1, SenderLined.SIGN_LINE_COUNT))
                    .executes(ctx -> setAuto(ctx, ctx.getArgument(CMD_SIDE, Side.class), ""))
                    .then(Commands.argument(CMD_CONTENT, StringArgumentType.greedyString())
                        .executes(ctx -> setAuto(ctx, ctx.getArgument(CMD_SIDE, Side.class)))
                    )
                )
            )
            // auto set <line> <content>
            .then(Commands.argument(CMD_LINE, IntegerArgumentType.integer(1, SenderLined.SIGN_LINE_COUNT))
                .executes(ctx -> setAuto(ctx, Side.FRONT, ""))
                .then(Commands.argument(CMD_CONTENT, StringArgumentType.greedyString())
                    .executes(ctx -> setAuto(ctx, Side.FRONT))
                )
            )
        )
        .requires(stack -> stack.getSender().hasPermission("planarplugins.command.sign.auto"))
        .build();
  }

  private int clearAutoSide(CommandContext<CommandSourceStack> context, Side side) {
    return clearAuto(context, side, -1);
  }

  private int clearAutoIndex(CommandContext<CommandSourceStack> context, Side side) {
    return clearAuto(context, side, context.getArgument(CMD_LINE, int.class));
  }

  private int clearAuto(CommandContext<CommandSourceStack> context, Side side, int index) {
    if (!(context.getSource().getExecutor() instanceof Player player)) {
      context.getSource().getSender().sendMessage("Executor is not a player!");
      return 0;
    }

    PersistentDataContainer dataContainer = player.getPersistentDataContainer();

    if (index == -1) {
      for (index = 0; index < SenderLined.SIGN_LINE_COUNT; ++index) {
        NamespacedKey autoLineKey = new NamespacedKey(plugin, String.format(autoLine, side.name().toLowerCase(Locale.ROOT), index));
        dataContainer.remove(autoLineKey);
      }
    } else {
      NamespacedKey autoLineKey = new NamespacedKey(plugin, String.format(autoLine, side.name().toLowerCase(Locale.ROOT), index));
      dataContainer.remove(autoLineKey);
    }

    return autoShow(context, side);
  }

  private int setAuto(CommandContext<CommandSourceStack> context, Side side) {
    return setAuto(context, side, context.getArgument(CMD_CONTENT, String.class));
  }

  private int setAuto(CommandContext<CommandSourceStack> context, Side side, String content) {
    if (!(context.getSource().getExecutor() instanceof Player player)) {
      context.getSource().getSender().sendMessage("Target is not a player!");
      return 0;
    }

    int index = context.getArgument(CMD_LINE, int.class) - 1;
    NamespacedKey autoLineKey = new NamespacedKey(plugin, String.format(autoLine, side.name().toLowerCase(Locale.ROOT), index));
    PersistentDataContainer dataContainer = player.getPersistentDataContainer();

    Component component = Signs.getComponent(context.getSource().getSender(), content);
    if (PlainTextComponentSerializer.plainText().serialize(component).length() > 50) {
      context.getSource().getSender().sendMessage("That's probably too much text to fit!");
      return 0;
    }

    // Note: we save the component as a MiniMessage string so the original sender's abilities are respected.
    dataContainer.set(autoLineKey, PersistentDataType.STRING, MiniMessage.miniMessage().serialize(component));
    return autoShow(context, side);
  }

  private int autoShow(CommandContext<CommandSourceStack> context, Side side) {
    if (!(context.getSource().getExecutor() instanceof Player player)) {
      context.getSource().getSender().sendMessage("Target is not a player!");
      return 0;
    }

    context.getSource().getSender().sendMessage("|==| Auto " + side.name().toLowerCase(Locale.ROOT) + " |==|");
    applyAutoLines(player, new SenderLined(context.getSource().getSender()), side, true);
    context.getSource().getSender().sendMessage("|==============|");

    return Command.SINGLE_SUCCESS;
  }

  private boolean applyAutoLines(Player player, Lined lined, Side side, boolean displayNull) {
    boolean changed = false;
    PersistentDataContainer persistentData = player.getPersistentDataContainer();

    int maxIndex = lined.size();
    for (int index = 0; index < maxIndex; ++index) {
      NamespacedKey autoLineKey = new NamespacedKey(plugin, String.format(autoLine, side.name().toLowerCase(Locale.ROOT), index));
      String autoLine = persistentData.get(autoLineKey, PersistentDataType.STRING);

      Component component;

      // No auto set for this line.
      if (autoLine == null) {
        if (index == 2) {
          // happy week of april 1, maybe
          // TODO calendar?
          component = Component.text("test");
          changed = true;
        } else if (displayNull) {
          component = Component.empty();
        } else {
          continue;
        }
      } else {
        // See above note about sender; setter's abilities are respected.
        component = MiniMessage.miniMessage().deserialize(autoLine);
        changed = true;
      }

      lined.line(index, component);
    }

    return changed;
  }

}
