package com.nedap.go;

import java.util.Random;
public class NaivePlayer extends Player{

  public NaivePlayer(String name, int colour) {
    super(name, colour);
  }

  /**
   * Determines the field for the next move.
   * @param board: the current game board
   * @return the player's choice
   */
  public int determineMove(Board board) {
    boolean valid = false;
    int randomMove = 0;
    while (!valid) {
      int upperRange = board.getBoardSize();
      Random random = new Random();
      randomMove = random.nextInt(upperRange);
      String validMove = this.isValidMove(randomMove, this.getColour());
      valid = (validMove=="Move valid");
    }
    return randomMove ;
  }
  
  public String isValidMove(int index, int colour) {
    return "";
  }
}
