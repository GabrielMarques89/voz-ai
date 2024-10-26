package org.gmarques.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.gmarques.model.openai.objects.Tool;
import org.gmarques.personalizations.Websites;

public class AbrirSite extends FunctionBase {
    @Override
    public String name() {
        return "abrir_site";
    }

    @Override
    public void run(JsonNode functionArgs) {
        try {
            JsonNode parsedArgs = mapper().readTree(functionArgs.asText());
            String url = parsedArgs.get("url").asText();

            System.out.println("Função chamada: " + name());
            System.out.println("Parâmetros: url=" + url);
            execute(url);
        } catch (JsonProcessingException e) {
            System.out.println("Um erro ocorreu na função run: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Tool getTool() {
        return Tool.builder()
                .name(name())
                .type("function")
                .description("Abre um site no navegador padrão. Exemplos: " + Websites.siteMap + ". Pode ajustar variáveis dos exemplos, como \"$VARIAVEL$\" de acordo com o prompt. Caso não encontre nos exemplos favor fazer uma inferência a respeito da url em sites comuns")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "url", Map.of(
                                        "type", "string",
                                        "description", "A URL do site a ser aberto."
                                )
                        ),
                        "required", List.of("url")
                ))
                .build();
    }

    @SneakyThrows
    @Override
    public void execute(String... parameters) {
        String url = parameters[0];
        openWebsite(url);
        System.out.println("Site aberto: " + url);
    }

    public static void openWebsite(String url) throws URISyntaxException, IOException {
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        Desktop.getDesktop().browse(new URI(url));
    }
}
