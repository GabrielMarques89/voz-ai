package org.gmarques.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class GlobalShortcutListener implements NativeKeyListener {

  private Runnable toggleCallback;

  public GlobalShortcutListener(Runnable toggleCallback) {
    this.toggleCallback = toggleCallback;
    disableLogging();
    try {
      GlobalScreen.registerNativeHook();
    } catch (Exception e) {
      e.printStackTrace();
    }
    GlobalScreen.addNativeKeyListener(this);
  }

  private void disableLogging() {
    Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
    logger.setLevel(Level.OFF);
    logger.setUseParentHandlers(false);
  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {

    if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_F13) {
      toggleCallback.run();
    }
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

  }

  @Override
  public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

  }

  public void unregister() {
    try {
      GlobalScreen.unregisterNativeHook();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
