package org.gmarques.functions;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.gmarques.personalizations.Websites;
import org.gmarques.util.ParameterBuilder;
import org.gmarques.util.WebsiteOpener;

@Log4j2
public class AbrirSite extends FunctionBase {
    @Override
    public String name() {
        return "abrir_site";
    }

    @Override
    public String description() {
        return "Abre um site no navegador padrão. Exemplos: " + Websites.siteMap +
            ". Pode ajustar variáveis dos exemplos, como \"$VARIAVEL$\" de acordo com o prompt. " +
            "Caso não encontre nos exemplos, favor fazer uma inferência a respeito da URL em sites comuns.";
    }

    @Override
    public Map<String, Object> parameters() {
        return new ParameterBuilder()
            .addParameter("url", "string", "A URL do site a ser aberto.")
            .addRequired("url")
            .build();
    }

    @Override
    protected void execute(JsonNode functionArgs) throws Exception {
        String url = functionArgs.get("url").asText();
        log.info("Executing function {}: url={}", name(), url);
        WebsiteOpener.openWebsite(url);
        log.info("Website opened: {}", url);
    }
}
