package com.github.gpaddons.blockhighlightboundaries.type;

/** Enum representing types of block highlights. */
public enum HighlightType {

  /** A block highlight using the game_test_marker block debug. */
  DEBUG_BLOCK(false, false),
  /** A block highlight using invisible glowing entities. */
  GLOWING_ENTITY(true, false),
  /** A block highlight using an item display with a custom model */
  ITEM_DISPLAY(true, true);

  private final boolean requiresErase;
  private final boolean scalable;

  HighlightType(boolean requiresErase, boolean scalable) {
    this.requiresErase = requiresErase;
    this.scalable = scalable;
  }

  public boolean requiresErase() {
    return this.requiresErase;
  }

  public boolean isScalable() {
    return this.scalable;
  }
}
