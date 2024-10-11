package org.gmarques.service;

import javax.sound.sampled.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioPlay {

  private static AudioPlay instance;
  private SourceDataLine audioLine;
  private AudioFormat format;
  private final BlockingQueue<AudioData> audioQueue;
  private volatile boolean isRunning;
  private final Object lock = new Object();

  private AudioPlay() {
    audioQueue = new LinkedBlockingQueue<>();
    isRunning = true;
    startPlaybackThread();
  }

  public static AudioPlay getInstance() {
    if (instance == null) {
      synchronized (AudioPlay.class) {
        if (instance == null) {
          instance = new AudioPlay();
        }
      }
    }
    return instance;
  }

  public void playAudio(byte[] audioData, AudioFormat format) {
    audioQueue.offer(new AudioData(audioData, format));
  }

  private void startPlaybackThread() {
    Thread playbackThread = new Thread(() -> {
      while (isRunning || !audioQueue.isEmpty()) {
        try {
          AudioData data = audioQueue.take(); // Bloqueia se a fila estiver vazia
          playAudioData(data);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          System.out.println("Thread de reprodução interrompida.");
        }
      }
      closeAudioLine();
    });
    playbackThread.setDaemon(true);
    playbackThread.start();
  }

  private void playAudioData(AudioData data) {
    try {
      synchronized (lock) {
        if (audioLine == null || !audioLine.isOpen() || !data.format.matches(format)) {
          if (audioLine != null && audioLine.isOpen()) {
            audioLine.drain();
            audioLine.stop();
            audioLine.close();
          }

          format = data.format;
          DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
          audioLine = (SourceDataLine) AudioSystem.getLine(info);
          audioLine.open(format);
          audioLine.start();
        }

        audioLine.write(data.audioData, 0, data.audioData.length);
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Erro ao escrever dados de áudio na linha de áudio.");
    }
  }

  public void stop() {
    isRunning = false;
  }

  private void closeAudioLine() {
    synchronized (lock) {
      if (audioLine != null) {
        audioLine.drain();
        audioLine.stop();
        audioLine.close();
        audioLine = null;
        System.out.println("Linha de áudio fechada.");
      }
    }
  }

  // Classe interna para encapsular dados de áudio e formato
  private static class AudioData {
    byte[] audioData;
    AudioFormat format;

    AudioData(byte[] audioData, AudioFormat format) {
      this.audioData = audioData;
      this.format = format;
    }
  }
}
