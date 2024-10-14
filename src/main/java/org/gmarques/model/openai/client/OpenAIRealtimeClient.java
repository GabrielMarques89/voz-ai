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
  }

  @Override
  public void onError(Exception ex) {
    System.err.println("Erro na chamada da API: " + ex.getMessage());
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

  public void disconnectAndCloseAudio() {
    this.close();
    closeAudioLineSafely();
  }
}