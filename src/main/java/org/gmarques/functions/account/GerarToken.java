package org.gmarques.functions;

import com.fasterxml.jackson.databind.JsonNode;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.log4j.Log4j2;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.gmarques.util.ParameterBuilder;

@Log4j2
public class GerarToken extends FunctionBase {

    @Override
    public String name() {
        return "gerar_token_dev";
    }

    @Override
    public String description() {
        return "Gera um token de serviço do AGI e o copia para a área de transferência do Windows.";
    }

    @Override
    public Map<String, Object> parameters() {
        return new ParameterBuilder().build(); // No parameters
    }

    @Override
    protected void execute(JsonNode functionArgs) throws Exception {
        log.info("Executing function {}", name());
        String clientToken = getClientToken();
        copyToClipboard(clientToken);
        log.info("Client token copiado para a área de transferência: {}", clientToken);
    }

    private String getClientToken() throws Exception {
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
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("auth").get("client_token").asText();
    }

    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }

    private String loadAgiTokenUrl() {
        // Implement method to load AGI token URL
        return "";
    }
}
