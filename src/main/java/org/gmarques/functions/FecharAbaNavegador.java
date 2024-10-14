package org.gmarques.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.gmarques.model.openai.objects.Tool;

public class FecharAbaNavegador extends FunctionBase {

  @Override
  public String name() {
    return "fechar_aba_navegador";
  }

  @Override
  public void run(JsonNode functionArgs) {
    try {
      System.out.println("Função chamada: " + name());
      var parsedArgs = mapper().readTree(functionArgs.asText());
      var quantidade = parsedArgs.get("quantidade").asInt();
      execute(String.valueOf(quantidade));
    } catch (JsonProcessingException e) {
      System.out.println("Falha na conversão dos parametros para JSON.");
      throw new RuntimeException(e);
    }

  }

  @Override
  public Tool getTool() {
    return Tool.builder()
        .name(name())
        .type("function")
        .description("Fecha uma ou mais abas do navegador.")
        .parameters(Map.of(
            "type", "object",
            "properties", Map.of(
                "quantidade", Map.of(
                    "type", "integer",
                    "description", "Número de abas a serem fechadas. Valor padrão é 1."
                )
            ),
            "required", List.of()
        ))
        .build();
  }

  @SneakyThrows
  @Override
  public void execute(String... parameters) {
    if (parameters.length > 0) {
      try {
        var quantidade = Integer.parseInt(parameters[0]);
        for (int i = 0; i < quantidade; i++) {
          closeBrowserTab();
          Thread.sleep(200);
        }
        System.out.println("Fechadas " + quantidade + " abas do navegador.");
      } catch (NumberFormatException e) {
        System.out.println("Parâmetro inválido para quantidade: " + parameters[0]);
      }
    }
  }

  public static void closeBrowserTab() {
    try {
      Robot robot = new Robot();
      robot.keyPress(KeyEvent.VK_CONTROL);
      robot.keyPress(KeyEvent.VK_W);
      robot.keyRelease(KeyEvent.VK_W);
      robot.keyRelease(KeyEvent.VK_CONTROL);
    } catch (AWTException e) {
      System.out.println("Erro ao simular as teclas: " + e.getMessage());
      e.printStackTrace();
    }
  }
}