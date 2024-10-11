package org.gmarques.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import org.gmarques.model.openai.objects.Tool;

import java.awt.*;
import java.net.URI;
import java.util.List;
import java.util.Map;

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
                .description("Abre um site no navegador padrão. Favor fazer uma inferência a respeito da url do site a ser aberto")
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

    public static void openWebsite(String url) {
        try {
            if (!url.startsWith("http")) {
                url = "http://" + url;
            }
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            System.out.println("Erro ao abrir o site: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
