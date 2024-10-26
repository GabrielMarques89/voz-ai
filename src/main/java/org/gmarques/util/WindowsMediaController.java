package org.gmarques.util;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.WORD;
import com.sun.jna.platform.win32.WinUser.INPUT;
import com.sun.jna.platform.win32.WinUser.KEYBDINPUT;
import lombok.extern.slf4j.Slf4j;
import org.gmarques.model.enums.MediaAction;
import org.gmarques.model.interfaces.MediaController;

@Slf4j
public class WindowsMediaController implements MediaController {
    private static final int KEYEVENTF_KEYUP = 0x0002;

    @Override
    public void executeAction(String actionStr) throws Exception {
        MediaAction action = MediaAction.fromString(actionStr);
        log.info("Executando ação de mídia: {}", action.getAction());

        switch (action) {
            case PLAY_PAUSE:
                sendMediaKeyWindows(0xB3); // Media Play/Pause
                break;
            case NEXT_TRACK:
                sendMediaKeyWindows(0xB0); // Media Next Track
                break;
            case MAX_VOLUME:
                Runtime.getRuntime().exec("nircmd.exe setsysvolume 65535");
                break;
            case INCREASE_VOLUME:
                Runtime.getRuntime().exec("nircmd.exe changesysvolume 5000");
                break;
            case DECREASE_VOLUME:
                Runtime.getRuntime().exec("nircmd.exe changesysvolume -5000");
                break;
            default:
                log.warn("Ação não reconhecida: {}", action.getAction());
        }
    }

    private void sendMediaKeyWindows(int keyCode) {
        INPUT input = new INPUT();
        input.type = new DWORD(INPUT.INPUT_KEYBOARD);
        input.input.setType("ki");
        input.input.ki = new KEYBDINPUT();
        input.input.ki.wVk = new WORD(keyCode);
        input.input.ki.dwFlags = new DWORD(0);

        User32.INSTANCE.SendInput(new DWORD(1), new INPUT[]{input}, input.size());
        input.input.ki.dwFlags = new DWORD(KEYEVENTF_KEYUP);
        User32.INSTANCE.SendInput(new DWORD(1), new INPUT[]{input}, input.size());
    }
}
