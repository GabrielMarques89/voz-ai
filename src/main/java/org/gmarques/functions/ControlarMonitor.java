package org.gmarques.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import lombok.SneakyThrows;
import org.gmarques.model.openai.objects.Tool;

import java.util.List;
import java.util.Map;

public class ControlarMonitor extends FunctionBase {
    private static final int SC_MONITORPOWER = 0xF170;
    private static final int MONITOR_ON = -1;
    private static final int MONITOR_OFF = 2;

    @Override
    public String name() {
        return "controlar_monitor";
    }

    @Override
    public void run(JsonNode functionArgs) {
        try {
            JsonNode parsedArgs = mapper().readTree(functionArgs.asText());
            String acao = parsedArgs.get("acao").asText();

            System.out.println("Função chamada: " + name());
            System.out.println("Parâmetros: acao=" + acao);
            execute(acao);
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
            .description("Liga ou desliga o monitor do computador.")
            .parameters(Map.of(
                "type", "object",
                "properties", Map.of(
                    "acao", Map.of(
                        "type", "string",
                        "description", "A ação a ser executada no monitor: 'ligar' ou 'desligar'.",
                        "enum", List.of("ligar", "desligar")
                    )
                ),
                "required", List.of("acao")
            ))
            .build();
    }

    @SneakyThrows
    @Override
    public void execute(String... parameters) {
        String acao = parameters[0].toLowerCase();

        if (acao.equals("ligar")) {
            turnOnMonitor();
            System.out.println("Monitor ligado.");
        } else if (acao.equals("desligar")) {
            turnOffMonitor();
            System.out.println("Monitor desligado.");
        } else {
            System.out.println("Ação inválida: " + acao);
        }
    }

    public static void turnOffMonitor() {
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        User32.INSTANCE.SendMessage(hwnd, WinUser.WM_SYSCOMMAND, new WinDef.WPARAM(SC_MONITORPOWER), new WinDef.LPARAM(MONITOR_OFF));
    }

    public static void turnOnMonitor() {
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        User32.INSTANCE.SendMessage(hwnd, WinUser.WM_SYSCOMMAND, new WinDef.WPARAM(SC_MONITORPOWER), new WinDef.LPARAM(MONITOR_ON));
    }
}
