package org.gmarques.util.actions;

import java.awt.Robot;
import java.awt.event.InputEvent;

public class ClickAction implements Action {
    private int x;
    private int y;

    public ClickAction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void execute() throws Exception {
      var robot = new Robot();
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
}
