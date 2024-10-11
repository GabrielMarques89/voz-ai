package org.gmarques.model.openai.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gmarques.model.openai.enums.ToolType;
import org.gmarques.model.openai.interfaces.FunctionInterface;
import org.gmarques.model.openai.objects.Tool;

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
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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
}
