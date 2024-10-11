package org.gmarques.model.openai.functions;


import static org.gmarques.model.openai.client.OpenAIService.callOpenAiChat;
import static org.gmarques.util.FileHelper.abrirArquivoNoIntelliJ;
import static org.gmarques.util.ProjectRegistry.getFolderStructure;
import static org.gmarques.util.ProjectRegistry.getProjectPath;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.SneakyThrows;
import org.gmarques.model.openai.objects.Tool;
import org.gmarques.util.ProjectRegistry;

public class EncontrarEstruturaProjeto extends FunctionBase {

  public String name() {
    return "alterar_arquivo";
  }

  @Override
  public Tool getTool() {
    return Tool.builder()
        .name(name())
        .type("function")
        .description(
            "Descobrir qual projeto faz parte do escopo da alteração de arquivo. É necessário voltar também um descritivo da alteração proposta para manter o escopo. Os projetos existentes hoje são: "
                + ProjectRegistry.getAllProjectNames())
        .parameters(Map.of(
            "type", "object",
            "properties", Map.of(
                "nome_projeto", Map.of(
                    "type", "string",
                    "description", "O nome do projeto"
                ),
                "nome_arquivo", Map.of(
                    "type", "string",
                    "description", "O nome do arquivo a ser alterado"
                ),
                "descricao_alteracao", Map.of(
                    "type", "string",
                    "description", "Descrição das alterações a serem realizadas"
                )
            ),
            "required", List.of("nome_projeto", "nome_arquivo", "descricao_alteracao")
        ))
        .build();
  }

  @Override
  public void run(JsonNode functionArgs) {
    try {
      JsonNode parsedArgs = mapper().readTree(functionArgs.asText());
      String nomeProjeto = parsedArgs.get("nome_projeto").asText();
      String nome_arquivo = parsedArgs.get("nome_arquivo").asText();
      String descricao_alteracao = parsedArgs.get("descricao_alteracao").asText();

      execute(nomeProjeto, nome_arquivo, descricao_alteracao);
    } catch (JsonProcessingException e) {
      System.out.println("Um erro ocorreu na função run: " + e.getMessage());
      e.printStackTrace();
    }
  }


  @SneakyThrows
  @Override
  public void execute(String... parameters) {
    String nomeProjeto = parameters[0];
    String nome_arquivo = parameters[1];
    String descricao_alteracao = parameters[2];

    var projectPath = getProjectPath(nomeProjeto);
    String folderStructureWithFiles = ProjectRegistry.getFolderStructureWithFiles(projectPath);

    String prompt = String.format(
        "Quero alterar uma das classes desse projeto. Decida qual arquivo eu preciso alterar. " +
            "Retorne somente o path completo do arquivo sem qualquer texto ou explicação adicional. "
            +
            "Não inclua ```json ou ``` nem nenhum símbolo no retorno. " +
            "O arquivo é %s. " +
            "A estrutura de pastas desse projeto é:\n%s",
        nome_arquivo, folderStructureWithFiles
    );

    var response = callOpenAiChat(prompt);

    System.out.println("Resposta com o nome do arquivo: " + response);

    Path targetFilePath = Paths.get(response);
    String conteudoArquivo = Files.readString(targetFilePath);

    String promptAlteracao = String.format(
        "Quero alterar essa classe. Quero %s." +
            "Retorne a classe completa com a alteração, indicando com um comentário a região alterada. Não inclua ```java ou ```json ou ``` no retorno. Não inclua qualquer texto ou explicação adicional. "
            +
            "O conteúdo do arquivo original é:\n%s",
        descricao_alteracao, conteudoArquivo
    );

    var refactoredContent = callOpenAiChat(promptAlteracao);
    Files.writeString(targetFilePath, refactoredContent, StandardOpenOption.TRUNCATE_EXISTING);

    System.out.println("Alterações realizadas com sucesso no arquivo: " + targetFilePath.toString());
    abrirArquivoNoIntelliJ(targetFilePath.toString());
  }
}