package com.github.jikoo.planarplugins;

import com.github.jikoo.planarwrappers.util.Experience;
import com.google.errorprone.annotations.Keep;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class BottleExperience extends JavaPlugin implements Listener {

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
  }

  @EventHandler
  @Keep
  private void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_AIR || event.getHand() == null) {
      return;
    }

    Player player = event.getPlayer();
    ItemStack held = player.getInventory().getItem(event.getHand());

    if (held.getType() != Material.GLASS_BOTTLE) {
      return;
    }

    RayTraceResult rayTrace = player.rayTraceBlocks(5, FluidCollisionMode.ALWAYS);
    if (rayTrace != null) {
      return;
    }

    int quantity = player.isSneaking() ? held.getAmount() : 1;
    int currentExp = Experience.getExp(player);
    quantity = Math.min(quantity, currentExp / 11);

    if (quantity <= 0) {
      return;
    }

    Experience.changeExp(player, -11 * quantity);
    held.setAmount(held.getAmount() - quantity);

    if (held.getAmount() <= 0) {
      held = ItemStack.empty();
    }

    player.getInventory().setItem(event.getHand(), held);
    player.getWorld()
        .dropItem(player.getLocation(), ItemStack.of(Material.EXPERIENCE_BOTTLE, quantity))
        .setPickupDelay(0);
  }

  @EventHandler
  @Keep
  private void onBottleThrow(ExpBottleEvent event) {
    event.setExperience(10);
  }

}
