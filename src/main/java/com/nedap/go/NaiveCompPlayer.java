package com.nedap.go;

import java.util.List;
import java.util.Random;


public class NaiveCompPlayer extends Player{
  private List<String> history;

  public NaiveCompPlayer(String name, int colour) {
    super(name, colour);
  }

  /**
   * Determines the field for the next move.
   * @param board: the current game board
   * @return the player's choice
   */
  public String determineMove(Board board) {
    boolean valid = false;
    int randomMove = 0;
    while (!valid) {
      int upperRange = board.getBoardSize();
      Random random = new Random();
      randomMove = random.nextInt(upperRange);
      
      String validMove = this.isValidMove(randomMove, this.getColour(), board);
      valid = (validMove=="Move valid");
    }
    return ("MOVE" + randomMove) ;
  }
  
  public String isValidMove(int index, int colour, Board board) {
 // check for captures and remove stuff if needed
    // check if its the right colour to doMove
    if (!board.onBoard(index)) {
      return "Move invalid: not on board";
    }  
    if (!board.isEmpty(index)){
      return "Move invalid: point not empty";
    } 
    Board copy = board.deepCopy();
    copy.setPoint(index, colour);
    if(history.contains(copy.getCurrentStringBoard())) { 
      return "Move invalid: creates a previous board state";
    }
    // rules: check rules etc
    return "Move valid";
  }
  
  public void addToHistory(String currentBoard) {
    history.add(currentBoard);
  }
}
