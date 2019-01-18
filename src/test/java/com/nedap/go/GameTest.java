package com.nedap.go;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class GameTest {
  private Game game;
  private Player[] players;

  @Before
  public void setUp() {
    players = new Player[2];
    players[0]= new HumanPlayer("Piet", 1);
    players[1]= new Player("Klaas", 2);
    game = new Game(players, 3);
  }

  @Test
  public void testcurrentPlayer() {
    assertEquals(game.currentPlayer(), players[0]);
    game.doMove(5, 1);
    assertEquals(game.currentPlayer(), players[1]);
    game.doPass();
    assertEquals(game.currentPlayer(), players[0]);
  }
  
  @Test
  public void testPasses() {
    game.doMove(5, 1);
    game.doPass();
    assertEquals(game.numberOfPasses(), 1);
    game.doMove(6, 1);
    assertEquals(game.numberOfPasses(), 0);
    game.doPass();
    game.doPass();
    assertEquals(game.numberOfPasses(), 2);
  }
  
  @Test
  public void testgetBoardSize() {
    assertEquals(game.getBoardSize(),9);
  }
  
  @Test
  public void testgetScore() {
    fail("Not yet implemented");
  }
  
  @Test
  public void testisFinished() {
    assertFalse(game.isFinished());
    game.doPass();
    game.doPass();
    assertTrue(game.isFinished());
    
    game.doMove(0, 1);
    assertFalse(game.isFinished());
    game.doMove(1, 1);
    game.doMove(2, 1);
    game.doMove(3, 1);
    game.doMove(4, 1);
    game.doMove(5, 1);
    game.doMove(6, 1);
    game.doMove(7, 1);
    game.doMove(8, 1);
    assertTrue(game.isFinished());
  }
  
  @Test
  public void testisValidMove() {
    assertEquals(game.isValidMove(0, 1), "Move valid");
    assertEquals(game.isValidMove(10, 1), "Move invalid: not on board");
    game.doMove(0, 1);
    assertEquals(game.isValidMove(0, 2), "Move invalid: point not empty");
    game.removeStone(0);
    assertEquals(game.isValidMove(0, 1), "Move invalid: creates a previous board state");
    System.out.println("Misses some rules (isValidMove())");
  }
  
  @Test
  public void testdetermineWinner() {
    fail("getScore not yet implemented");
  }
  
  @Test
  public void testupdate() {
    String testone = "000000000";
    String testtwo = "100000000";
    assertEquals(game.update(), testone);
    game.doMove(0, 1);
    assertEquals(game.update(), testtwo);
  }
  
  @Test
  public void testremoveStone() {
    String testone = "100000000";
    String testtwo = "000000000";
    game.doMove(0, 1);
    assertEquals(game.update(), testone);
    game.removeStone(0);
    assertEquals(game.update(), testtwo);
  }
}
