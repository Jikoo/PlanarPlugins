package com.github.jikoo.planarplugins;

import com.google.errorprone.annotations.Keep;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class FreeCart extends JavaPlugin implements Listener {

  private @UnknownNullability NamespacedKey freeCart;

  @Override
  public void onEnable() {
    saveDefaultConfig();
    freeCart = new NamespacedKey(this, "free_cart");
    getServer().getPluginManager().registerEvents(this, this);
  }

  @EventHandler
  @Keep
  private void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK
        || event.getHand() == EquipmentSlot.HAND
        || event.getPlayer().isInsideVehicle()) {
      return;
    }
    Block block = event.getClickedBlock();
    if (block == null || !Tag.RAILS.isTagged(block.getType())) {
      return;
    }
    block.getWorld().spawn(
        block.getLocation().add(0.5, 1.5, 0.5),
        RideableMinecart.class,
        cart -> {
          cart.getPersistentDataContainer().set(freeCart, PersistentDataType.BOOLEAN, true);
          cart.addPassenger(event.getPlayer());
        }
    );
  }

  @EventHandler
  @Keep
  private void onPlayerQuit(PlayerQuitEvent event) {
    Entity vehicle = event.getPlayer().getVehicle();
    if (vehicle != null && vehicle.getPersistentDataContainer().has(freeCart)) {
      vehicle.eject();
      vehicle.remove();
    }
  }

  @EventHandler
  @Keep
  private void onVehicleExit(VehicleExitEvent event) {
    if (event.getVehicle().getPersistentDataContainer().has(freeCart)) {
      event.getVehicle().eject();
      event.getVehicle().remove();
    }
  }

  @EventHandler
  @Keep
  private void onVehicleDestroy(VehicleDestroyEvent event) {
    if (event.getVehicle().getPersistentDataContainer().has(freeCart)) {
      event.setCancelled(true);
      event.getVehicle().eject();
      event.getVehicle().remove();
    }
  }

}
