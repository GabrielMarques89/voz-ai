package org.gmarques.model.openai.objects.chatapi;

import java.util.List;

public class ChatCompletionRequest {

  public String model;
  public List<Message> messages;
  public int max_tokens;
  public List<String> stop;

  public ChatCompletionRequest(String model, List<Message> messages, int max_tokens,
      List<String> stop) {
    this.model = model;
    this.messages = messages;
    this.max_tokens = max_tokens;
    this.stop = stop;
  }
}


