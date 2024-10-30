package org.gmarques.functions;


import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.gmarques.model.openai.client.OpenAIService;
import org.gmarques.model.openai.enums.ModelType;
import org.gmarques.util.ParameterBuilder;
import org.gmarques.util.ProjectRegistry;

@Log4j2
public class EncontrarEstruturaProjetoParaAlteracaoDeArquivo extends FunctionBase {

  @Override
  public String name() {
    return "alterar_arquivo";
  }

  @Override
  public String description() {
    return "Descobre qual projeto faz parte do escopo da alteração de arquivo. Retorna um descritivo da alteração proposta para manter o escopo. Os projetos existentes hoje são: " + ProjectRegistry.getAllProjectNames();
  }

  @Override
  public Map<String, Object> parameters() {
    return new ParameterBuilder()
        .addParameter("nome_projeto", "string", "O nome do projeto")
        .addParameter("nome_arquivo", "string", "O nome do arquivo a ser alterado")
        .addParameter("descricao_alteracao", "string", "Descrição das alterações a serem realizadas")
        .addRequired("nome_projeto")
        .addRequired("nome_arquivo")
        .addRequired("descricao_alteracao")
        .build();
  }

  @Override
  protected void execute(JsonNode functionArgs) throws Exception {
    String nomeProjeto = functionArgs.get("nome_projeto").asText();
    String nomeArquivo = functionArgs.get("nome_arquivo").asText();
    String descricaoAlteracao = functionArgs.get("descricao_alteracao").asText();

    log.info("Executing function {}: nome_projeto={}, nome_arquivo={}, descricao_alteracao={}", name(), nomeProjeto, nomeArquivo, descricaoAlteracao);

    String projectPath = ProjectRegistry.getProjectPath(nomeProjeto);
    String folderStructureWithFiles = ProjectRegistry.getFolderStructureWithFiles(projectPath);

    String prompt = String.format(
        "Quero alterar uma das classes desse projeto. Decida qual arquivo eu preciso alterar. " +
            "Retorne somente o path completo do arquivo sem qualquer texto ou explicação adicional. " +
            "Não inclua ```json ou ``` nem nenhum símbolo no retorno. " +
            "O arquivo é %s. " +
            "A estrutura de pastas desse projeto é:\n%s",
        nomeArquivo, folderStructureWithFiles
    );

    String response = OpenAIService.callOpenAiChat(prompt, ModelType.text);

    log.info("Resposta com o nome do arquivo: {}", response);

    Path targetFilePath = Paths.get(response);
    String conteudoArquivo = Files.readString(targetFilePath);

    String promptAlteracao = String.format(
        "Quero alterar essa classe. Quero %s. " +
            "Retorne a classe completa com a alteração, indicando com um comentário a região alterada. Não inclua ```java ou ```json ou ``` no retorno. Não inclua qualquer texto ou explicação adicional. " +
            "O conteúdo do arquivo original é:\n%s",
        descricaoAlteracao, conteudoArquivo
    );

    String refactoredContent = OpenAIService.callOpenAiChat(promptAlteracao, ModelType.text);
    Files.writeString(targetFilePath, refactoredContent, StandardOpenOption.TRUNCATE_EXISTING);

    log.info("Alterações realizadas com sucesso no arquivo: {}", targetFilePath.toString());
    abrirArquivoNoIntelliJ(targetFilePath.toString());
  }

  private void abrirArquivoNoIntelliJ(String filePath) {
    // Implement method to open file in IntelliJ
  }
}
