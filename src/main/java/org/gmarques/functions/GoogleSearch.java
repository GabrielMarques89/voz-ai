package org.gmarques.functions;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.gmarques.util.ParameterBuilder;

@Log4j2
public class GoogleSearch extends FunctionBase {

  private static final String GOOGLE_URL = "https://www.google.com/search?q=";

  @Override
  public String name() {
    return "pesquisar_google";
  }

  @Override
  public String description() {
    return "Realiza uma pesquisa no Google com base na consulta fornecida. Responda ao usuário antes de chamar a ferramenta.";
  }

  @Override
  public Map<String, Object> parameters() {
    return new ParameterBuilder()
        .addParameter("consulta", "string", "A consulta de pesquisa para o Google.")
        .addRequired("consulta")
        .build();
  }

  @Override
  protected void execute(JsonNode functionArgs) throws Exception {
    var parsedArgs = objectMapper.readTree(functionArgs.asText());
    String consulta = parsedArgs.get("consulta").asText();
    log.info("Executing function {}: consulta={}", name(), consulta);

    String encodedQuery = URLEncoder.encode(consulta, StandardCharsets.UTF_8);
    String url = GOOGLE_URL + encodedQuery;

    java.awt.Desktop.getDesktop().browse(new URI(url));
  }

}