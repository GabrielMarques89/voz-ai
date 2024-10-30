package org.gmarques.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.gmarques.util.ParameterBuilder;

@Log4j2
public class ControlarMonitor extends FunctionBase {
    private static final int SC_MONITORPOWER = 0xF170;
    private static final int MONITOR_ON = -1;
    private static final int MONITOR_OFF = 2;

    @Override
    public String name() {
        return "controlar_monitor";
    }

    @Override
    public String description() {
        return "Liga ou desliga o monitor do computador.";
    }

    @Override
    public Map<String, Object> parameters() {
        return new ParameterBuilder()
            .addEnumParameter("acao", "string", "A ação a ser executada no monitor: 'ligar' ou 'desligar'.", List.of("ligar", "desligar"))
            .addRequired("acao")
            .build();
    }

    @Override
    protected void execute(JsonNode functionArgs) throws Exception {
        String acao = functionArgs.get("acao").asText().toLowerCase();
        log.info("Executing function {}: acao={}", name(), acao);

        if ("ligar".equals(acao)) {
            turnOnMonitor();
            log.info("Monitor ligado.");
        } else if ("desligar".equals(acao)) {
            turnOffMonitor();
            log.info("Monitor desligado.");
        } else {
            log.warn("Ação inválida: {}", acao);
            throw new IllegalArgumentException("Ação inválida: " + acao);
        }
    }

    private void turnOffMonitor() {
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        User32.INSTANCE.SendMessage(hwnd, WinUser.WM_SYSCOMMAND, new WinDef.WPARAM(SC_MONITORPOWER), new WinDef.LPARAM(MONITOR_OFF));
    }

    private void turnOnMonitor() {
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        User32.INSTANCE.SendMessage(hwnd, WinUser.WM_SYSCOMMAND, new WinDef.WPARAM(SC_MONITORPOWER), new WinDef.LPARAM(MONITOR_ON));
    }
}
