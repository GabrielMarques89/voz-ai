package org.gmarques.functions;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.gmarques.model.interfaces.MediaController;
import org.gmarques.model.openai.objects.Tool;
import org.gmarques.util.WindowsMediaController;

@Slf4j
public class ControlarMedia extends FunctionBase {
    private final MediaController mediaController;

    public ControlarMedia() {
        this.mediaController = createMediaController();
    }

    @Override
    public String name() {
        return "controlar_media";
    }

    @Override
    public void run(JsonNode functionArgs) {
        try {
            String acao = functionArgs.get("acao").asText();
            log.info("Função chamada: {}", name());
            log.info("Parâmetro: {}", acao);
            execute(acao);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public Tool getTool() {
        return Tool.builder()
                .name(name())
                .type("function")
                .description("Controla a reprodução de mídia e o volume do computador com base na ação fornecida.")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "acao", Map.of(
                                        "type", "string",
                                        "description", "A ação a ser executada no controle de mídia. Valores possíveis: 'play_pause', 'proxima_musica', 'volume_maximo', 'aumentar_volume', 'diminuir_volume'."
                                )
                        ),
                        "required", Collections.singletonList("acao")
                ))
                .build();
    }

    @Override
    public void execute(String... parameters) {
        String acao = parameters[0];
        try {
            mediaController.executeAction(acao);
        } catch (Exception e) {
            log.error("Erro ao executar ação de mídia: {}", acao, e);
            handleException(e);
        }
    }

    private MediaController createMediaController() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return new WindowsMediaController();
        } else {
            log.warn("Sistema operacional não suportado para controle de mídia: {}", os);
            return new UnsupportedMediaController();
        }
    }

    private static class UnsupportedMediaController implements MediaController {
        @Override
        public void executeAction(String action) throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Controle de mídia não suportado neste sistema operacional.");
        }
    }
}
