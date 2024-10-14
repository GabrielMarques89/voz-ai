package org.gmarques.model.openai.client;

import static org.gmarques.config.ApiKeyLoader.loadApiKey;
import static org.gmarques.service.AudioPlay.playInputStream;
import static org.gmarques.util.Constants.TTS_MODEL;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.gmarques.model.openai.enums.ModelType;
import org.gmarques.model.openai.objects.ImageGenerationRequest;
import org.gmarques.model.openai.objects.ImageGenerationResponse;
import org.gmarques.model.openai.objects.chatapi.ChatCompletionRequest;
import org.gmarques.model.openai.objects.chatapi.ChatCompletionResponse;
import org.gmarques.model.openai.objects.chatapi.Message;
import org.jetbrains.annotations.NotNull;

public class OpenAIService {

  private static final OkHttpClient client = createHttpClient();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static String callOpenAiChat(String prompt, ModelType model) {
    try {
      Request request = createRequest(model, prompt);
      return executeRequest(request, model);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error processing the request.");
      throw new RuntimeException("Error calling OpenAI");
    }
  }

  @NotNull
  private static OkHttpClient createHttpClient() {
    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

    return new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build();
  }

  @NotNull
  private static Request createRequest(ModelType model, String prompt) throws IOException {
    String jsonInputString = createRequestBodyJson(model, prompt);
    RequestBody body = RequestBody.create(
        MediaType.get("application/json; charset=utf-8"),
        jsonInputString
    );

    return buildRequest(model.getUrl(), body);
  }

  @NotNull
  private static Request buildRequest(String endpointUrl, RequestBody body) throws IOException {
    return new Request.Builder()
        .url(endpointUrl)
        .post(body)
        .addHeader("Authorization", "Bearer " + loadApiKey())
        .addHeader("Content-Type", "application/json")
        .build();
  }

  private static String createRequestBodyJson(ModelType model, String prompt) throws IOException {
    switch (model) {
      case ModelType.text -> {
        ChatCompletionRequest requestData = new ChatCompletionRequest(
            model.getValue(),
            List.of(new Message("user", prompt)),
            2000,
            null
        );
        return objectMapper.writeValueAsString(requestData);
      }
      case ModelType.textToSpeech -> {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("model", TTS_MODEL);
        requestData.put("input", prompt);
        requestData.put("voice", "alloy");
        return objectMapper.writeValueAsString(requestData);
      }
      case ModelType.image -> {
        ImageGenerationRequest requestData = new ImageGenerationRequest(
            model.getValue(),
            prompt,
            1,
            "1024x1024"
        );
        return objectMapper.writeValueAsString(requestData);
      }
      default -> {
        throw new IOException("Model not supported");
      }
    }
  }

  private static String executeRequest(Request request, ModelType model) throws IOException {
    try (Response response = client.newCall(request).execute()) {
      System.out.println("Response from API: " + response);
      if (!response.isSuccessful()) {
        throw new IOException("Unexpected code " + response);
      }

      if (model == ModelType.textToSpeech) {
        playAudioResponse(response);
        return "Audio played";
      } else {
        String jsonResponse = response.body().string();
        return parseResponse(jsonResponse, model);
      }
    }
  }

  private static void playAudioResponse(Response response) throws IOException {
    InputStream inputStream = response.body().byteStream();
    playInputStream(inputStream);
  }

  private static String parseResponse(String jsonResponse, ModelType model) throws IOException {
    switch (model) {
      case ModelType.text -> {
        ChatCompletionResponse completionResponse = objectMapper.readValue(jsonResponse, ChatCompletionResponse.class);

        if (completionResponse.choices == null || completionResponse.choices.isEmpty()) {
          System.out.println("Error, response is empty");
          throw new IOException("Empty response from API");
        }
        return completionResponse.choices.get(0).message.content;
      }
      case ModelType.image -> {
        ImageGenerationResponse imageResponse = objectMapper.readValue(jsonResponse, ImageGenerationResponse.class);

        if (imageResponse.data == null || imageResponse.data.isEmpty()) {
          System.out.println("Error, response is empty");
          throw new IOException("Empty response from API");
        }
        return imageResponse.data.get(0).url;
      }
      default -> {
        throw new IOException("Model not supported");
      }
    }
  }
}
