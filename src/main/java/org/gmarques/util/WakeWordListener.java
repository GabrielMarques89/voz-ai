package org.gmarques.util;

import ai.picovoice.porcupine.Porcupine;
import ai.picovoice.porcupine.Porcupine.BuiltInKeyword;
import ai.picovoice.porcupine.PorcupineException;
import java.io.IOException;
import org.gmarques.service.AudioCapture;

public class WakeWordListener implements AudioCapture.AudioDataListener {

    private Porcupine porcupine;
    private final Runnable onWakeWordDetected;

    /**
     * Constructor to initialize WakeWordListener with Porcupine.
     *
     * @param accessKey           Picovoice Access Key.
     * @param keywordPath         Path to the wake word keyword file.
     * @param onWakeWordDetected  Runnable to execute upon wake word detection.
     * @throws PorcupineException  If Porcupine initialization fails.
     * @throws IOException         If there's an I/O error.
     */
    public WakeWordListener(String accessKey, BuiltInKeyword keywordPath, Runnable onWakeWordDetected) throws PorcupineException, IOException {
        this.onWakeWordDetected = onWakeWordDetected;

        
        porcupine = new Porcupine.Builder()
            .setAccessKey(accessKey)
            .setSensitivity(0.9f)
            .setBuiltInKeyword(keywordPath)
            .build();
    }

    /**
     * Processes incoming audio data for wake word detection.
     *
     * @param data Audio data bytes.
     */
    @Override
    public void onAudioData(byte[] data) {
        
        short[] pcm = new short[data.length / 2];
        for (int i = 0; i < pcm.length; i++) {
            int low = data[2 * i] & 0xFF;
            int high = data[2 * i + 1] << 8;
            pcm[i] = (short) (high | low);
        }

        try {
            if (porcupine.process(pcm) >= 0) {
                System.out.println("Wake word 'Jarvis' detected.");
                onWakeWordDetected.run();
            }
        } catch (PorcupineException e) {
            System.err.println("Porcupine processing error:");
            e.printStackTrace();
        }
    }

    /**
     * Stops and releases Porcupine resources.
     */
    public void stop() {
        if (porcupine != null) {
            porcupine.delete();
            porcupine = null;
            System.out.println("Wake word listener stopped.");
        }
    }
}
