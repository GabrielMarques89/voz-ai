package org.gmarques.util;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class TrayIconManager {

    private TrayIcon trayIcon;
    private final Image iconInactive;
    private final Image iconActive;
    private boolean isActive;
    private final ActionListener toggleAction;

    /**
     * Construtor que inicializa o TrayIcon com os ícones e a ação de toggle.
     *
     * @param toggleAction Ação a ser executada quando o ícone for clicado.
     */
    public TrayIconManager(ActionListener toggleAction) {
        this.toggleAction = toggleAction;
        isActive = false;

        // Carregue seus ícones aqui. Certifique-se de que os caminhos estejam corretos.
        iconInactive = loadImage("off.ico");
        iconActive = loadImage("on.ico");

        if (!SystemTray.isSupported()) {
            System.err.println("System tray não é suportado.");
            return;
        }

        trayIcon = new TrayIcon(iconInactive, "Audio Tray App");
        trayIcon.setImageAutoSize(true);

        // Adiciona um menu popup com uma opção para sair
        PopupMenu popup = new PopupMenu();
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            // Encerra a aplicação
            System.exit(0);
        });
        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);

        trayIcon.addActionListener(toggleAction);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            System.err.println("Falha ao adicionar o tray icon:");
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

    /**
     * Alterna o estado do ícone (ativo/inativo).
     */
    public void toggle() {
        isActive = !isActive;
        updateIcon(isActive);
    }

    /**
     * Atualiza o ícone baseado no estado de gravação.
     *
     * @param active Se o estado é ativo (gravação em andamento).
     */
    public void updateIcon(boolean active) {
        isActive = active;
        trayIcon.setImage(isActive ? iconActive : iconInactive);
        trayIcon.setToolTip(isActive ? "Audio Tray App - Gravação Ativa" : "Audio Tray App");
    }
}
