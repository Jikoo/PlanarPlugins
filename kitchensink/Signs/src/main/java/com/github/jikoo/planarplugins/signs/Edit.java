package com.github.jikoo.planarplugins.signs;

import com.github.jikoo.planarplugins.Signs;
import com.github.jikoo.planarplugins.signs.arguments.SideArgument;
import com.github.jikoo.planarplugins.signs.lined.SenderLined;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class Edit {

  private static final String CMD_LINE = "line";
  private static final String CMD_CONTENT = "content";
  private static final String CMD_SIDE = "side";
  private static final String CMD_WORLD = "world";
  private static final String CMD_POS = "position";

  // /sign edit <pos> <side> <line> for console?
  // /sign edit <line>

  public static CommandNode<CommandSourceStack> command() {
    return Commands.literal("edit")
        // edit <line> <content>
        .then(Commands.argument(CMD_LINE, IntegerArgumentType.integer(1, SenderLined.SIGN_LINE_COUNT))
            .then(Commands.argument(CMD_CONTENT, StringArgumentType.greedyString())
                .executes(ctx -> edit(ctx, true))
                .requires(stack -> stack.getExecutor() instanceof Player && stack.getSender().hasPermission("planarplugins.command.sign.edit"))
            )
        )
        // edit <world> <pos> <side> <line> <content>
        .then(Commands.argument(CMD_WORLD, ArgumentTypes.world())
            .then(Commands.argument(CMD_POS, ArgumentTypes.blockPosition())
                .then(Commands.argument(CMD_SIDE, new SideArgument())
                    .then(Commands.argument(CMD_LINE, IntegerArgumentType.integer(1, SenderLined.SIGN_LINE_COUNT))
                        .then(Commands.argument(CMD_CONTENT, StringArgumentType.greedyString())
                            .executes(ctx -> edit(ctx, false))
                            .requires(stack -> !(stack.getSender() instanceof Player) && stack.getSender().hasPermission("planarplugins.command.sign.edit"))
                        )
                    )
                )
            )
        )
        .build();
  }

  private static int edit(
      CommandContext<CommandSourceStack> context,
      boolean raytraceTarget
  ) throws CommandSyntaxException {
    Block target = getTarget(context, raytraceTarget);

    if (target == null || !Tag.ALL_SIGNS.isTagged(target.getType())) {
      context.getSource().getSender().sendMessage("That's not a sign!");
      return 0;
    }

    BlockState state = target.getState();

    if (!(state instanceof Sign sign)) {
      context.getSource().getSender().sendMessage("That's not a sign!");
      return 0;
    }

    SignSide side = getSide(context, sign, raytraceTarget);

    if (side == null) {
      return 0;
    }

    String text = context.getArgument(CMD_CONTENT, String.class);
    Component content = Signs.getComponent(context.getSource().getSender(), text);
    int index = context.getArgument(CMD_LINE, int.class) - 1;

    Component replaced = side.line(index);
    side.line(index, content);

    if (raytraceTarget) {
      // TODO fire event and check results
    }

    sign.update();

    Component message = Component.text().content("Replaced").append(
        Component.text().content(" \"").append(replaced).append(Component.text("\""))
            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, ClickEvent.Payload.string(MiniMessage.miniMessage().serialize(replaced))))
            .hoverEvent(HoverEvent.showText(Component.text("Click to copy!")))
    ).build();
    context.getSource().getSender().sendMessage(message);
    return Command.SINGLE_SUCCESS;
  }

  private static @Nullable SignSide getSide(CommandContext<CommandSourceStack> context, Sign sign, boolean raytraceTarget) {
    if (raytraceTarget) {
      if (!(context.getSource().getExecutor() instanceof Player player)) {
        context.getSource().getSender().sendMessage("Target is not a player!");
        return null;
      }
      return sign.getTargetSide(player);
    }
    return sign.getSide(context.getArgument(CMD_SIDE, Side.class));
  }

  private static @Nullable Block getTarget(
      CommandContext<CommandSourceStack> context,
      boolean raytraceTarget
  ) throws CommandSyntaxException {
    if (raytraceTarget) {
      if (!(context.getSource().getExecutor() instanceof Player player)) {
        context.getSource().getSender().sendMessage("Target is not a player!");
        return null;
      }

      return player.getTargetBlockExact(6, FluidCollisionMode.NEVER);
    }
    World world = context.getArgument(CMD_WORLD, World.class);
    BlockPositionResolver resolver = context.getArgument(CMD_POS, BlockPositionResolver.class);
    BlockPosition position = resolver.resolve(context.getSource());
    return world.getBlockAt(position.blockX(), position.blockY(), position.blockZ());
  }

  private Edit() {}

}
