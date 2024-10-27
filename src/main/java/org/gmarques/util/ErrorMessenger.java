package org.gmarques.util;

import lombok.extern.slf4j.Slf4j;
import org.gmarques.model.openai.client.OpenAIService;
import org.gmarques.model.openai.enums.ModelType;

@Slf4j
public class ErrorMessenger {
    public static void sendErrorMessageToOpenAI(String message) {
        try {
            OpenAIService.callOpenAiChat(message, ModelType.TEXT_TO_SPEECH);
        } catch (Exception ex) {
            log.error("Failed to send error message to OpenAI", ex);
        }
    }
}
