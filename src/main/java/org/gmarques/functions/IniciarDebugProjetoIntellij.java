package org.gmarques.functions;

import static org.gmarques.util.ProjectRegistry.getProjectPath;
import static org.gmarques.util.WindowHelper.alterarJanelaWindows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.gmarques.model.openai.objects.Tool;
import org.gmarques.util.ProjectRegistry;

public class IniciarDebugProjetoIntellij extends FunctionBase {

    @Override
    public String name() {
        return "iniciar_debug";
    }

    @Override
    public void run(JsonNode functionArgs) {
        try {
            JsonNode parsedArgs = mapper().readTree(functionArgs.asText());
            String nomeProjeto = parsedArgs.get("nome_projeto").asText();

            System.out.println("Função chamada: " + name());
            System.out.println("Parâmetros: nome_projeto=" + nomeProjeto);
            execute(nomeProjeto);
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
                "Inicia o debug de um projeto, ativa a janela do IntelliJ IDEA que contém o nome do projeto especificado e dispara as teclas Shift + F9 para iniciar o modo de depuração. Os projetos existentes hoje são: "
                    + ProjectRegistry.getAllProjectNames())
            .parameters(Map.of(
                "type", "object",
                "properties", Map.of(
                    "nome_projeto", Map.of(
                        "type", "string",
                        "description", "O nome do projeto cuja janela do IntelliJ IDEA será ativada."
                    )
                ),
                "required", List.of("nome_projeto")
            ))
            .build();
    }

    @SneakyThrows
    @Override
    public void execute(String... parameters) {
        String nomeProjeto = parameters[0];
        var realProjectName = getProjectPath(nomeProjeto);
        var searchWindowName = realProjectName.substring(realProjectName.lastIndexOf(File.separator) + 1);

        alterarJanelaWindows(searchWindowName);
        Thread.sleep(500);
        sendShiftF9Keystroke();
        System.out.println("Atalho Shift + F9 enviado para o IntelliJ IDEA.");
    }

    private void sendShiftF9Keystroke() {
        try {
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.keyPress(KeyEvent.VK_F9);
            robot.keyRelease(KeyEvent.VK_F9);
            robot.keyRelease(KeyEvent.VK_SHIFT);
        } catch (AWTException e) {
            System.out.println("Erro ao enviar as teclas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
