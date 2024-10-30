package org.gmarques.functions;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.gmarques.util.ParameterBuilder;

@Log4j2
public class PesquisarLocal extends FunctionBase {

  @Override
  public String name() {
    return "pesquisar_local";
  }

  @Override
  public String description() {
    return "Realiza uma pesquisa local no computador com base na consulta fornecida.";
  }

  @Override
  public Map<String, Object> parameters() {
    return new ParameterBuilder()
        .addParameter("consulta", "string", "A consulta de pesquisa local no computador.")
        .addRequired("consulta")
        .build();
  }

  @Override
  protected void execute(JsonNode functionArgs) throws Exception {
    String consulta = functionArgs.get("consulta").asText();
    log.info("Executing function {}: consulta={}", name(), consulta);

    String caminhoEverything = "C:\\Program Files (x86)\\Everything\\Everything.exe";
    String comando = String.format("\"%s\" -search \"%s\"", caminhoEverything, consulta);
    Process process = Runtime.getRuntime().exec(comando);
    process.waitFor();
    log.info("Pesquisa local realizada com sucesso: {}", comando);
  }
}
