package org.gmarques.functions;

import static org.gmarques.util.WindowHelper.alterarJanelaWindows;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.gmarques.util.ParameterBuilder;

@Log4j2
public class AlterarJanela extends FunctionBase {
    @Override
    public String name() {
        return "alterar_janela";
    }

    @Override
    public String description() {
        return "Altera a janela ativa no Windows com base no nome do aplicativo ou título da janela fornecido.";
    }

    @Override
    public Map<String, Object> parameters() {
        return new ParameterBuilder()
            .addParameter("nome_janela", "string", "O nome do aplicativo ou parte do título da janela para a qual mudar o foco.")
            .addRequired("nome_janela")
            .build();
    }

    @Override
    protected void execute(JsonNode functionArgs) throws Exception {
        String nomeJanela = functionArgs.get("nome_janela").asText();
        log.info("Executing function {}: nome_janela={}", name(), nomeJanela);

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            alterarJanelaWindows(nomeJanela);
        } else {
            log.warn("Esta funcionalidade é suportada apenas no Windows.");
        }
    }
}
