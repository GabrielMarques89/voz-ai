package org.gmarques;

import static org.gmarques.config.ApiKeyLoader.loadApiKey;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.gmarques.model.openai.client.OpenAIRealtimeClient;
import org.gmarques.service.AudioCapture;
import org.gmarques.util.Constants;
import org.gmarques.util.GlobalShortcutListener;
import org.gmarques.util.TrayIconManager;

public class Main {

  private static boolean isRecording = false;
  private static OpenAIRealtimeClient client;
  private static AudioCapture audioCapture;
  private static Thread recordingThread;

  public static void main(String[] args) {
    TrayIconManager trayIconManager = new TrayIconManager(Main::toggleRecording);
    GlobalShortcutListener shortcutListener = new GlobalShortcutListener(trayIconManager::toggle);

    initializeWebSocket();

    System.out.println("Audio Tray App is running. Press F12 to toggle recording.");
  }

  private static void initializeWebSocket() {
    String url = Constants.API_URL;
    try {
      URI uri = new URI(url);
      Map<String, String> headers = new HashMap<>();
      headers.put("Authorization", "Bearer " + loadApiKey());
      headers.put("OpenAI-Beta", "realtime=v1");

      client = new OpenAIRealtimeClient(uri, headers);
      client.connectBlocking();
      System.out.println("WebSocket connection established.");
    } catch (Exception e) {
      System.err.println("Failed to establish WebSocket connection:");
      e.printStackTrace();
    }
  }

  private static synchronized void toggleRecording() {
    if (isRecording) {
      stopRecording();
    } else {
      startRecording();
    }
  }

  private static void startRecording() {
    try {
      audioCapture = new AudioCapture();
      audioCapture.start();
      isRecording = true;

      recordingThread = new Thread(() -> {
        try {
          while (client.isOpen() && isRecording) {
            byte[] audioData = audioCapture.read();
            if (audioData != null) {
              client.sendAudio(audioData);
            }
            Thread.sleep(10);
          }
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
          System.err.println("An error occurred during recording:");
          e.printStackTrace();
        } finally {
          stopRecording();
        }
      });
      recordingThread.start();
    } catch (Exception e) {
      System.err.println("Failed to start recording:");
      e.printStackTrace();
    }
  }

  private static void stopRecording() {
    isRecording = false;
    if (audioCapture != null) {
      audioCapture.stop();
      audioCapture = null;
      System.out.println("Audio capture stopped.");
    }
    if (recordingThread != null) {
      recordingThread.interrupt();
      try {
        recordingThread.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      recordingThread = null;
    }
  }

  public static void shutdown() {
    stopRecording();
    if (client != null) {
      try {
        client.closeBlocking();
        client = null;
        System.out.println("WebSocket connection closed.");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }
}