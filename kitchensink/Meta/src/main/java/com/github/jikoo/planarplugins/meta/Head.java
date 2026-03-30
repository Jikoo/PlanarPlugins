package com.github.jikoo.planarplugins.meta;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import org.jspecify.annotations.NullMarked;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@NullMarked
public final class Head {

  public static CommandNode<CommandSourceStack> command() {
    return Commands.literal("head")
        .then(Commands.literal("owner")
            .then(Commands.argument("name", StringArgumentType.word())
                .executes(Head::owner)))
        .then(Commands.literal("texture")
            .then(Commands.argument("texture", StringArgumentType.string())
                .executes(Head::texture)))
        .requires(stack -> stack.getSender().hasPermission("planarplugins.command.meta"))
        .build();
  }

  private static int owner(CommandContext<CommandSourceStack> context) {
    if (!(context.getSource().getExecutor() instanceof Player player)) {
      context.getSource().getSender().sendMessage("Target is not a player!");
      return 0;
    }

    String owner = context.getArgument("name", String.class);
    OfflinePlayer target = Bukkit.getOfflinePlayer(owner);

    if (player.getInventory().getItemInMainHand().editMeta(SkullMeta.class, skull -> skull.setOwningPlayer(target))) {
      context.getSource().getSender().sendMessage("Owner changed!");
      return Command.SINGLE_SUCCESS;
    }

    context.getSource().getSender().sendMessage("Item in hand does not have skull meta!");
    return 0;
  }

  private static int texture(CommandContext<CommandSourceStack> context) {
    if (!(context.getSource().getExecutor() instanceof Player player)) {
      context.getSource().getSender().sendMessage("Target is not a player!");
      return 0;
    }

    String name = "Test";
    PlayerProfile profile = Bukkit.getServer().createProfileExact(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), name);

    String rawTexture = context.getArgument("texture", String.class);

    if (rawTexture.contains("textures.minecraft.net/texture/")) {
      // Raw link provided. All good!
      if (textureUrlFailed(context, profile, rawTexture)) {
        return 0;
      }
    } else if (rawTexture.matches("^[A-Fa-f0-9]{64}$")) {
      // Raw texture hash provided. Prepend rest of URL.
      if (textureUrlFailed(context, profile, "https://textures.minecraft.net/texture/" + rawTexture)) {
        return 0;
      }
    } else if (rawTexture.matches("^[A-Za-z0-9+/_-]+={0,2}$")) { // TODO does Mojang use regular or URL-safe?
      // eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTgzOWMzNTY1ZjM2YzlkNmU1MmQ1NWExNzYwYjExYzIwNjA5NTMxNDNmZmU0ZmZlOWM4YjYwNmVlNGUzNjQ4ZiJ9fX0=
      profile.setProperty(new ProfileProperty("textures", rawTexture));
    } else {
      context.getSource().getSender().sendMessage("Unknown texture format!");
      return 0;
    }

    if (player.getInventory().getItemInMainHand().editMeta(SkullMeta.class, skull -> skull.setPlayerProfile(profile))) {
      return Command.SINGLE_SUCCESS;
    }

    context.getSource().getSender().sendMessage("Item in hand does not have skull meta!");
    return 0;
  }

  // https://textures.minecraft.net/texture/1839c3565f36c9d6e52d55a1760b11c2060953143ffe4ffe9c8b606ee4e3648f
  private static boolean textureUrlFailed(CommandContext<CommandSourceStack> context, PlayerProfile profile, String texture) {
    URL textureUrl;
    try {
      textureUrl = new URI(texture).toURL();
    } catch (MalformedURLException | URISyntaxException e) {
      context.getSource().getSender().sendMessage("Unable to parse skin URL from " + texture);
      return true;
    }
    PlayerTextures textures = profile.getTextures();
    textures.setSkin(textureUrl);
    profile.setTextures(textures);
    return false;
  }

  private Head() {}

}
