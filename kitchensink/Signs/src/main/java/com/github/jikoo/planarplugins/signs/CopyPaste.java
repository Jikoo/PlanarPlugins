package com.github.jikoo.planarplugins.signs;

import com.github.jikoo.planarplugins.signs.arguments.EnumArgument;
import com.google.errorprone.annotations.Keep;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NullMarked
public final class CopyPaste implements Listener {

  private static final String CMD_COPY = "copy";
  private static final String CMD_CLOBBER = "clobber";
  private static final String CMD_CLEAR = "clear";
  private static final String CMD_SIDES = "sides";

  private final Map<UUID, Clipboard> clipboards;

  public CopyPaste() {
    clipboards = new HashMap<>();
  }

  public CommandNode<CommandSourceStack> command() {
    // TODO perms + player req
    return Commands.literal("clipboard")
        .requires(stack -> stack.getExecutor() instanceof Player && stack.getSender().hasPermission("planarplugins.command.sign.clipboard"))
        // clipboard copy
        .then(Commands.literal(CMD_COPY)
            .executes(ctx -> copy(ctx, true, Sides.WHEREVER_YOU_ARE))
            // clipboard copy <clobber>
            .then(Commands.argument(CMD_CLOBBER, BoolArgumentType.bool())
                .executes(ctx -> copy(ctx, ctx.getArgument(CMD_CLOBBER, boolean.class), Sides.WHEREVER_YOU_ARE))
                // clipboard copy <clobber> <sides>
                .then(Commands.argument(CMD_SIDES, new EnumArgument<>(Sides.class))
                    .executes(ctx -> copy(ctx, ctx.getArgument(CMD_CLOBBER, boolean.class), ctx.getArgument(CMD_SIDES, Sides.class)))
                )
            )
            // clipboard copy <sides>
            .then(Commands.argument(CMD_SIDES, new EnumArgument<>(Sides.class))
                .executes(ctx -> copy(ctx, true, ctx.getArgument(CMD_SIDES, Sides.class)))
                .then(Commands.argument(CMD_CLOBBER, BoolArgumentType.bool())
                    .executes(ctx -> copy(ctx, ctx.getArgument(CMD_CLOBBER, boolean.class), ctx.getArgument(CMD_SIDES, Sides.class)))
                )
            )
        )
        .then(Commands.literal(CMD_CLEAR).executes(ctx -> {
          if (ctx.getSource().getExecutor() instanceof Player player) {
            clipboards.remove(player.getUniqueId());
          }
          ctx.getSource().getSender().sendMessage("Clipboard cleared!");
          return Command.SINGLE_SUCCESS;
        }))

        // TODO
        //  clipboard show?
        //  clipboard edit?
        .build();
  }

  @EventHandler
  @Keep
  private void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK
        || event.getClickedBlock() == null
        || !Tag.ALL_SIGNS.isTagged(event.getClickedBlock().getType())) {
      return;
    }

    Player player = event.getPlayer();
    Clipboard clipboard = clipboards.get(player.getUniqueId());

    if (clipboard == null || !(event.getClickedBlock().getState() instanceof Sign sign)) {
      return;
    }

    Side near = sign.getInteractableSideFor(player);
    Side far = near == Side.FRONT ? Side.BACK : Side.FRONT;

    apply(sign, near, clipboard.near());
    apply(sign, far, clipboard.far());

    // TODO event

    player.sendRichMessage("<yellow>Pasta!</yellow> <dark_aqua>/sign clipboard clear</dark_aqua> to stop!");
    sign.update();
  }

  private void apply(Sign sign, Side side, @Nullable Component[] clipboard) {
    SignSide signSide = sign.getSide(side);
    int signSize = signSide.lines().size();
    for (int i = 0; i < clipboard.length && i < signSize; ++i) {
      Component line = clipboard[i];
      if (line != null) {
        signSide.line(i, line);
      }
    }
  }

  private int copy(CommandContext<CommandSourceStack> context, boolean clobber, Sides sides) {
    if (!(context.getSource().getExecutor() instanceof Player player)) {
      context.getSource().getSender().sendMessage("Target is not a player!");
      return 0;
    }

    Block block = player.getTargetBlockExact(6, FluidCollisionMode.NEVER);

    if (block == null || !Tag.ALL_SIGNS.isTagged(block.getType()) || !(block instanceof Sign sign)) {
      context.getSource().getSender().sendMessage("Must target a sign!");
      return 0;
    }

    Side near = sign.getInteractableSideFor(player);
    @Nullable Component[] nearLines;
    if (sides == Sides.FAR) {
      nearLines = new Component[]{};
    } else {
      nearLines = clobberFilter(sign.getSide(near).lines().toArray(new Component[0]), clobber);
    }

    Side far = near == Side.FRONT ? Side.BACK : Side.FRONT;
    @Nullable Component[] farLines;
    if (sides == Sides.NEAR) {
      farLines = new Component[]{};
    } else {
      farLines = clobberFilter(sign.getSide(far).lines().toArray(new Component[0]), clobber);
    }

    clipboards.put(player.getUniqueId(), new Clipboard(nearLines, farLines));
    context.getSource().getSender().sendMessage("Copied!");
    return Command.SINGLE_SUCCESS;
  }

  private @Nullable Component[] clobberFilter(@Nullable Component[] content, boolean clobber) {
    if (clobber) {
      return content;
    }

    for (int i = content.length - 1; i >= 0; --i) {
      if (!Component.IS_NOT_EMPTY.test(content[i])) {
        content[i] = null;
      }
    }

    return content;
  }

  private enum Sides {
    NEAR,
    FAR,
    WHEREVER_YOU_ARE // I believe that my heart will go on.
  }

  private record Clipboard(@Nullable Component[] near, @Nullable Component[] far) {}

}
