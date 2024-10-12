package org.gmarques.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import lombok.Getter;

public class AudioCapture {

    private final TargetDataLine targetDataLine;
    private final AudioFormat audioFormat;
    private Thread captureThread;
    private boolean running = false;
    private final List<AudioDataListener> listeners = new ArrayList<>();
  /**
   * -- GETTER --
   *  Returns the audio data queue for processing.
   *
   * @return BlockingQueue of audio data bytes.
   */
  @Getter
  private final BlockingQueue<byte[]> audioQueue = new LinkedBlockingQueue<>();

    public interface AudioDataListener {
        void onAudioData(byte[] data);
    }

    /**
     * Constructor to initialize AudioCapture with a specific AudioFormat.
     *
     * @param format Desired AudioFormat for capturing audio.
     * @throws LineUnavailableException If the audio line cannot be opened.
     */
    public AudioCapture(AudioFormat format) throws LineUnavailableException {
        this.audioFormat = format;

        // Select the specific mixer for your microphone
        Mixer.Info selectedMixerInfo = null;
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixerInfos) {
            if (mixerInfo.getName().contains("Trust GXT 232 Microphone")) {
                selectedMixerInfo = mixerInfo;
                break;
            }
        }

        if (selectedMixerInfo == null) {
            throw new LineUnavailableException("Trust GXT 232 Microphone mixer not found.");
        }

        Mixer mixer = AudioSystem.getMixer(selectedMixerInfo);
        DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

        if (!mixer.isLineSupported(lineInfo)) {
            throw new LineUnavailableException("The Trust GXT 232 Microphone does not support the specified format.");
        }

        targetDataLine = (TargetDataLine) mixer.getLine(lineInfo);
        targetDataLine.open(audioFormat);
    }

    /**
     * Adds an AudioDataListener to receive audio data.
     *
     * @param listener Listener to receive audio data.
     */
    public void addAudioDataListener(AudioDataListener listener) {
        listeners.add(listener);
    }

  /**
     * Starts the audio capture process.
     */
    public void start() {
        targetDataLine.start();
        running = true;

        captureThread = new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (running) {
                int count = targetDataLine.read(buffer, 0, buffer.length);
                if (count > 0) {
                    byte[] data = new byte[count];
                    System.arraycopy(buffer, 0, data, 0, count);
                    // Notify all listeners
                    for (AudioDataListener listener : listeners) {
                        listener.onAudioData(data);
                    }
                    // Add to queue for recording
                    audioQueue.offer(data);
                }
            }
        });
        captureThread.start();
    }

    /**
     * Stops the audio capture process.
     */
    public void stop() {
        running = false;
        targetDataLine.stop();
        targetDataLine.close();
        try {
            captureThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
