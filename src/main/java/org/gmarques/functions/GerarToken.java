package org.gmarques.functions;

import static org.gmarques.config.ApiKeyLoader.loadAgiTokenUrl;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.gmarques.model.openai.objects.Tool;

public class GerarToken extends FunctionBase {

    @Override
    public String name() {
        return "gerar_token_dev";
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
                .description("Gera um token de serviço do AGI e o copia para a área de transferência do Windows.")
                .parameters(Map.of(
                        "type", "object",
                        "properties", Map.of(),
                        "required", List.of()
                ))
                .build();
    }

    @Override
    public void execute(String... parameters) {
        try {
            String clientToken = getClientToken();
            copyToClipboard(clientToken);
            System.out.println("Client token copiado para a área de transferência: " + clientToken);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public String getClientToken() throws Exception {
        String url = loadAgiTokenUrl();
        String jsonInputString = "{\"role_id\":\"token4devs\"}";

        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .build();

        RequestBody body = RequestBody.create(jsonInputString, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build();

        var response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        String responseBody = response.body().string();
        JsonNode jsonNode = mapper().readTree(responseBody);
        return jsonNode.get("auth").get("client_token").asText();
    }


    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }
}
