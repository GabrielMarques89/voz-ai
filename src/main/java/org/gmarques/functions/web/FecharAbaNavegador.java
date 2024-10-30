package org.gmarques.functions;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.gmarques.util.ParameterBuilder;

@Log4j2
public class FecharAbaNavegador extends FunctionBase {

  @Override
  public String name() {
    return "fechar_aba_navegador";
  }

  @Override
  public String description() {
    return "Fecha uma ou mais abas do navegador.";
  }

  @Override
  public Map<String, Object> parameters() {
    return new ParameterBuilder()
        .addParameter("quantidade", "integer", "Número de abas a serem fechadas. Valor padrão é 1.")
        .build();
  }

  @Override
  protected void execute(JsonNode functionArgs) throws Exception {
    int quantidade = functionArgs.has("quantidade") ? functionArgs.get("quantidade").asInt() : 1;
    log.info("Executing function {}: quantidade={}", name(), quantidade);

    for (int i = 0; i < quantidade; i++) {
      closeBrowserTab();
      Thread.sleep(200);
    }
    log.info("Fechadas {} abas do navegador.", quantidade);
  }

  private void closeBrowserTab() {
    try {
      Robot robot = new Robot();
      robot.keyPress(KeyEvent.VK_CONTROL);
      robot.keyPress(KeyEvent.VK_W);
      robot.keyRelease(KeyEvent.VK_W);
      robot.keyRelease(KeyEvent.VK_CONTROL);
    } catch (AWTException e) {
      log.error("Erro ao simular as teclas: {}", e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
}
