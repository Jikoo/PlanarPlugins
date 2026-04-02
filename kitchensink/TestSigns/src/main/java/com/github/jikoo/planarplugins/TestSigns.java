package com.github.jikoo.planarplugins;

import com.google.errorprone.annotations.Keep;
import net.kyori.adventure.text.Component;
import org.bukkit.Tag;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class TestSigns extends JavaPlugin implements Listener {

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
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

    for (Side side : Side.values()) {
      SignSide signSide = sign.getSide(side);
      signSide.line(2, Component.text("test"));
    }

    state.update(true);
  }

}
