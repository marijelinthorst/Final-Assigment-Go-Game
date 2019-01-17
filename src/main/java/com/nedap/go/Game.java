package com.nedap.go;

import java.util.List;

/**
 * Class for maintaining the Go game.
 * Returns current player, number of passes, score of colour, check
 * if game is finished, gives current status of board
 * do move, pass, remove stone
 * 
 * 
 * 
 * valid move (game his, rules)
 * 
 * get players?
 * exit?
 * get Board?
 * @author marije.linthorst
 */
public class Game {
  private Board board;
  private int countPasses; // how many players have passed
  private Player[] players; // players in the game
  private int current; // index current player
  private List<String> history;
  
  //-- Constructor -----------------------------------------------

  /**
   * Creates a new Game object.
   * @param s0 the first player
   * @param s1 the second player
   */
  public Game(Player[] players, int boardsize) {
      board = new Board(boardsize);
      this.players=players;
      current = 0;
  }
  
  // ---- Queries ------------------------------------------------
  
  public Player currentPlayer() {
    return players[current];
  }
  
  public int numberOfPasses() {
    return countPasses;
  }
  
  public int getScore (int colour) {
    //board.getCurrentIntBoard();
    // do something
    return 0;
  }
  
  public boolean isFinished() {
    if (countPasses==players.length || board.isFull()) {
      return true;
    } else {
    return false;
    }
  }
  
  /**
   * returns message about the validity of the move
   * @param index of the move
   * @param colour of the player
   * @return
   */
  public String isValidMove(int index, int colour) {
    Board copy = board.deepCopy();
    boolean iets = true;
    if (!board.onBoard(index)) {
      return "Not on board";
    } else if (!board.isEmpty(index)){
      return "Point not empty";
    } else if(iets) { 
      copy.setPoint(index, colour);
      }
    // rules: check rules etc
    return "true";
  }
  
  /**
   * Returns player that has won. <br>
   */
  private Player determineWinner() {
    Player winner = players[0];
    for (int i = 1; i<players.length;i++) {
      if (winner.getScore() < players[i].getScore()) {
        winner = players[i];
      }
    }
    return winner; 
  }
  
  /**
   * returns the game situation as a String.
   */
  private String update() {
      return board.getCurrentStringBoard();
  }
  
  
  // -- Commands ---------------------------------------------------

  /**
   * Places stone on point, resets passes and changes current player
   * to the next player
   */
  public void doMove(int index, int colour) {
    board.setPoint(index, colour);
    history.add(board.getCurrentStringBoard());
    countPasses=0;
    current++;
    if (current == players.length) {
      current=0;
    }
  }
  
  /**
   * Counts how often the method is called and changes the current
   * player to the next player
   */
  public void doPass() {
    countPasses++;
    current++;
    if (current == players.length) {
      current=0;
    }
  }
  
  /**
   * removes stone from board (e.g. when captured)
   * @param index
   */
  public void removeStone(int index) {
    board.setPoint(index, 0);
  }
}
