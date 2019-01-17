package com.nedap.go;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class GameTest {
  private Game game;
  private Player[] players;

  @Before
  public void setUp() throws Exception {
    game = new Game(players, 5);
  }

  @Test
  public void test() {
    fail("Not yet implemented");
  }

}
