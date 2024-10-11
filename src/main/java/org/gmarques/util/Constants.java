package org.gmarques.util;

public class Constants {
  public static final String MODEL = "gpt-4o-realtime-preview-2024-10-01";
  public static final String TEXT_MODEL_4_o = "gpt-4o";
  public static final String TEXT_MODEL_o1_mini = "o1-mini";
  public static final String TEXT_MODEL_o1_preview = "o1-preview";
  public static final String TEXT_MODEL_URL = "https://api.openai.com/v1/chat/completions";
  public static final String API_URL = "wss://api.openai.com/v1/realtime?model=" + MODEL;
  public static final String SHORTCUT_KEY = "F12"; // Define your shortcut key here
  public static final String DEFAULT_INSTRUCTIONS = "Você deve responder em português brasileiro de forma descontraída e amigável. Utilize um tom informal. Sempre que possível, utilize as funções disponíveis para executar ações no computador. Retorne apenas a confirmação em áudio e utilize as funções para ações específicas, sem fornecer explicações adicionais.";
  public static final String FUNCTION_CALL = "response.function_call_arguments.done";
}
