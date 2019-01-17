package com.nedap.go;

public abstract class Player {
  private int colour;
  private Board board;
  private String name;
  
  // -- Constructors -----------------------------------------------
  
  /**
   * Creates a new Player object.
   * 
   */
  public Player(String name, int colour) {
      this.name = name;
      this.colour = colour;
  }
  
  //-- Queries ----------------------------------------------------

  /**
   * Returns the name of the player.
   */
  /*@ pure */ 
  public String getName() {
      return name;
  }
  
  /**
   * Returns the colour of the player.
   */
  /*@ pure */ 
  public int getColour() {
      return colour;
  }
  
  /**
   * Determines the field for the next move.
   * @param board: the current game board
   * @return the player's choice
   */
  public abstract int determineMove(Board board);

  //-- Commands ---------------------------------------------------
  
  /**
   * Makes a move on the board.
   * @param board: the current board
   */
  public void makeMove() {
    int choice = determineMove(board);
    board.setPoint(choice, colour);
    // of een pass
  }

  public int getScore() {
    return board.getScore(colour);
  }


}
