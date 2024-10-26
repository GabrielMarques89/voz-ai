package org.gmarques.functions;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.gmarques.model.openai.objects.Tool;

public class GoogleSearch extends FunctionBase {

  public static final String GOOGLE_URL = "https://www.google.com/search?q=";

  public String name() {
    return "pesquisar_google";
  }

  public Tool getTool() {
    return Tool.builder()
        .name(name())
        .type(FUNCTION)
        .description("Realiza uma pesquisa no Google com base na consulta fornecida. Responda o usuário antes de chamar a ferramenta")
        .parameters(Map.of(
            "type", "object",
            "properties", Map.of(
                "consulta", Map.of(
                    "type", "string",
                    "description", "A consulta de pesquisa para o Google."
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
    var consulta = parameters[0];
    var encodedQuery = java.net.URLEncoder.encode(consulta,StandardCharsets.UTF_8);
    try {
      String url = GOOGLE_URL + encodedQuery;
      java.awt.Desktop.getDesktop().browse(new URI(url));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
