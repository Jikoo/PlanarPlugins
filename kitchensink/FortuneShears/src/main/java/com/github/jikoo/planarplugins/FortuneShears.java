package com.github.jikoo.planarplugins;

import com.github.jikoo.planarenchanting.anvil.Anvil;
import com.github.jikoo.planarenchanting.anvil.AnvilBehavior;
import com.github.jikoo.planarenchanting.anvil.AnvilCreator;
import com.github.jikoo.planarenchanting.anvil.AnvilResult;
import com.github.jikoo.planarenchanting.anvil.ComponentAnvilFunctions;
import com.github.jikoo.planarenchanting.anvil.ComponentVanillaBehavior;
import com.github.jikoo.planarenchanting.anvil.WorkPiece;
import com.github.jikoo.planarenchanting.table.Enchantability;
import com.github.jikoo.planarenchanting.table.EnchantingTable;
import com.github.jikoo.planarenchanting.table.TableEnchantListener;
import com.google.errorprone.annotations.Keep;
import io.papermc.paper.event.block.PlayerShearBlockEvent;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@NullMarked
public class FortuneShears extends JavaPlugin implements Listener {

  private final Set<Enchantment> enchants = Set.of(Enchantment.UNBREAKING, Enchantment.EFFICIENCY, Enchantment.FORTUNE, Enchantment.SILK_TOUCH);
  private final EnchantingTable table = new EnchantingTable(enchants, new Enchantability(14));
  private final Anvil anvil = new Anvil() {

    private final AnvilBehavior<ItemStack> behavior = new ComponentVanillaBehavior() {
        @Override
        public boolean enchantApplies(Enchantment enchantment, ItemStack base) {
          return enchants.contains(enchantment);
        }
      };

    @Override
    public AnvilResult getResult(AnvilView view) {
      WorkPiece<ItemStack> piece = AnvilCreator.createComponentPiece(view);

      AnvilInventory anvilInv = view.getTopInventory();
      ItemStack base = anvilInv.getItem(0);

      if (base == null || base.getAmount() != 1) {
        return AnvilResult.EMPTY;
      }

      piece.apply(behavior, ComponentAnvilFunctions.RENAME);
      piece.apply(behavior, ComponentAnvilFunctions.UPDATE_PRIOR_WORK_COST);

      if (!piece.apply(behavior, ComponentAnvilFunctions.REPAIR_WITH_MATERIAL)) {
        // Only do combination repair if this is not a material repair.
        piece.apply(behavior, ComponentAnvilFunctions.REPAIR_WITH_COMBINATION);
      }

      piece.apply(behavior, ComponentAnvilFunctions.COMBINE_ENCHANTMENTS_JAVA);

      return piece.temper();
    }
  };

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);

    TableEnchantListener listener = new TableEnchantListener(this) {
      @Override
      protected EnchantingTable getTable(
          Player player,
          ItemStack enchanted) {
        return table;
      }

      @Override
      protected boolean isIneligible(Player player, ItemStack enchanted) {
        return enchanted.getType() != Material.SHEARS;
      }
    };
    getServer().getPluginManager().registerEvents(listener, this);
  }

  @EventHandler
  @Keep
  private void onPrepareAnvil(PrepareAnvilEvent event) {
    AnvilView view = event.getView();
    AnvilInventory inventory = view.getTopInventory();
    ItemStack base = inventory.getFirstItem();
    ItemStack addition = inventory.getSecondItem();

    if (areItemsInvalid(base, addition)) {
      return;
    }

    AnvilResult result = anvil.getResult(event.getView());

    if (result.equals(AnvilResult.EMPTY)) {
      return;
    }

    final var input = base.clone();
    final var input2 = addition.clone();
    final var resultItem = result.item();

    event.setResult(resultItem);

    getServer().getScheduler().runTask(this, () -> {
      // Ensure inputs have not been modified since our calculations.
      if (!input.equals(inventory.getFirstItem()) || !input2.equals(inventory.getFirstItem())) {
        return;
      }

      // Set result again - overrides bad enchantment plugins that always write result.
      inventory.setResult(resultItem);
      // Set repair cost. As vanilla has no result for our combinations, this is always set to 0
      // after the event has completed and needs to be set again.
      view.setRepairCost(result.levelCost());
    });
  }

  @Contract("null, _ -> true; _, null -> true")
  private boolean areItemsInvalid(@Nullable ItemStack base, @Nullable ItemStack addition) {
    return base == null || base.isEmpty()
        || addition == null || addition.isEmpty()
        || base.getType() != Material.SHEARS;
  }

  @EventHandler(ignoreCancelled = true)
  @Keep
  private void onPlayerShearEntity(PlayerShearEntityEvent event) {
    handleShear(new ShearEvent(event));
  }

  @EventHandler(ignoreCancelled = true)
  @Keep
  private void onPlayerShearBlock(PlayerShearBlockEvent event) {
    handleShear(new ShearEvent(event));
  }

  private void handleShear(ShearEvent event) {
    int fortune = event.hand().getEnchantmentLevel(Enchantment.FORTUNE);
    if (fortune < 1) {
      return;
    }

    List<ItemStack> drops = event.drops();
    if (drops.isEmpty()) {
      return;
    }

    int bonus = ThreadLocalRandom.current().nextInt(fortune + 1);

    if (bonus < 1) {
      return;
    }

    ItemStack drop = drops.getFirst();

    // TODO does this affect the resulting drops?
    //  Theoretically should, but the list is supposed to be immutable.
    //  May need to pass setDrops access through ShearEvent
    drop.setAmount(drop.getAmount() + bonus);
  }

  private record ShearEvent(ItemStack hand, List<ItemStack> drops) {

    ShearEvent(PlayerShearEntityEvent event) {
      this(event.getItem(), event.getDrops());
    }

    ShearEvent(PlayerShearBlockEvent event) {
      this(event.getItem(), event.getDrops());
    }

  }

}
