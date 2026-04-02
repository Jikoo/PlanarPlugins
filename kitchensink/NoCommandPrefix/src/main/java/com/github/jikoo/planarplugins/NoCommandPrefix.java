package com.github.jikoo.planarplugins;

import com.google.errorprone.annotations.Keep;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class NoCommandPrefix extends JavaPlugin implements Listener {

  private static final String BYPASS_PERMISSION = "planarplugins.commands.unfiltered";

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
  }

  @EventHandler(ignoreCancelled = true)
  @Keep
  private void onPlayerCommand(PlayerCommandPreprocessEvent event) {
    int colon = event.getMessage().indexOf(':');
    int space = event.getMessage().indexOf(' ');
    if (!event.getPlayer().hasPermission(BYPASS_PERMISSION)
        && 0 < colon
        && (colon < space || space < 0)) {
      event.setMessage("/" + event.getMessage().substring(colon + 1));
    }
  }

  @EventHandler
  @Keep
  private void onCommandSend(PlayerCommandSendEvent event) {
    if (event.getPlayer().hasPermission(BYPASS_PERMISSION)) {
      return;
    }

    CommandMap commandMap = getServer().getCommandMap();

    event.getCommands().removeIf(command -> {
      if (command.indexOf(':') > -1) {
        return true;
      }

      Command internalCommand = commandMap.getCommand(command);
      return internalCommand == null || !internalCommand.testPermissionSilent(event.getPlayer());
    });
  }

  @EventHandler
  @Keep
  private void onTabComplete(TabCompleteEvent event) {
    if (!(event.getSender() instanceof Player)
        || event.getSender().hasPermission(BYPASS_PERMISSION)) {
      return;
    }

    int colon = event.getBuffer().indexOf(':');
    int space = event.getBuffer().indexOf(' ');
    if (0 < colon && (colon < space || space < 0)) {
      event.setCancelled(true);
    }
  }

}
