package org.gmarques.functions;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.gmarques.util.ParameterBuilder;

@Log4j2
public class DigitarTexto extends FunctionBase {

  @Override
  public String name() {
    return "digitar_texto";
  }

  @Override
  public String description() {
    return "Simula a digitação de texto no teclado.";
  }

  @Override
  public Map<String, Object> parameters() {
    return new ParameterBuilder()
        .addParameter("texto", "string", "O texto a ser digitado.")
        .addRequired("texto")
        .build();
  }

  @Override
  protected void execute(JsonNode functionArgs) throws Exception {
    String texto = functionArgs.get("texto").asText();
    log.info("Executing function {}: texto={}", name(), texto);

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
      log.warn("Sistema operacional não suportado para digitar texto.");
    }
  }
}
