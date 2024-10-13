package org.gmarques.model.openai.client;

import static org.gmarques.util.Constants.DEFAULT_INSTRUCTIONS;
import static org.gmarques.util.Constants.FUNCTION_CALL;

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

  private final ObjectMapper mapper = new ObjectMapper();
  private SourceDataLine audioLine;
  private AudioFormat format;


  public OpenAIRealtimeClient(URI serverUri, Map<String, String> httpHeaders) {
    super(serverUri, httpHeaders);
    mapper.setSerializationInclusion(
        com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
    mapper.configure(
        com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
  }

  @Override
  public void onOpen(ServerHandshake handshakedata) {
    System.out.println("Conectado.");
    sendResponseCreate();
    format = new AudioFormat(24000.0f, 16, 1, true, false);

    try {
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
      audioLine = (SourceDataLine) AudioSystem.getLine(info);
      audioLine.open(format);
      audioLine.start();
      System.out.println("Escutando..");
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Erro ao configurar a linha de áudio.");
    }
  }

  @Override
  public void onMessage(String message) {
    handleMessage(message);
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    System.out.println("Conexão fechada: " + reason);
    if (audioLine != null && audioLine.isOpen()) {
      audioLine.drain();
      audioLine.close();
    }
  }

  @Override
  public void onError(Exception ex) {
    System.out.println("Something wrong. An error occurred on the api call." + ex.getMessage());
  }

  private List<Tool> tools() {
    return FunctionFactory.functionMap.values().stream()
        .map(FunctionInterface::getTool)
        .collect(Collectors.toList());
  }

  private void sendResponseCreate() {
    try {
      var session = Session.builder()
          .modalities(List.of("text", "audio"))
          .instructions(DEFAULT_INSTRUCTIONS)
          .voice("alloy")
          .tools(tools())
          .tool_choice("auto")
          .temperature(0.9)
          .input_audio_format("pcm16")
          .output_audio_format("pcm16")
          .build();

      var event = new SessionEvent("session.update", null, null, session);
      String eventString = mapper.writeValueAsString(event);
      send(eventString);
      System.out.println("Evento enviado: " + eventString);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void sendAudio(byte[] audioData) {
    try {
      String audioBase64 = Base64.getEncoder().encodeToString(audioData);
      ObjectNode event = mapper.createObjectNode();
      event.put("type", "input_audio_buffer.append");
      event.put("audio", audioBase64);
      String eventString = mapper.writeValueAsString(event);
      send(eventString);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static final List<String> ignoredEvents = List.of("response.audio_transcript.delta",
      "response.audio.delta", "input_audio_buffer.committed", "input_audio_buffer.speech_stopped",
      "input_audio_buffer.speech_started", "response.output_item.added",
      "response.function_call_arguments.delta");

  public void handleMessage(String message) {
    try {
      JsonNode event = mapper.readTree(message);
      String eventType = event.get("type").asText();
      if(!ignoredEvents.contains(eventType)){
        System.out.println(event.toPrettyString());
      }


      switch (eventType) {
        case "response.create":

          break;
        case "error":
          System.out.println("Erro: " + event.get("message").asText());
          break;
        case "conversation.item.created":
          JsonNode item = event.get("item");
          if (item != null && "message".equals(item.get("type").asText())) {
            JsonNode contentArray = item.get("content");
            if (contentArray != null && contentArray.isArray()) {
              for (JsonNode content : contentArray) {
                String contentType = content.get("type").asText();
                if ("output_audio".equals(contentType)) {
                  String audioBase64 = content.get("audio").asText();
                  byte[] audioData = Base64.getDecoder().decode(audioBase64);

                } else if ("output_text".equals(contentType)) {
                  String text = content.get("text").asText();
                  System.out.println("Texto recebido: " + text);
                } else if ("function_call".equals(contentType)) {
                  String functionName = content.get("name").asText();
                  JsonNode functionArgs = content.get("arguments");
                  FunctionFactory.run(functionName, functionArgs);
                }
              }
            }
          }
          break;

        case FUNCTION_CALL:
          String functionName = event.get("name").asText();
          JsonNode functionArgs = event.get("arguments");
          FunctionFactory.run(functionName, functionArgs);
          System.out.println(event.toPrettyString());
          break;
        case "response.audio.delta":
          String deltaAudioBase64 = event.get("delta").asText();
          byte[] deltaAudioData = Base64.getDecoder().decode(deltaAudioBase64);
          AudioPlay.getInstance().playAudio(deltaAudioData, format);
          break;

        case "response.audio.done":
          if (audioLine != null && audioLine.isOpen()) {
            audioLine.drain();
            audioLine.close();
          }
          break;
        default:

          break;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void disconnectAndCloseAudio() {
    try {
      this.close();
      if (audioLine != null && audioLine.isOpen()) {
        audioLine.drain();
        audioLine.close();
        System.out.println("Linha de áudio fechada manualmente.");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
