package org.gmarques.model.openai.client;

import static org.gmarques.config.ApiKeyLoader.loadApiKey;
import static org.gmarques.util.Constants.DALL_E_3;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import org.gmarques.model.openai.objects.ImageGenerationRequest;
import org.gmarques.model.openai.objects.ImageGenerationResponse;
import org.gmarques.model.openai.objects.chatapi.ChatCompletionRequest;
import org.gmarques.model.openai.objects.chatapi.ChatCompletionResponse;
import org.gmarques.model.openai.objects.chatapi.Message;
import org.gmarques.util.Constants;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;

public class OpenAIService {

  private static final OkHttpClient client = createHttpClient();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static String callOpenAiChat(String prompt, String promptType) {
    String model = determineModel(prompt, promptType);

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

  private static String determineModel(String prompt, String promptType) {
    return promptType.equals("chat") || prompt == null ? Constants.TEXT_MODEL_4_o : DALL_E_3;
  }

  @NotNull
  private static Request createRequest(String model, String prompt) throws IOException {
    String endpointUrl = getModelUrl(model);
    String jsonInputString = createRequestBodyJson(model, prompt);
    RequestBody body = RequestBody.create(
        MediaType.get("application/json; charset=utf-8"),
        jsonInputString
    );

    return new Request.Builder()
        .url(endpointUrl)
        .post(body)
        .addHeader("Authorization", "Bearer " + loadApiKey())
        .addHeader("Content-Type", "application/json")
        .build();
  }

  private static String createRequestBodyJson(String model, String prompt) throws IOException {
    if (model.equals(Constants.TEXT_MODEL_4_o)) {
      ChatCompletionRequest requestData = new ChatCompletionRequest(
          model,
          List.of(new Message("user", prompt)),
          2000,
          null
      );
      return objectMapper.writeValueAsString(requestData);
    } else {
      ImageGenerationRequest requestData = new ImageGenerationRequest(
          model,
          prompt,
          1,
          "1024x1024"
      );
      return objectMapper.writeValueAsString(requestData);
    }
  }

  private static String executeRequest(Request request, String model) throws IOException {
    try (Response response = client.newCall(request).execute()) {
      System.out.println("Response from API: " + response);
      if (!response.isSuccessful()) {
        throw new IOException("Unexpected code " + response);
      }

      String jsonResponse = response.body().string();
      return parseResponse(jsonResponse, model);
    }
  }

  private static String parseResponse(String jsonResponse, String model) throws IOException {
    if (model.equals(Constants.TEXT_MODEL_4_o)) {
      ChatCompletionResponse completionResponse = objectMapper.readValue(jsonResponse,
          ChatCompletionResponse.class);

      if (completionResponse.choices == null || completionResponse.choices.isEmpty()) {
        System.out.println("Error, response is empty");
        throw new IOException("Empty response from API");
      }
      return completionResponse.choices.get(0).message.content;
    } else {
      ImageGenerationResponse imageResponse = objectMapper.readValue(jsonResponse,
          ImageGenerationResponse.class);

      if (imageResponse.data == null || imageResponse.data.isEmpty()) {
        System.out.println("Error, response is empty");
        throw new IOException("Empty response from API");
      }
      return imageResponse.data.get(0).url;
    }
  }

  @NotNull
  private static String getModelUrl(String model) {
    return model.equals(Constants.TEXT_MODEL_4_o) ? Constants.TEXT_MODEL_URL
        : Constants.IMAGE_MODEL_URL;
  }
}