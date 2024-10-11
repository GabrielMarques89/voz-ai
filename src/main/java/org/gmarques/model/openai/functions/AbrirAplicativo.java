//package org.gmarques.model.openai.functions;
//
//import java.util.List;
//import java.util.Map;
//import org.gmarques.model.openai.enums.ToolType;
//import org.gmarques.model.openai.objects.Tool;
//import org.gmarques.model.openai.objects.ToolParameters;
//import org.gmarques.model.openai.objects.ToolProperties;
//import org.gmarques.model.openai.objects.ToolProperty;
//
//public class AbrirAplicativo  extends FunctionBase{
//
//  public String name() {
//    return "abrir_aplicativo";
//  }
//
//  public Tool getTool() {
//    return Tool.builder()
//        .name(name())
//        .type(FUNCTION)
//        .description("Realiza uma pesquisa local no computador com base na consulta fornecida.")
//        .parameters(getToolParameters()).build();
//  }
//
//  @Override
//  public void execute(String... parameters) {
//    var appName = parameters[0];
//    System.out.println("Abrindo aplicativo: " + appName);
//    try {
//      String os = System.getProperty("os.name").toLowerCase();
//      if (os.contains("win")) {
//
//        Runtime.getRuntime().exec("cmd /c start " + appName);
//      } else if (os.contains("mac")) {
//
//        Runtime.getRuntime().exec("open " + appName);
//      } else if (os.contains("nix") || os.contains("nux")) {
//
//        Runtime.getRuntime().exec("xdg-open " + appName);
//      } else {
//        System.out.println("Sistema operacional não suportado para abrir aplicativos.");
//      }
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }
//
//  @Override
//  public String getDescription() {
//    return "";
//  }
//
//  @Override
//  public ToolParameters getToolParameters() {
//    Map.of(
//        "type", "object",
//        "properties", Map.of(
//            "consulta", Map.of(
//                "type", "string",
//                "description", "A consulta de pesquisa local no computador."
//            )
//        ),
//        "required", List.of("consulta")
//    );
//
//    ToolProperty nomeProjetoProperty = new ToolProperty(
//        ToolType.STRING, "O nome do projeto onde os arquivos serão criados."
//    );
//
//    ToolProperty descricoesProperty = new ToolProperty(
//        ToolType.ARRAY, "Uma lista de descrições dos arquivos de código a serem criados."
//    );
//
//    ToolProperties toolProperties = new ToolProperties(nomeProjetoProperty, descricoesProperty);
//
//    return new ToolParameters(
//        ToolType.OBJECT,
//        toolProperties,
//        List.of("nome_projeto", "descricoes")
//    );
//  }
//}
