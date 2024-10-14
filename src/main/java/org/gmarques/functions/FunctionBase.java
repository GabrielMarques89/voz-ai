package org.gmarques.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.gmarques.model.openai.client.OpenAIService;
import org.gmarques.model.openai.enums.ModelType;
import org.gmarques.model.openai.interfaces.FunctionInterface;
import org.gmarques.model.openai.objects.Tool;

@Slf4j
public abstract class FunctionBase implements FunctionInterface {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    protected static final String FUNCTION = "function";

    @Override
    public void run(JsonNode functionArgs){
        JsonNode parsedArgs = null;
        try {
            parsedArgs = mapper().readTree(functionArgs.asText());
            String consulta = parsedArgs.get("consulta").asText();
            System.out.println("Função chamada: " + name());
            System.out.println("Parâmetro: " + consulta);
            execute(consulta);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public Tool getTool() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectMapper mapper() {
        return objectMapper;
    }

    protected void handleException(Exception e) {
        String errorMessage = "Ocorreu um erro na execução da função " + name() + ": " + e.getClass().getSimpleName();
        System.out.println(errorMessage);
        e.printStackTrace();

        sendErrorMessageToOpenAI(errorMessage);
    }

    @SneakyThrows
    private void sendErrorMessageToOpenAI(String message) {
        OpenAIService.callOpenAiChat(message, ModelType.textToSpeech);
    }
}
