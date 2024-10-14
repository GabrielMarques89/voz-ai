package org.gmarques.model.openai.enums;

import org.gmarques.util.Constants;

public enum ModelType {
  
  text("text", Constants.TEXT_MODEL_URL),
  image("image", Constants.IMAGE_MODEL_URL),
  voice("voice", Constants.VOICE_MODEL_URL),
  textToSpeech("text-to-speech",Constants.TTS_MODEL_URL);

  private final String value;
  private final String url;

  ModelType(String value, String url) {
    this.value = value;
    this.url = url;
  }

  public String getValue() {
    return value;
  }

  public String getUrl() {
    return url;
  }
}
