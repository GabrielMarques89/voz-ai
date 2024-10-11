package org.gmarques.service;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AudioCapture {
    private TargetDataLine line;
    private AudioFormat format;
    private boolean capturing;
    private ByteArrayOutputStream out;

    public AudioCapture() {
        format = new AudioFormat(16000.0f, 16, 1, true, false);
    }

    public void start() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("The system does not support the specified format.");
        }
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        capturing = true;
        out = new ByteArrayOutputStream();
    }

    public byte[] read() throws IOException {
        if (!capturing) {
            return null;
        }
        byte[] buffer = new byte[4096];
        int bytesRead = line.read(buffer, 0, buffer.length);
        if (bytesRead > 0) {
            out.write(buffer, 0, bytesRead);
            return buffer;
        }
        return null;
    }

    public void stop() {
        capturing = false;
        if (line != null) {
            line.stop();
            line.close();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
