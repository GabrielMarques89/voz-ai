package org.gmarques.functions;


import static org.gmarques.util.FileHelper.abrirArquivoNoIntelliJ;
import static org.gmarques.util.ProjectRegistry.getFolderStructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.extern.log4j.Log4j2;
import org.gmarques.model.openai.client.OpenAIService;
import org.gmarques.model.openai.enums.ModelType;
import org.gmarques.util.ParameterBuilder;
import org.gmarques.util.ProjectRegistry;

@Log4j2
public class CriarArquivoCodigo extends FunctionBase {

  @Override
  public String name() {
    return "criar_arquivo_codigo";
  }

  @Override
  public String description() {
    return
        "Cria arquivo(s) de código em um projeto específico com base nas descrições fornecidas. Os projetos existentes hoje são: "
            + ProjectRegistry.getAllProjectNames();
  }

  @Override
  public Map<String, Object> parameters() {
    Map<String, Object> itemsSchema = Map.of(
        "type", "string",
        "description", "A descrição de um arquivo de código a ser criado."
    );

    return new ParameterBuilder()
        .addParameter(
            "nome_projeto",
            "string",
            "O nome do projeto onde os arquivos serão criados."
        )
        .addParameter(
            "descricoes",
            "array",
            "Uma lista de descrições dos arquivos de código a serem criados.",
            Map.of("items", itemsSchema)
        )
        .addRequired("nome_projeto")
        .addRequired("descricoes")
        .build();
  }

  @Override
  protected void execute(JsonNode functionArgs) throws Exception {
    String nomeProjeto = functionArgs.get("nome_projeto").asText();
    JsonNode descricoesNode = functionArgs.get("descricoes");

    log.info("Executing function {}: nome_projeto={}, descricoes={}", name(), nomeProjeto,
        descricoesNode);

    String projectPath = ProjectRegistry.getProjectPath(nomeProjeto);
    if (projectPath == null) {
      log.warn("Projeto não encontrado: {}", nomeProjeto);
      return;
    }

    List<String> descricoes = objectMapper.convertValue(descricoesNode, new TypeReference<>() {
    });
    String folderStructure = getFolderStructure(projectPath);

    List<CompletableFuture<Void>> futures = descricoes.stream()
        .map(descricao -> CompletableFuture.runAsync(() -> {
          try {
            processDescricao(descricao, folderStructure, projectPath);
          } catch (Exception e) {
            log.error("Erro ao processar a descrição: {}", descricao, e);
          }
        }))
        .toList();

    CompletableFuture<Void> allOf = CompletableFuture.allOf(
        futures.toArray(new CompletableFuture[0]));
    allOf.get();
  }

  private void processDescricao(String descricao, String folderStructure, String projectPath)
      throws Exception {
    String prompt = String.format(
        "Crie uma classe Java com o seguinte: '%s'. " +
            "Retorne somente um array de objetos JSON com os campos 'nome_arquivo', 'path_arquivo' e 'conteudo', sem qualquer texto ou explicação adicional. "
            +
            "Não inclua ```json ou ``` no retorno. " +
            "A estrutura de pastas desse projeto é:\n%s",
        descricao, folderStructure
    );

    String response = OpenAIService.callOpenAiChat(prompt, ModelType.text);
    JsonNode resultJson = objectMapper.readTree(response);

    for (JsonNode fileNode : resultJson) {
      try {
        String nomeArquivo = fileNode.get("nome_arquivo").asText();
        String pathArquivo = fileNode.get("path_arquivo").asText();
        String conteudo = fileNode.get("conteudo").asText();

        Path filePath = Paths.get(projectPath, pathArquivo, nomeArquivo);
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, conteudo, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING);

        log.info("Arquivo criado: {}", filePath.toString());

        abrirArquivoNoIntelliJ(filePath.toString());
      } catch (Exception e) {
        log.error("Erro ao processar o arquivo: {}", fileNode.get("nome_arquivo").asText(), e);
      }
    }
  }
}
