package com.github.jikoo.planarplugins;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RepairCostRemover extends JavaPlugin implements Listener {

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
  }

  @EventHandler
  public void onPrepareAnvil(PrepareAnvilEvent event) {
    var result = event.getResult();
    if (result != null && !result.isEmpty()) {
      result.resetData(DataComponentTypes.REPAIR_COST);
    }
  }

}
