package com.nedap.go;

import java.util.Scanner;

public class HumanPlayer extends Player{

  public HumanPlayer(String name, int colour) {
    super(name, colour);
  }
  
  /**
   * Determines the field for the next move.
   * @param board: the current game board
   * @return the player's choice
   */
  public String determineMove(Board board) {
    // board to his
    String message = "> " + this.getName() + " (" + this.getColour() + ")"
        + ", what is your choice? Type MOVE, PASS or EXIT";
    String command = readString(message);
    
    if(command.startsWith("PASS")) {
      return "PASS";
    } else if(command.startsWith("MOVE")) {
      String askMove = "";
      return "";
    }
    return "";
  }
  
  private String readString(String message) {
    String value = "";
    boolean StringRead = false;
    Scanner line = new Scanner(System.in);
    do {
      System.out.print(message);
      try (Scanner scannerLine = new Scanner(line.nextLine())) {
        if (scannerLine.hasNext()) {
          StringRead = true;
          value = scannerLine.next();
        }
      }
    } while (!StringRead);
    line.close();
    return value;
  }
}
