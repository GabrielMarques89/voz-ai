package org.gmarques;

import static org.gmarques.config.ApiKeyLoader.loadApiKey;
import static org.gmarques.config.ApiKeyLoader.loadPorcupineApiKey;

import ai.picovoice.porcupine.Porcupine.BuiltInKeyword;
import ai.picovoice.porcupine.PorcupineException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javazoom.jl.player.Player;
import lombok.SneakyThrows;
import org.gmarques.model.openai.client.OpenAIRealtimeClient;
import org.gmarques.service.AudioCapture;
import org.gmarques.util.Constants;
import org.gmarques.util.GlobalShortcutListener;
import org.gmarques.util.TrayIconManager;
import org.gmarques.util.WakeWordListener;

public class Main {

  private static boolean isRecording = false;
  private static OpenAIRealtimeClient client;
  private static AudioCapture audioCapture;
  private static Thread recordingThread;
  private static WakeWordListener wakeWordListener;
  private static TrayIconManager trayIconManager;

  public static void main(String[] args) {
    trayIconManager = new TrayIconManager(e -> toggleRecording());
    GlobalShortcutListener shortcutListener = new GlobalShortcutListener(trayIconManager::toggle);

    initializeWebSocket();
    Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));

    System.out.println("Audio Tray App está em execução. Diga 'Jarvis' ou pressione F12 para alternar a gravação.");
  }

  /**
   * Inicializa a conexão WebSocket com a API do OpenAI.
   */
  private static void initializeWebSocket() {
    String url = Constants.VOICE_MODEL_URL;
    try {
      URI uri = new URI(url);
      Map<String, String> headers = new HashMap<>();
      headers.put("Authorization", "Bearer " + loadApiKey());
      headers.put("OpenAI-Beta", "realtime=v1");

      client = new OpenAIRealtimeClient(uri, headers);
      client.connectBlocking();
      System.out.println("Conexão WebSocket estabelecida.");
    } catch (Exception e) {
      System.err.println("Falha ao estabelecer a conexão WebSocket:");
      e.printStackTrace();
    }
  }

  /**
   * Inicializa o listener para a palavra de ativação "Jarvis".
   */
  @SneakyThrows
  private static void initializeWakeWordListener() {
    String accessKey = loadPorcupineApiKey();

    try {
      wakeWordListener = new WakeWordListener(accessKey, BuiltInKeyword.JARVIS, Main::toggleRecordingWithTimer);
      audioCapture = new AudioCapture(new AudioFormat(
          16000.0f,
          16,
          1,
          true,
          false
      ));
      audioCapture.addAudioDataListener(wakeWordListener);
      audioCapture.start();
    } catch (PorcupineException | IOException | LineUnavailableException e) {
      System.err.println("Falha ao inicializar o listener da palavra de ativação:");
      e.printStackTrace();
    }
  }

  /**
   * Alterna o estado de gravação e atualiza o TrayIcon.
   */
  public static synchronized void toggleRecording() {
    if (isRecording) {
      sayHi();
      stopRecording();
      trayIconManager.updateIcon(false);
    } else {
      startRecording();
      trayIconManager.updateIcon(true);
    }
  }

  /**
   * Alterna a gravação e define um temporizador para parar após 15 segundos.
   */
  private static synchronized void toggleRecordingWithTimer() {
    toggleRecording();

    if (isRecording) {
      new Thread(() -> {
        try {
          Thread.sleep(15000);
          stopRecording();
          trayIconManager.updateIcon(false);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }).start();
    }
  }

  /**
   * Inicia a gravação de áudio e envia para o WebSocket (openAI)
   */
  private static void startRecording() {
    try {
      isRecording = true;

      recordingThread = new Thread(() -> {
        BlockingQueue<byte[]> queue = audioCapture.getAudioQueue();
        try {
          while (client.isOpen() && isRecording) {
            byte[] audioData = queue.take();
            client.sendAudio(audioData);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (Exception e) {
          System.err.println("Ocorreu um erro durante a gravação:");
          e.printStackTrace();
        } finally {
          stopRecording();
        }
      });
      recordingThread.start();

    } catch (Exception e) {
      System.err.println("Falha ao iniciar a gravação:");
      e.printStackTrace();
    }
  }

  @SneakyThrows
  private static void sayHi() {
    InputStream audioStream = Main.class.getResourceAsStream("/poisnao.mp3");

    if (audioStream == null) {
      System.out.println("MP3 file not found in resources.");
      return;
    }

    Player player = new Player(audioStream);
    player.play();

  }

  /**
   * Para a gravação de áudio.
   */
  private static void stopRecording() {
    isRecording = false;
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

  /**
   * Limpa e encerra todos os recursos utilizados pela aplicação.
   */
  public static void shutdown() {
    stopRecording();
    if (client != null) {
      try {
        client.closeBlocking();
        client = null;
        System.out.println("Conexão WebSocket fechada.");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    if (wakeWordListener != null) {
      wakeWordListener.stop();
    }
    if (audioCapture != null) {
      audioCapture.stop();
    }
  }
}
