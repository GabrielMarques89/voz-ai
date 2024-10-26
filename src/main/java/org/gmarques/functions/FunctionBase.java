package org.gmarques.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.gmarques.model.interfaces.FunctionInterface;
import org.gmarques.model.openai.client.OpenAIService;
import org.gmarques.model.openai.enums.ModelType;

@Slf4j
public abstract class FunctionBase implements FunctionInterface {
    protected final String FUNCTION = "function";
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected void handleException(Exception e) {
        String errorMessage = "Ocorreu um erro na execução da função " + name() + ": " + e.getClass().getSimpleName();
        log.error(errorMessage, e);
        sendErrorMessageToOpenAI(errorMessage);
    }

    private void sendErrorMessageToOpenAI(String message) {
        try {
            OpenAIService.callOpenAiChat(message, ModelType.TEXT_TO_SPEECH);
        } catch (Exception ex) {
            log.error("Falha ao enviar mensagem de erro para o OpenAI", ex);
        }
    }

    public ObjectMapper mapper() {
        return objectMapper;
    }
}
