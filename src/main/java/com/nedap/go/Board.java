package com.nedap.go;

/**
 * Board for GO game. Constructor creates a board with size n*n. Another
 * constuctor makes board with String. Returns value of point. Checks if 
 * board is full. Changes the board when setPoint method is called. 
 * Returns board size. Checks if point is empty.
 * 
 * , returns board to string or to int[].    
 * @author marije.linthorst
 *
 */
public class Board {
  private final int BSIZE;
  private int[] points;
  
//-- Constructors -----------------------------------------------

  /**
   * Creates a board and ensures this board is empty(=0).
   * @param size
   */
  public Board (int size) {
    BSIZE = size;
    points = new int[BSIZE*BSIZE];
    for (int i = 0; i < BSIZE * BSIZE; i++) {
      points[i] = 0;
    }
  }
  
  /**
   * Creates a board from a String.
   * @param string
   */
  public Board (String board) {
    BSIZE = (int) Math.sqrt(board.length());
    points = new int[BSIZE*BSIZE];
    for (int i = 0; i<board.length(); i++) {
      char charAti = board.charAt(i);
      points[i] = Character.getNumericValue(charAti);
    }
  }
  
  // ------------------ Queries----------------------
  
  /**
   * Returns the content of the point i.
   * @param i: index of the point
   * @return the integer on the point
   */
  /*@pure*/
  public int getPoint(int i) {
      return points[i];
  }
  
  /**
   * Tests if the point is empty
   */
  /*@pure*/
  public boolean isEmpty(int i) {
    return points[i]==0;
  }
  
  public boolean onBoard(int i) {
    return (i<this.getBoardSize() && i>=0);
  }
  
  /**
   * Tests if the whole board is full.
   * @return true if all fields are occupied
   */
  /*@pure*/
  public boolean isFull() {
    int count = 0;
      for (int i =0; i < BSIZE*BSIZE; i++) {
        if (points[i] == 0) {
          count++;
        }
      }
      return count==0;
  }
  
  /**
   * returns size n*n, if board is n*n.
   * @return
   */
  public int getBoardSize() {
    return points.length;
  }
  
  /**
   * returns current string board
   */
  public String getCurrentStringBoard() {
    String array="";
    for (int i = 0; i<points.length;i++) {
      array = array + points[i];
    }
    return array;
  }
  
  /**
   * returns current int[] board
   */
  public int[] getCurrentIntBoard() {
    return points;
  }
  
  /**
   * Creates a deep copy of this field.
   */
  public Board deepCopy() {
      Board newBoard = new Board(BSIZE);
      for (int i = 0;i < BSIZE * BSIZE; i++) {
        newBoard.points[i] = this.points[i];
      }
      return newBoard;
  }
  
  // --------------- Commands---------------------------
  /**
   * Sets the content of field i to the given colour.
   *
   * @param i: index of the point
   * @param colour: the colour to be placed
   */
  public void setPoint(int i, int colour) {
      points[i]=colour;
  }
  
  // --------------- For TUI test --------------------
  /**
   * String representation of the board.
   */
  public String toTUIString() {
    String s = "";
    for (int i=0;i<BSIZE;i++) {
      for (int j=0;j<BSIZE;j++) {
        s = s + getPoint(i*BSIZE + j) + " ";
      }
      s = s + "\n";
    }
    return s;
  }
}
