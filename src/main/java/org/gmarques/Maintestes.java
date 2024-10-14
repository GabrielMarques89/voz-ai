package org.gmarques;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import lombok.SneakyThrows;

public class Maintestes {


  @SneakyThrows
  public static void main(String[] args) {
    Robot robot = new Robot();

    // Press F13 key (KeyEvent.VK_F13 is available from Java 11 onward)
    robot.keyPress(KeyEvent.VK_F13);
    robot.keyRelease(KeyEvent.VK_F13);
  }
}
