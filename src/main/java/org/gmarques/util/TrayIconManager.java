package org.gmarques.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;

public class TrayIconManager {
    private TrayIcon trayIcon;
    private boolean isOn;

    private Runnable toggleCallback;

    public TrayIconManager(Runnable toggleCallback) {
        this.toggleCallback = toggleCallback;
        isOn = false;
        setupTrayIcon();
    }

    private void setupTrayIcon() {
        if (!SystemTray.isSupported()) {
            System.out.println("System tray not supported!");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        Image image = loadImage("off.ico");

        PopupMenu popup = new PopupMenu();

        MenuItem toggleItem = new MenuItem("Turn On");
        toggleItem.addActionListener((ActionEvent e) -> {
            toggle();
        });

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener((ActionEvent e) -> {
            System.exit(0);
        });

        popup.add(toggleItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon = new TrayIcon(image, "Audio Tray App", popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener((ActionEvent e) -> {
            toggle();
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            e.printStackTrace();
        }
    }

    private Image loadImage(String path) {
        try (InputStream is = getClass().getResourceAsStream("/" + path)) {
            if (is != null) {
                return ImageIO.read(is);
            } else {
                System.out.println("Icon image not found.");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void toggle() {
        isOn = !isOn;
        if (isOn) {
            trayIcon.setImage(loadImage("on.ico"));
        } else {
            trayIcon.setImage(loadImage("off.ico"));
        }
        toggleCallback.run();
    }

    public boolean isOn() {
        return isOn;
    }
}
