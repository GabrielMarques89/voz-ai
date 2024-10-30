package org.gmarques.functions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.extern.log4j.Log4j2;
import org.gmarques.model.openai.client.OpenAIService;
import org.gmarques.model.openai.enums.ModelType;
import org.gmarques.util.ParameterBuilder;
import org.gmarques.util.ProjectRegistry;

@Log4j2
public class GerarImagem extends FunctionBase {

    @Override
    public String name() {
        return "gerar_imagem";
    }

    @Override
    public String description() {
        return "Gera imagem(ns) em um projeto específico com base nas descrições fornecidas. Os projetos existentes hoje são: " + ProjectRegistry.getAllProjectNames();
    }

    @Override
    public Map<String, Object> parameters() {
        return new ParameterBuilder()
            .addParameter("nome_projeto", "string", "O nome do projeto onde as imagens serão geradas.")
            .addParameter("descricão", "string", "Descrição da imagen a ser gerada.")
            .addRequired("nome_projeto")
            .addRequired("descricoes")
            .build();
    }

    @Override
    protected void execute(JsonNode functionArgs) throws Exception {
        String nomeProjeto = functionArgs.get("nome_projeto").asText();
        List<String> descricoes = objectMapper.convertValue(functionArgs.get("descricoes"), new TypeReference<List<String>>() {});

        log.info("Executing function {}: nome_projeto={}, descricoes={}", name(), nomeProjeto, descricoes);

        String projectPath = ProjectRegistry.getProjectPath(nomeProjeto);
        if (projectPath == null) {
            log.warn("Projeto não encontrado: {}", nomeProjeto);
            return;
        }

        String folderStructure = getFolderStructure(projectPath);

        List<CompletableFuture<Void>> futures = descricoes.stream()
            .map(descricao -> CompletableFuture.runAsync(() -> {
                try {
                    processDescricao(descricao, folderStructure);
                } catch (Exception e) {
                    log.error("Erro ao processar a descrição: {}", descricao, e);
                }
            }))
            .toList();

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.get();
    }

    private void processDescricao(String descricao, String folderStructure) throws Exception {
        String prompt = String.format(
            "Crie uma imagem (extensão png) baseada na seguinte descrição: '%s'. " +
                "Retorne somente um objeto JSON com os campos 'revised_prompt' e 'url', sem qualquer texto ou explicação adicional. " +
                "A estrutura de pastas desse projeto é:\n%s",
            descricao, folderStructure
        );

        String response = OpenAIService.callOpenAiChat(prompt, ModelType.image);

        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        desktop.browse(new java.net.URI(response));
    }

    private String getFolderStructure(String projectPath) {
        // Implement method to get folder structure
        return "";
    }
}
