package com.github.jikoo.planarplugins;

import com.google.errorprone.annotations.Keep;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WitherFacts extends JavaPlugin implements Listener {

  private @UnknownNullability ItemStack facts;

  @Override
  public void onEnable() {
    this.facts = new ItemStack(Material.WRITTEN_BOOK);
    facts.editMeta(BookMeta.class, book -> {
      book.author(Component.text("Pete").color(NamedTextColor.BLUE));
      book.title(Component.text("Wither Facts"));
      book.addPages(Component.text("Withers are awesome."));
    });

    getServer().getPluginManager().registerEvents(this, this);
  }

  @EventHandler(ignoreCancelled = true)
  @Keep
  private void onPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();
    EntityDamageEvent lastDamage = player.getLastDamageCause();
    if (lastDamage != null
        && (lastDamage.getCause() == EntityDamageEvent.DamageCause.WITHER
        || (lastDamage instanceof EntityDamageByEntityEvent entityDamage
        && entityDamage.getDamager().getType() == EntityType.WITHER))) {
      event.getDrops().add(facts.clone());
    }
  }

}
