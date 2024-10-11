package org.gmarques.functions;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import org.gmarques.model.openai.objects.Tool;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;

public class FecharAbaNavegador extends FunctionBase {

    @Override
    public String name() {
        return "fechar_aba_navegador";
    }

    @Override
    public void run(JsonNode functionArgs) {
        System.out.println("Função chamada: " + name());
        execute();
    }

    @Override
    public Tool getTool() {
        return Tool.builder()
                .name(name())
                .type("function")
                .description("Fecha a aba atual do navegador.")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(),
                        "required", List.of()
                ))
                .build();
    }

    @SneakyThrows
    @Override
    public void execute(String... parameters) {
        closeBrowserTab();
        System.out.println("Aba do navegador fechada.");
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
