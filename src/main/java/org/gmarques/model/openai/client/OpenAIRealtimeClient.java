package org.gmarques.model.openai.client;

import static org.gmarques.util.Constants.DEFAULT_INSTRUCTIONS;
import static org.gmarques.util.Constants.FUNCTION_CALL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import org.gmarques.functions.FunctionFactory;
import org.gmarques.model.openai.events.SessionEvent;
import org.gmarques.model.openai.interfaces.FunctionInterface;
import org.gmarques.model.openai.objects.Session;
import org.gmarques.model.openai.objects.Tool;
import org.gmarques.service.AudioPlay;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class OpenAIRealtimeClient extends WebSocketClient {

  private final ObjectMapper mapper;
  private SourceDataLine audioLine;
  private AudioFormat format;
  private static final List<String> IGNORED_EVENTS = List.of(
      "response.audio_transcript.delta", "response.audio.delta", "input_audio_buffer.committed",
      "input_audio_buffer.speech_stopped", "input_audio_buffer.speech_started",
      "response.output_item.added", "response.function_call_arguments.delta");

  // Reconnection parameters
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private final AtomicBoolean shouldReconnect = new AtomicBoolean(true);
  private int reconnectAttempts = 0;
  private static final int MAX_RECONNECT_ATTEMPTS = 10;
  private static final long INITIAL_BACKOFF_MS = 1000; // 1 second
  private static final long MAX_BACKOFF_MS = 60000;    // 60 seconds

  public OpenAIRealtimeClient(URI serverUri, Map<String, String> httpHeaders) {
    super(serverUri, httpHeaders);
    mapper = new ObjectMapper();
    configureMapper();
  }

  private void configureMapper() {
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
  }

  @Override
  public void onOpen(ServerHandshake handshakedata) {
    System.out.println("Conectado.");
    reconnectAttempts = 0; // Reset reconnection attempts on successful connection
    sendResponseCreate();
    setupAudioLine();
  }

  private void setupAudioLine() {
    format = new AudioFormat(24000.0f, 16, 1, true, false);
    try {
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
      audioLine = (SourceDataLine) AudioSystem.getLine(info);
      audioLine.open(format);
      audioLine.start();
      System.out.println("Escutando..");
    } catch (LineUnavailableException e) {
      System.err.println("Erro ao configurar a linha de áudio: " + e.getMessage());
    }
  }

  @Override
  public void onMessage(String message) {
    handleMessage(message);
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    System.out.println("Conexão fechada: " + reason);
    closeAudioLineSafely();
    if (shouldReconnect.get()) {
      attemptReconnect();
    }
  }

  @Override
  public void onError(Exception ex) {
    System.err.println("Erro na chamada da API: " + ex.getMessage());
    if (shouldReconnect.get()) {
      attemptReconnect();
    }
  }

  private void sendResponseCreate() {
    try {
      Session session = createSession();
      String eventString = mapper.writeValueAsString(new SessionEvent("session.update", null, null, session));
      send(eventString);
      System.out.println("Evento enviado: " + eventString);
    } catch (JsonProcessingException e) {
      System.err.println("Erro ao enviar evento: " + e.getMessage());
    }
  }

  private Session createSession() {
    return Session.builder()
        .modalities(List.of("text", "audio"))
        .instructions(DEFAULT_INSTRUCTIONS)
        .voice("alloy")
        .tools(getTools())
        .tool_choice("auto")
        .temperature(0.9)
        .input_audio_format("pcm16")
        .output_audio_format("pcm16")
        .build();
  }

  private List<Tool> getTools() {
    return FunctionFactory.functionMap.values().stream()
        .map(FunctionInterface::getTool)
        .collect(Collectors.toList());
  }

  public void sendAudio(byte[] audioData) {
    try {
      String audioBase64 = Base64.getEncoder().encodeToString(audioData);
      ObjectNode event = mapper.createObjectNode();
      event.put("type", "input_audio_buffer.append");
      event.put("audio", audioBase64);
      send(mapper.writeValueAsString(event));
    } catch (JsonProcessingException e) {
      System.err.println("Erro ao enviar áudio: " + e.getMessage());
    }
  }

  public void handleMessage(String message) {
    try {
      JsonNode event = mapper.readTree(message);
      String eventType = event.get("type").asText();

      if (!IGNORED_EVENTS.contains(eventType)) {
        System.out.println(event.toPrettyString());
      }

      switch (eventType) {
        case "error":
          handleError(event);
          break;
        case "conversation.item.created":
          handleConversationItemCreated(event);
          break;
        case FUNCTION_CALL:
          handleFunctionCall(event);
          break;
        case "response.audio.delta":
          handleAudioDelta(event);
          break;
        case "response.audio.done":
          closeAudioLineSafely();
          break;
        default:
          break;
      }
    } catch (JsonProcessingException e) {
      System.err.println("Erro ao processar mensagem: " + e.getMessage());
    }
  }

  private void handleError(JsonNode event) {
    System.err.println("Erro: " + event.get("message").asText());
  }

  private void handleConversationItemCreated(JsonNode event) {
    JsonNode item = event.get("item");
    if (item != null && "message".equals(item.get("type").asText())) {
      for (JsonNode content : item.get("content")) {
        switch (content.get("type").asText()) {
          case "output_audio":
            playAudio(content.get("audio").asText());
            break;
          case "output_text":
            System.out.println("Texto recebido: " + content.get("text").asText());
            break;
          case "function_call":
            FunctionFactory.run(content.get("name").asText(), content.get("arguments"));
            break;
        }
      }
    }
  }

  private void handleFunctionCall(JsonNode event) {
    FunctionFactory.run(event.get("name").asText(), event.get("arguments"));
    System.out.println(event.toPrettyString());
  }

  private void handleAudioDelta(JsonNode event) {
    byte[] deltaAudioData = Base64.getDecoder().decode(event.get("delta").asText());
    AudioPlay.getInstance().playAudio(deltaAudioData, format);
  }

  private void playAudio(String audioBase64) {
    byte[] audioData = Base64.getDecoder().decode(audioBase64);
    AudioPlay.getInstance().playAudio(audioData, format);
  }

  private void closeAudioLineSafely() {
    if (audioLine != null && audioLine.isOpen()) {
      audioLine.drain();
      audioLine.close();
      System.out.println("Linha de áudio fechada.");
    }
  }

  /**
   * Attempts to reconnect with exponential backoff.
   */
  private void attemptReconnect() {
    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
      System.err.println("Número máximo de tentativas de reconexão atingido. Parando tentativas.");
      return;
    }

    reconnectAttempts++;
    long backoffTime = calculateExponentialBackoff(reconnectAttempts);
    System.out.println("Tentando reconectar em " + backoffTime + " ms (Tentativa " + reconnectAttempts + ")");

    scheduler.schedule(() -> {
      try {
        System.out.println("Tentativa de reconexão #" + reconnectAttempts);
        reconnect(); // Reconnect using WebSocketClient's reconnect method
      } catch (Exception e) {
        System.err.println("Erro ao tentar reconectar: " + e.getMessage());
        attemptReconnect(); // Schedule next reconnection attempt
      }
    }, backoffTime, TimeUnit.MILLISECONDS);
  }

  /**
   * Calculates exponential backoff delay.
   *
   * @param attempt Current reconnection attempt number.
   * @return Delay in milliseconds.
   */
  private long calculateExponentialBackoff(int attempt) {
    long delay = INITIAL_BACKOFF_MS * (long) Math.pow(2, attempt - 1);
    return Math.min(delay, MAX_BACKOFF_MS);
  }

  /**
   * Gracefully shuts down the client and stops reconnection attempts.
   */
  public void shutdown() {
    shouldReconnect.set(false);
    scheduler.shutdownNow();
    closeAudioLineSafely();
    close();
    System.out.println("Cliente WebSocket encerrado.");
  }
}
