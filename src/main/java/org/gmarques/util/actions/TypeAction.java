package org.gmarques.util.actions;

import java.awt.Robot;
import java.awt.event.KeyEvent;

public class TypeAction implements Action {
    private String text;

    public TypeAction(String text) {
        this.text = text;
    }

    @Override
    public void execute() throws Exception {
        Robot robot = new Robot();
        for (char c : text.toCharArray()) {
            int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
        }
    }
}