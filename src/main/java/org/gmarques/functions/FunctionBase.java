package org.gmarques.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.gmarques.model.interfaces.FunctionInterface;
import org.gmarques.model.openai.objects.Tool;
import org.gmarques.util.ErrorMessenger;

@Slf4j
public abstract class FunctionBase implements FunctionInterface {
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(JsonNode functionArgs) {
        try {
            execute(functionArgs);
        } catch (Exception e) {
            handleException(e);
        }
    }

    protected abstract void execute(JsonNode functionArgs) throws Exception;

    protected void handleException(Exception e) {
        String errorMessage = "An error occurred in function " + name() + ": " + e.getMessage();
        log.error(errorMessage, e);
        ErrorMessenger.sendErrorMessageToOpenAI(errorMessage);
    }

    public Tool getTool() {
        return Tool.builder()
            .name(name())
            .type("function")
            .description(description())
            .parameters(parameters())
            .build();
    }
}
