package org.gmarques.functions;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import org.gmarques.model.openai.objects.Tool;

public class PesquisarLocal extends FunctionBase {
  public String name() {
    return "pesquisar_local";
  }

  public Tool getTool() {
    return Tool.builder()
        .name(name())
        .type(FUNCTION)
        .description("Realiza uma pesquisa local no computador com base na consulta fornecida.")
        .parameters(Map.of(
            "type", "object",
            "properties", Map.of(
                "consulta", Map.of(
                    "type", "string",
                    "description", "A consulta de pesquisa local no computador."
                )
            ),
            "required", List.of("consulta")
        ))
        .build();
  }

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
  public void execute(String... parameters) {
    try {
      String caminhoEverything = "C:\\Program Files (x86)\\Everything\\Everything.exe";
      String comando = String.format("\"%s\" -search \"%s\"", caminhoEverything, parameters[0]);
      Process process = Runtime.getRuntime().exec(comando);
      process.waitFor();
      System.out.println("Pesquisa local realizada com sucesso: " + comando);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Erro ao realizar pesquisa local.");
    }
  }
}
