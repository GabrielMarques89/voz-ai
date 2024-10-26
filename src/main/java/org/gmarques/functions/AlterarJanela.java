package org.gmarques.functions;

import static org.gmarques.util.WindowHelper.alterarJanelaWindows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import org.gmarques.model.openai.objects.Tool;

public class AlterarJanela extends FunctionBase {
    public String name() {
        return "alterar_janela";
    }

    public Tool getTool() {
        return Tool.builder()
            .name(name())
            .type("function")
            .description("Altera a janela ativa no Windows com base no nome do aplicativo ou título da janela fornecido.")
            .parameters(Map.of(
                "type", "object",
                "properties", Map.of(
                    "nome_janela", Map.of(
                        "type", "string",
                        "description", "O nome do aplicativo ou parte do título da janela para a qual mudar o foco."
                    )
                ),
                "required", List.of("nome_janela")
            ))
            .build();
    }

    @Override
    public void run(JsonNode functionArgs){
        JsonNode parsedArgs = null;
        try {
            parsedArgs = mapper().readTree(functionArgs.asText());
            String consulta = parsedArgs.get("nome_janela").asText();
            System.out.println("Função chamada: " + this.name());
            System.out.println("Parâmetro: " + consulta);
            execute(consulta);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute(String... parameters) {
        String nomeJanela = parameters[0];
        System.out.println("Alterando para a janela: " + nomeJanela);
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            alterarJanelaWindows(nomeJanela);
        } else {
            System.out.println("Esta funcionalidade é suportada apenas no Windows.");
        }
    }
}
