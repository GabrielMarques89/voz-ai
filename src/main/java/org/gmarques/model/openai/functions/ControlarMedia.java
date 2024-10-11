package org.gmarques.model.openai.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jna.platform.win32.WinDef.WORD;
import java.util.List;
import java.util.Map;
import org.gmarques.model.openai.interfaces.FunctionInterface;
import org.gmarques.model.openai.objects.Tool;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinUser.INPUT;
import com.sun.jna.platform.win32.WinUser.KEYBDINPUT;

public class ControlarMedia extends FunctionBase {
    private static final int KEYEVENTF_KEYUP = 0x0002;


    public String name() {
        return "controlar_media";
    }

    @Override
    public void run(JsonNode functionArgs){
        JsonNode parsedArgs = null;
        try {
            parsedArgs = mapper().readTree(functionArgs.asText());
            String consulta = parsedArgs.get("acao").asText();
            System.out.println("Função chamada: " + this.name());
            System.out.println("Parâmetro: " + consulta);
            execute(consulta);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Tool getTool() {
        return Tool.builder()
            .name(name())
            .type(FUNCTION)
            .description("Controla a reprodução de mídia e o volume do computador com base na ação fornecida.")
            .parameters(Map.of(
                "type", "object",
                "properties", Map.of(
                    "acao", Map.of(
                        "type", "string",
                        "description", "A ação a ser executada no controle de mídia. Valores possíveis: 'play_pause', 'proxima_musica', 'volume_maximo', 'aumentar_volume', 'diminuir_volume'."
                    )
                ),
                "required", List.of("acao")
            ))
            .build();
    }

    @Override
    public void execute(String... parameters) {
        String acao = parameters[0];
        System.out.println("Executando ação de mídia: " + acao);

        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                controlarMediaWindows(acao);
            } else {
                System.out.println("Sistema operacional não suportado para controle de mídia.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void controlarMediaWindows(String acao) throws Exception {
        switch (acao) {
            case "play_pause":
                sendMediaKeyWindows(0xB3); // VK_MEDIA_PLAY_PAUSE
                break;
            case "proxima_musica":
                sendMediaKeyWindows(0xB0); // VK_MEDIA_NEXT_TRACK
                break;
            case "volume_maximo":
                // Set volume to max using nircmd
                Runtime.getRuntime().exec("nircmd.exe setsysvolume 65535");
                break;
            case "aumentar_volume":
                Runtime.getRuntime().exec("nircmd.exe changesysvolume 5000");
                break;
            case "diminuir_volume":
                Runtime.getRuntime().exec("nircmd.exe changesysvolume -5000");
                break;
            default:
                System.out.println("Ação não reconhecida.");
        }
    }

    private void sendMediaKeyWindows(int keyCode) {
        INPUT input = new INPUT();
        input.type = new DWORD(INPUT.INPUT_KEYBOARD);
        input.input.setType("ki");
        input.input.ki = new KEYBDINPUT();
        input.input.ki.wVk = new WORD(keyCode);
        input.input.ki.dwFlags = new DWORD(0);

        // Key down
        User32.INSTANCE.SendInput(new DWORD(1), new INPUT[]{input}, input.size());
        // Key up
        input.input.ki.dwFlags = new DWORD(KEYEVENTF_KEYUP);
        User32.INSTANCE.SendInput(new DWORD(1), new INPUT[]{input}, input.size());
    }
}
