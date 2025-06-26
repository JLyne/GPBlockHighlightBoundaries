package com.github.gpaddons.blockhighlightboundaries.type;

/** Enum representing types of block highlights. */
public enum HighlightType {

  /** A block highlight using the game_test_marker block debug. */
  DEBUG_BLOCK(false),
  /** A block highlight using invisible glowing entities. */
  GLOWING_ENTITY(true);

  private final boolean requiresErase;

  HighlightType(boolean requiresErase) {
    this.requiresErase = requiresErase;
  }

  public boolean requiresErase() {
    return this.requiresErase;
  }
}
