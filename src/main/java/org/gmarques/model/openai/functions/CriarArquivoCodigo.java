package org.gmarques.model.openai.functions;


import static org.gmarques.model.openai.client.OpenAIService.callOpenAiChat;
import static org.gmarques.util.FileHelper.abrirArquivoNoIntelliJ;
import static org.gmarques.util.ProjectRegistry.getFolderStructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import org.gmarques.model.openai.objects.Tool;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.gmarques.util.ProjectRegistry;

public class CriarArquivoCodigo extends FunctionBase {

  public String name() {
    return "criar_arquivo_codigo";
  }

  @Override
  public void run(JsonNode functionArgs) {
    try {
      JsonNode parsedArgs = mapper().readTree(functionArgs.asText());
      String nomeProjeto = parsedArgs.get("nome_projeto").asText();
      JsonNode descricoesNode = parsedArgs.get("descricoes");

      String descricoesJson = mapper().writeValueAsString(descricoesNode);

      System.out.println("Função chamada: " + name());
      System.out.println(
          "Parâmetros: nome_projeto=" + nomeProjeto + ", descricoes=" + descricoesJson);
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
            "Cria arquivo(s) de código em um projeto específico com base nas descrições fornecidas. Os projetos existentes hoje são: "
                + ProjectRegistry.getAllProjectNames())
        .parameters(Map.of(
            "type", "object",
            "properties", Map.of(
                "nome_projeto", Map.of(
                    "type", "string",
                    "description", "O nome do projeto onde os arquivos serão criados."
                ),
                "descricoes", Map.of(
                    "type", "array",
                    "items", Map.of(
                        "type", "string",
                        "description", "A descrição de um arquivo de código a ser criado."
                    ),
                    "description", "Uma lista de descrições dos arquivos de código a serem criados."
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
      descricoes = mapper().readValue(descricoesJson, new TypeReference<List<String>>() {
      });
    } catch (JsonProcessingException e) {
      System.out.println("Erro ao processar as descrições: " + e.getMessage());
      e.printStackTrace();
      return;
    }

    String folderStructure = getFolderStructure(projectPath);

    List<CompletableFuture<Void>> futures = descricoes.stream()
        .map(descricao -> CompletableFuture.runAsync(() -> {
          String prompt = String.format(
              "Crie uma classe Java com o seguinte: '%s'. " +
                  "Retorne somente um array de objetos JSON com os campos 'nome_arquivo', 'path_arquivo' e 'conteudo', sem qualquer texto ou explicação adicional. "
                  +
                  "Não inclua ```json ou ``` no retorno. " +
                  "A estrutura de pastas desse projeto é:\n%s",
              descricao, folderStructure
          );

          var response = callOpenAiChat(prompt);
          try {
            JsonNode resultJson = mapper().readTree(response);

            for (JsonNode fileNode : resultJson) {
              try {
                String nomeArquivo = fileNode.get("nome_arquivo").asText();
                String pathArquivo = fileNode.get("path_arquivo").asText();
                String conteudo = fileNode.get("conteudo").asText();

                Path filePath = Paths.get(pathArquivo, nomeArquivo);
                Files.createDirectories(filePath.getParent());
                Files.writeString(filePath, conteudo, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

                System.out.println("Arquivo criado: " + filePath.toString());

                abrirArquivoNoIntelliJ(filePath.toString());
              } catch (Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Erro ao processar o arquivo: " + fileNode.get("nome_arquivo").asText());
              }
            }
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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