package org.gmarques.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.gmarques.model.openai.objects.Tool;

public class DigitarTexto extends FunctionBase {

  public String name() {
    return "digitar_texto";
  }

  public Tool getTool() {
    return Tool.builder()
        .name(name())
        .type(FUNCTION)
        .description("Simula a digitação de texto no teclado.")
        .parameters(Map.of(
            "type", "object",
            "properties", Map.of(
                "texto", Map.of(
                    "type", "string",
                    "description", "O texto a ser digitado."
                )
            ),
            "required", List.of("texto")
        ))
        .build();
  }

  @Override
  public void run(JsonNode functionArgs) {
    JsonNode parsedArgs = null;
    try {
      parsedArgs = mapper().readTree(functionArgs.asText());
      String consulta = parsedArgs.get("texto").asText();
      System.out.println("Função chamada: " + this.name());
      System.out.println("Parâmetro: " + consulta);
      execute(consulta);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @SneakyThrows
  @Override
  public void execute(String... parameters) {
    String texto = parameters[0];
    System.out.println("Digitando texto: " + texto);
    String os = System.getProperty("os.name").toLowerCase();
    Robot robot = new Robot();

    StringSelection stringSelection = new StringSelection(texto);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(stringSelection, null);

    if (os.contains("win") || os.contains("nix") || os.contains("nux")) {
      robot.keyPress(KeyEvent.VK_CONTROL);
      robot.keyPress(KeyEvent.VK_V);
      robot.keyRelease(KeyEvent.VK_V);
      robot.keyRelease(KeyEvent.VK_CONTROL);
    } else if (os.contains("mac")) {
      robot.keyPress(KeyEvent.VK_META);
      robot.keyPress(KeyEvent.VK_V);
      robot.keyRelease(KeyEvent.VK_V);
      robot.keyRelease(KeyEvent.VK_META);
    } else {
      System.out.println("Sistema operacional não suportado para digitar texto.");
    }
  }
}
