package com.github.jikoo.planarplugins;

import com.google.errorprone.annotations.Keep;
import org.bukkit.GameEvent;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.world.GenericGameEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class DoubleDoors extends JavaPlugin implements Listener {

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
  }

  @EventHandler
  @Keep
  private void onBlockMultiPlace(BlockMultiPlaceEvent event) {
    Block block = event.getBlock();
    Door door = asDoor(block);

    if (door == null) {
      // Bed or double plant or something.
      return;
    }

    // Prefer to assume we're placing the right door.
    BlockFace adjacentFace = getAdjacentDoor(door.getFacing(), Door.Hinge.RIGHT);
    Block adjacentBlock = block.getRelative(adjacentFace);
    Door adjacentDoor = asDoor(adjacentBlock);

    if (adjacentDoor != null) {
      correctHinges(adjacentDoor, adjacentBlock, door, block);
      return;
    }

    // If there was no door to the left, try the right.
    adjacentFace = adjacentFace.getOppositeFace();
    adjacentBlock = block.getRelative(adjacentFace);
    adjacentDoor = asDoor(adjacentBlock);

    if (adjacentDoor != null) {
      correctHinges(door, block, adjacentDoor, adjacentBlock);
    }
  }

  @EventHandler
  @Keep
  private void onBlockGameEvent(GenericGameEvent event) {
    boolean open = event.getEvent() == GameEvent.BLOCK_OPEN;
    if (!open && event.getEvent() != GameEvent.BLOCK_CLOSE) {
      return;
    }

    Block block = event.getLocation().getBlock();

    Door door = asDoor(block);

    if (door == null) {
      return;
    }

    BlockFace adjacentFace = getAdjacentDoor(door.getFacing(), door.getHinge());
    Block adjacentBlock = block.getRelative(adjacentFace);

    // Note that we only need to adjust the one door half!
    // The server handles the other half.
    modifyAdjacentDoor(door, open, adjacentBlock);
  }

  private @Nullable Door asDoor(Block block) {
    if (!Tag.DOORS.isTagged(block.getType())) {
      return null;
    }

    BlockData data = block.getBlockData();

    if (!(data instanceof Door door)) {
      return null;
    }

    return door;
  }

  private BlockFace getAdjacentDoor(BlockFace facing, Door.Hinge hinge) {
    return switch (facing) {
      case NORTH -> hinge == Door.Hinge.LEFT ? BlockFace.EAST : BlockFace.WEST;
      case SOUTH -> hinge == Door.Hinge.LEFT ? BlockFace.WEST : BlockFace.EAST;
      case EAST -> hinge == Door.Hinge.LEFT ? BlockFace.SOUTH : BlockFace.NORTH;
      case WEST -> hinge == Door.Hinge.LEFT ? BlockFace.NORTH : BlockFace.SOUTH;
      default -> throw new IllegalStateException("Door not facing cardinal direction: " + facing);
    };
  }

  private void modifyAdjacentDoor(Door door, boolean open, Block adjacentBlock) {
    Door adjacentDoor = asDoor(adjacentBlock);

    if (adjacentDoor == null
        // If the door isn't facing the same direction, it isn't a double door.
        || adjacentDoor.getFacing() != door.getFacing()
        // If the door's hinge isn't opposite that of the changed door, it isn't a double door.
        || adjacentDoor.getHinge() == door.getHinge()
        // If the door is already in the correct state, don't mess with it.
        || adjacentDoor.isOpen() == open) {
      return;
    }

    adjacentDoor.setOpen(open);
    adjacentBlock.setBlockData(adjacentDoor);
  }

  private void correctHinges(Door leftDoor, Block leftBlock, Door rightDoor, Block rightBlock) {
    if (leftDoor.getFacing() != rightDoor.getFacing()) {
      return;
    }

    if (leftDoor.getHinge() != Door.Hinge.LEFT) {
      leftDoor.setHinge(Door.Hinge.LEFT);
      leftBlock.setBlockData(leftDoor);
    }

    if (rightDoor.getHinge() != Door.Hinge.RIGHT) {
      rightDoor.setHinge(Door.Hinge.RIGHT);
      rightBlock.setBlockData(rightDoor);
    }
  }

}