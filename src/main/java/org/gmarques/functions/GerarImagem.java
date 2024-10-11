package org.gmarques.functions;

import static org.gmarques.model.openai.client.OpenAIService.callOpenAiChat;
import static org.gmarques.util.ProjectRegistry.getFolderStructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import org.gmarques.model.openai.objects.Tool;
import org.gmarques.util.ProjectRegistry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GerarImagem extends FunctionBase {

    @Override
    public String name() {
        return "gerar_imagem";
    }

    @Override
    public void run(JsonNode functionArgs) {
        try {
            JsonNode parsedArgs = mapper().readTree(functionArgs.asText());
            String nomeProjeto = parsedArgs.get("nome_projeto").asText();
            JsonNode descricoesNode = parsedArgs.get("descricoes");

            String descricoesJson = mapper().writeValueAsString(descricoesNode);

            System.out.println("Função chamada: " + name());
            System.out.println("Parâmetros: nome_projeto=" + nomeProjeto + ", descricoes=" + descricoesJson);
            execute(nomeProjeto, descricoesJson);
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
            .description(
                "Gera imagem(ns) em um projeto específico com base nas descrições fornecidas. Os projetos existentes hoje são: "
                    + ProjectRegistry.getAllProjectNames())
            .parameters(Map.of(
                "type", "object",
                "properties", Map.of(
                    "nome_projeto", Map.of(
                        "type", "string",
                        "description", "O nome do projeto onde as imagens serão geradas."
                    ),
                    "descricoes", Map.of(
                        "type", "array",
                        "items", Map.of(
                            "type", "string",
                            "description", "A descrição da imagem a ser gerada."
                        ),
                        "description", "Uma lista de descrições das imagens a serem geradas."
                    )
                ),
                "required", List.of("nome_projeto", "descricoes")
            ))
            .build();
    }

    @SneakyThrows
    @Override
    public void execute(String... parameters) {
        String nomeProjeto = parameters[0];
        String descricoesJson = parameters[1];

        String projectPath = ProjectRegistry.getProjectPath(nomeProjeto);
        if (projectPath == null) {
            System.out.println("Projeto não encontrado: " + nomeProjeto);
            return;
        }

        List<String> descricoes;
        try {
            descricoes = mapper().readValue(descricoesJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            System.out.println("Erro ao processar as descrições: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        String folderStructure = getFolderStructure(projectPath);

        List<CompletableFuture<Void>> futures = descricoes.stream()
            .map(descricao -> CompletableFuture.runAsync(() -> {
                String prompt = String.format(
                    "Crie uma imagem (extensão png) baseada na seguinte descrição: '%s'. " +
                        "Retorne somente um objeto JSON com os campos 'revised_prompt' e 'url', sem qualquer texto ou explicação adicional. " +
                        "A estrutura de pastas desse projeto é:\n%s",
                    descricao, folderStructure
                );

                var response = callOpenAiChat(prompt, "image");
                try {
                    try {
                        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                        desktop.browse(new java.net.URI(response));
                    } catch (Exception e) {
                        System.out.println("Erro ao abrir o navegador: " + e.getMessage());
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Erro ao processar a imagem.");
                }
            }))
            .toList();

        CompletableFuture<Void> allOf = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0]));
        try {
            allOf.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
