package com.github.jikoo.planarplugins;

import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.annotations.Keep;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Player;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@NullMarked
public class HorseHusbandry extends JavaPlugin implements Listener {

  private enum HorseRange {
    DEFAULT(
        new Range(15.0, 30.0),
        new Range(0.1125, 0.3375),
        new Range(0.4, 1.0)
    ),
    DONKEY(
        DEFAULT.health,
        new Range(0.175),
        new Range(0.5)
    ),
    LLAMA(
        DEFAULT.health,
        null,
        null
    ),
    SKELETON(
        new Range(15.0),
        new Range(0.2),
        DEFAULT.jump
    ),
    ZOMBIE(
        new Range(25.0),
        new Range(0.21347, 0.28463),
        new Range(0.5, 0.7)
    );

    private final Range health;
    private final @Nullable Range speed;
    private final @Nullable Range jump;

    HorseRange(Range health, @Nullable Range speed, @Nullable Range jump) {
      this.health = health;
      this.speed = speed;
      this.jump = jump;
    }
  }

  private final Random random = new Random();

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
  }

  @EventHandler
  @Keep
  private void onHorseMount(EntityMountEvent event) {
    if (!(event.getEntity() instanceof Player player
        && event.getMount() instanceof AbstractHorse horse)) {
      return;
    }

    HorseRange range = getHorseRange(horse);

    if (range == null) {
      return;
    }

    player.showTitle(
        Title.title(
            Component.empty(),
            Component.text(
                String.format(
                    "Health: %.2f%%  -  Speed: %.2f%%  -  Jump: %.2f%%",
                    getSkillPercent(range.health, Attribute.MAX_HEALTH, horse),
                    getSkillPercent(range.speed, Attribute.MOVEMENT_SPEED, horse),
                    getSkillPercent(range.jump, Attribute.JUMP_STRENGTH, horse)
                )
            ),
            Title.Times.times(
                Duration.of(500, ChronoUnit.MILLIS),
                Duration.of(5, ChronoUnit.SECONDS),
                Duration.of(1, ChronoUnit.SECONDS)
            )
        )
    );
  }

  private double getSkillPercent(@Nullable Range range, Attribute att, AbstractHorse horse) {
    if (range == null || range.max() == range.min()) {
      return 100.0;
    }

    AttributeInstance attribute = horse.getAttribute(att);

    if (attribute == null) {
      return 100.0;
    }

    return (attribute.getBaseValue() - range.min()) / (range.max() - range.min()) * 100.0;
  }

  @EventHandler
  @Keep
  private void onHorseBreed(EntityBreedEvent event) {
    if (!(event.getEntity() instanceof AbstractHorse horse
        && event.getMother() instanceof AbstractHorse parent1
        && event.getFather() instanceof AbstractHorse parent2)) {
      return;
    }

    HorseRange range = getHorseRange(horse);

    if (range != null) {
      generateNewValue(range.health, Attribute.MAX_HEALTH, horse, parent1, parent2);
      generateNewValue(range.speed, Attribute.MOVEMENT_SPEED, horse, parent1, parent2);
      generateNewValue(range.jump, Attribute.JUMP_STRENGTH, horse, parent1, parent2);
    }
  }

  private @Nullable HorseRange getHorseRange(AbstractHorse horse) {
    return switch (horse) {
      case Horse ignored -> HorseRange.DEFAULT;
      // Mules are horse + donkey and cap at horse stats.
      // TODO mathematical mule max
      case Mule ignored -> HorseRange.DEFAULT;
      case Llama ignored -> HorseRange.LLAMA;
      case Donkey ignored -> HorseRange.DONKEY;
      case SkeletonHorse ignored -> HorseRange.SKELETON;
      case ZombieHorse ignored -> HorseRange.ZOMBIE;
      default -> null;
    };
  }

  private void generateNewValue(
      @Nullable Range range,
      Attribute attribute,
      AbstractHorse child,
      AbstractHorse parent1,
      AbstractHorse parent2
  ) {
    AttributeInstance childAttribute = child.getAttribute(attribute);
    AttributeInstance parent1Attribute = parent1.getAttribute(attribute);
    AttributeInstance parent2Attribute = parent2.getAttribute(attribute);

    if (childAttribute == null || parent1Attribute == null || parent2Attribute == null) {
      return;
    }

    // Get average of parents' stats.
    double parent1Value = parent1Attribute.getBaseValue();
    double parent2Value = parent2Attribute.getBaseValue();
    double parentalAverage = (parent1Value + parent2Value) / 2;

    // Add a random bonus ranging from -5% to +10% to the parents' average stats.
    double childValue = parentalAverage * random.nextDouble(0.95, 1.10);

    // Clamp value.
    if (range != null) {
      childValue = range.clamp(childValue);
    } else {
      // If range isn't available, use parents as range.
      // This should provide a fallthrough for entities with unknown ranges, i.e. llamas.
      double min;
      double max;
      if (parent1Value < parent2Value) {
        min = parent1Value;
        max = parent2Value;
      } else {
        min = parent2Value;
        max = parent1Value;
      }
      childValue = Math.clamp(childValue, min, max);
    }

    childAttribute.setBaseValue(childValue);
  }

  @Immutable
  private record Range(double min, double max) {
    private Range(double value) {
      this(value, value);
    }

    private double clamp(double input) {
      return Math.clamp(input, min, max);
    }
  }

}
