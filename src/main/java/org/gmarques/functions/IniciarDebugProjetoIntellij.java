package org.gmarques.functions;

import static org.gmarques.util.WindowHelper.alterarJanelaWindows;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.nio.file.Paths;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.gmarques.util.ParameterBuilder;
import org.gmarques.util.ProjectRegistry;

@Log4j2
public class IniciarDebugProjetoIntellij extends FunctionBase {

    @Override
    public String name() {
        return "iniciar_debug";
    }

    @Override
    public String description() {
        return "Inicia o debug de um projeto, ativa a janela do IntelliJ IDEA que contém o nome do projeto especificado e dispara as teclas Shift + F9 para iniciar o modo de depuração. Os projetos existentes hoje são: " + ProjectRegistry.getAllProjectNames();
    }

    @Override
    public Map<String, Object> parameters() {
        return new ParameterBuilder()
            .addParameter("nome_projeto", "string", "O nome do projeto cuja janela do IntelliJ IDEA será ativada.")
            .addRequired("nome_projeto")
            .build();
    }

    @Override
    protected void execute(JsonNode functionArgs) throws Exception {
        String nomeProjeto = functionArgs.get("nome_projeto").asText();
        log.info("Executing function {}: nome_projeto={}", name(), nomeProjeto);

        String realProjectPath = ProjectRegistry.getProjectPath(nomeProjeto);
        String searchWindowName = Paths.get(realProjectPath).getFileName().toString();

        alterarJanelaWindows(searchWindowName);
        Thread.sleep(500);
        sendShiftF9Keystroke();
        log.info("Atalho Shift + F9 enviado para o IntelliJ IDEA.");
    }

    private void sendShiftF9Keystroke() {
        try {
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.keyPress(KeyEvent.VK_F9);
            robot.keyRelease(KeyEvent.VK_F9);
            robot.keyRelease(KeyEvent.VK_SHIFT);
        } catch (AWTException e) {
            log.error("Erro ao enviar as teclas: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}