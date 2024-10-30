package org.gmarques.functions;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.gmarques.model.interfaces.MediaController;
import org.gmarques.util.ParameterBuilder;
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
    public String description() {
        return "Controla a reprodução de mídia e o volume do computador com base na ação fornecida.";
    }

    @Override
    public Map<String, Object> parameters() {
        return new ParameterBuilder()
            .addParameter("acao", "string", "A ação a ser executada no controle de mídia. Valores possíveis: 'play_pause', 'proxima_musica', 'volume_maximo', 'aumentar_volume', 'diminuir_volume'.")
            .addRequired("acao")
            .build();
    }

    @Override
    protected void execute(JsonNode functionArgs) throws Exception {
        String acao = functionArgs.get("acao").asText();
        log.info("Executing function {}: acao={}", name(), acao);
        mediaController.executeAction(acao);
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
