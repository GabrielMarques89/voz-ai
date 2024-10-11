package org.gmarques.model.openai.client;

import static org.gmarques.config.ApiKeyLoader.loadApiKey;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import org.gmarques.model.openai.objects.chatapi.ChatCompletionRequest;
import org.gmarques.model.openai.objects.chatapi.ChatCompletionResponse;
import org.gmarques.model.openai.objects.chatapi.Message;
import org.gmarques.util.Constants;

public class OpenAIService {

  private static final OkHttpClient client = new OkHttpClient.Builder()
          .connectTimeout(30, TimeUnit.SECONDS)  // Timeout de conexão
          .readTimeout(60, TimeUnit.SECONDS)     // Timeout de leitura
          .writeTimeout(60, TimeUnit.SECONDS)    // Timeout de escrita
          .build();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static String callOpenAiChat(String prompt) {
    try {
      ChatCompletionRequest requestData = new ChatCompletionRequest(
          Constants.TEXT_MODEL_4_o,
          List.of(new Message("user", prompt)),
          2000,
          null
      );

      String jsonInputString = objectMapper.writeValueAsString(requestData);

      RequestBody body = RequestBody.create(
          MediaType.get("application/json; charset=utf-8"),
          jsonInputString
      );

      Request request = new Request.Builder()
          .url(Constants.TEXT_MODEL_URL)
          .post(body)
          .addHeader("Authorization", "Bearer " + loadApiKey())
          .addHeader("Content-Type", "application/json")
          .build();


      System.out.println("Sending request to OpenAI: " + objectMapper.writeValueAsString(body));
      try (Response response = client.newCall(request).execute()) {
        System.out.println("Response from chat api: " + response);
        if (!response.isSuccessful()) {
          throw new IOException("Unexpected code " + response);
        }

        String jsonResponse = response.body().string();
        ChatCompletionResponse completionResponse = objectMapper.readValue(jsonResponse,
            ChatCompletionResponse.class);

        if (completionResponse.choices == null || completionResponse.choices.isEmpty()) {
          System.out.println("Error, response is empty");
        }
        return completionResponse.choices.get(0).message.content;
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Erro ao processar a solicitação.");
    }
    throw new RuntimeException("error calling openai");
  }
}
