package org.gmarques.util;

public class Constants {
  public static final String MODEL = "gpt-4o-realtime-preview-2024-10-01";
  public static final String TEXT_MODEL_4_o = "gpt-4o";
  public static final String DALL_E_3 = "dall-e-3";
  public static final String TEXT_MODEL_o1_mini = "o1-mini";
  public static final String TEXT_MODEL_o1_preview = "o1-preview";
  public static final String TEXT_MODEL_URL = "https://api.openai.com/v1/chat/completions";
  public static final String IMAGE_MODEL_URL = "https://api.openai.com/v1/images/generations";
  public static final String VOICE_MODEL_URL = "wss://api.openai.com/v1/realtime?model=" + MODEL;
  public static final String DEFAULT_INSTRUCTIONS = "Você deve responder em português brasileiro de forma descontraída e amigável. Utilize um tom informal. Sempre que possível, utilize as funções disponíveis para executar ações no computador. Sempre retorne também a confirmação em áudio e utilize as funções para ações específicas, sem fornecer explicações adicionais.";
  public static final String FUNCTION_CALL = "response.function_call_arguments.done";
  public static final String TTS_MODEL = "tts-1";
  public static final String TTS_MODEL_URL = "https://api.openai.com/v1/audio/speech";

}
