package com.github.gpaddons.blockhighlightboundaries.style;

import com.github.gpaddons.blockhighlightboundaries.HighlightConfiguration;
import com.griefprevention.util.IntVector;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link RealCornerVisualization} abstraction that attempts to snap elements to the nearest
 * surface.
 */
public abstract class SnapToSurface extends RealCornerVisualization {
  private int lastLoadedDisplayHeight = Integer.MIN_VALUE;

  protected SnapToSurface(
      @NotNull World world,
      @NotNull IntVector visualizeFrom,
      int height,
      @NotNull HighlightConfiguration config) {
    super(world, visualizeFrom, height, config);
  }

  @Override
  protected @NotNull IntVector findDisplayCoordinate(@NotNull IntVector displayCoord, int minY) {
    if (!displayCoord.isChunkLoaded(world)) {
      // Don't load chunks to find a display coordinate.
      return getDefaultDisplay(displayCoord, minY);
    }

    // Highest block + 1 air
    int maxY = world.getHighestBlockAt(displayCoord.x(), displayCoord.z()).getY() + 1;
    Block startBlock = world.getBlockAt(displayCoord.x(), displayCoord.y(), displayCoord.z());

    // Find suitable surface block (solid block below a transparent block)
    // Check down first to show blocks on the ground when there is a low roof/in a cave etc
    // Check up if no suitable block found to handle e.g mountains
    IntVector result = findSuitableBlockDown(startBlock, minY);

    if (result == null) {
      result = findSuitableBlockUp(startBlock, maxY);
    }

    // Fallback to default position if nothing found
    if (result == null) {
      // Display at real boundary edge because it will be shown.
      result = getDefaultDisplay(displayCoord, minY);
    }

    return result;
  }

  /**
   * Attempt to find a suitable block above the starting position
   * A suitable block is a solid block directly below a transparent block
   * @param start - Starting block
   * @param maxY - The y level to stop searching at
   * @return The first suitable block found, if any
   */
  private IntVector findSuitableBlockUp(Block start, int maxY) {
    int y = Math.min(start.getY(), maxY);
    Boolean previousSolid = null;

    do {
      boolean solid = !isTransparent(start.getWorld().getBlockAt(start.getX(), y, start.getZ()));

      // Valid spot
      if (!solid && Boolean.TRUE.equals(previousSolid)) {
        lastLoadedDisplayHeight = y;
        return new IntVector(start.getX(), y - 1, start.getZ());
      }

      previousSolid = solid;
      y += 1;
    } while (y <= maxY);

    return null;
  }

  /**
   * Attempt to find a suitable block below the starting position
   * A suitable block is a solid block directly below a transparent block
   * @param start - Starting block
   * @param minY - The y level to stop searching at
   * @return The first suitable block found, if any
   */
  private IntVector findSuitableBlockDown(Block start, int minY) {
    int y = Math.max(minY, start.getY());
    Boolean previousSolid = null;

    do {
      boolean solid = !isTransparent(start.getWorld().getBlockAt(start.getX(), y, start.getZ()));

      // Stop checking blocks if we encounter 2 solid blocks in a row, to avoid
      // selecting a block inaccessible to the player
      if (solid && Boolean.TRUE.equals(previousSolid)) {
        break;
      }

      // Valid spot
      if (solid && Boolean.FALSE.equals(previousSolid)) {
        lastLoadedDisplayHeight = y;
        return new IntVector(start.getX(), y, start.getZ());
      }

      previousSolid = solid;
      y -= 1;
    } while (y >= minY);

    return null;
  }

  private boolean isTransparent(Block block) {
    Material blockMaterial = block.getType();

    // Custom per-material definitions.
    switch (blockMaterial)
    {
      case WATER, SNOW:
        return false;
    }

    if (blockMaterial.isAir()
        || Tag.FENCES.isTagged(blockMaterial)
        || Tag.FENCE_GATES.isTagged(blockMaterial)
        || Tag.SIGNS.isTagged(blockMaterial)
        || Tag.WALLS.isTagged(blockMaterial)
        || Tag.WALL_SIGNS.isTagged(blockMaterial))
      return true;

    return block.getType().isTransparent();
  }

  @Override
  protected @NotNull IntVector getDefaultDisplay(@NotNull IntVector vector, int minY) {
    // If an element has been successfully displayed, display in line with it in unloaded chunks.
    if (lastLoadedDisplayHeight != Integer.MIN_VALUE) {
      return new IntVector(vector.x(), Math.max(minY, lastLoadedDisplayHeight), vector.z());
    }

    // Fall through to default.
    return super.getDefaultDisplay(vector, minY);
  }
}
