package com.nedap.go;

import java.util.ArrayList;
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
 * @author marije.linthorst
 */
public class Game {
  private Board board;
  private int countPasses; // how many players have passed
  private Player[] players; // players in the game
  private int current; // index current player
  private List<String> history;
  private int gameID;
  
  //-- Constructor -----------------------------------------------

  /**
   * Creates a new Game object.
   * @param s0 the first player
   * @param s1 the second player
   */
  public Game(Player[] players, int boardsize, int gameID) {
      board = new Board(boardsize);
      this.players=players;
      this.gameID = gameID;
      current = 0;
      history = new ArrayList<String>();
  }
  
  // ---- Queries ------------------------------------------------
  
  public int getGameID() {
    return gameID;
  }
  
  public Player currentPlayer() {
    return players[current];
  }
  
  public int numberOfPasses() {
    return countPasses;
  }
  
  public int getBoardSize() {
    return board.getBoardSize();
  }
  
  public double getScore (int colour) {
    //board.getCurrentIntBoard();
    // add 0.5 to score of black
    // do something
    if (colour==1) {
      return 0.5;
    } else {
      return 0;
    }
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
  
  /**
   * Returns player that has won. <br>
   */
  public Player determineWinner() {
    Player winner = players[0];
    for (int i = 1; i<players.length;i++) {
      if (winner.getScore(this) < players[i].getScore(this)) {
        winner = players[i];
      }
    }
    return winner; 
  }
  
  /**
   * returns the game situation as a String.
   */
  public String update() {
      return board.getCurrentStringBoard();
  }
  
  
  // -- Commands ---------------------------------------------------

  /**
   * Places stone on point, resets passes and changes current player
   * to the next player. Adds board, after move, to the history
   */
  public void doMove(int index, int colour) {
    board.setPoint(index, colour);
    Board copy = board.deepCopy();// is copy nodig?
    history.add(copy.getCurrentStringBoard());
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
