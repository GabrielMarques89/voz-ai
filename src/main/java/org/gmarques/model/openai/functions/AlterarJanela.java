package org.gmarques.model.openai.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jna.Native;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.Set;
import org.gmarques.model.openai.interfaces.FunctionInterface;
import org.gmarques.model.openai.objects.Tool;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

public class AlterarJanela extends FunctionBase {

    public String name() {
        return "alterar_janela";
    }

    public Tool getTool() {
        return Tool.builder()
            .name(name())
            .type(FUNCTION)
            .description("Altera a janela ativa no Windows com base no nome do aplicativo ou título da janela fornecido.")
            .parameters(Map.of(
                "type", "object",
                "properties", Map.of(
                    "nome_janela", Map.of(
                        "type", "string",
                        "description", "O nome do aplicativo ou parte do título da janela para a qual mudar o foco."
                    )
                ),
                "required", List.of("nome_janela")
            ))
            .build();
    }

    @Override
    public void run(JsonNode functionArgs){
        JsonNode parsedArgs = null;
        try {
            parsedArgs = mapper().readTree(functionArgs.asText());
            String consulta = parsedArgs.get("nome_janela").asText();
            System.out.println("Função chamada: " + this.name());
            System.out.println("Parâmetro: " + consulta);
            execute(consulta);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute(String... parameters) {
        String nomeJanela = parameters[0];
        System.out.println("Alterando para a janela: " + nomeJanela);

        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                alterarJanelaWindows(nomeJanela);
            } else {
                System.out.println("Esta funcionalidade é suportada apenas no Windows.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void alterarJanelaWindows(String nomeJanela) {
        User32 user32 = User32.INSTANCE;
        final String nomeJanelaLower = nomeJanela.toLowerCase();
        final HWND[] hwndEncontrado = {null};
        final double[] maiorSimilaridade = {0.0};

        user32.EnumWindows((hWnd, data) -> {
            char[] windowText = new char[512];
            user32.GetWindowText(hWnd, windowText, 512);
            String tituloJanela = Native.toString(windowText).trim();

            if (!tituloJanela.isEmpty()) {
                String tituloJanelaLower = tituloJanela.toLowerCase();
                double similaridade = calcularSimilaridade(nomeJanelaLower, tituloJanelaLower);

                if (similaridade > maiorSimilaridade[0]) {
                    maiorSimilaridade[0] = similaridade;
                    hwndEncontrado[0] = hWnd;
                }
            }
            return true; // Continua a enumeração
        }, null);

        HWND hwnd = hwndEncontrado[0];

        if (hwnd != null) {
            // Restaura a janela se estiver minimizada ou maximizada
            user32.ShowWindow(hwnd, User32.SW_RESTORE);
            // Traz a janela para o primeiro plano
            user32.SetForegroundWindow(hwnd);
            System.out.println("Janela encontrada e foco alterado.");
        } else {
            System.out.println("Nenhuma janela encontrada correspondente a: " + nomeJanela);
        }
    }

    /**
     * Calcula a similaridade entre duas strings usando o coeficiente de Sørensen-Dice.
     */
    private double calcularSimilaridade(String s1, String s2) {
        Set<String> bigramasS1 = criarBigramas(s1);
        Set<String> bigramasS2 = criarBigramas(s2);

        Set<String> intersecao = new HashSet<>(bigramasS1);
        intersecao.retainAll(bigramasS2);

        return (2.0 * intersecao.size()) / (bigramasS1.size() + bigramasS2.size());
    }

    /**
     * Cria um conjunto de bigramas a partir de uma string.
     */
    private Set<String> criarBigramas(String s) {
        Set<String> resultado = new HashSet<>();
        String palavra = s.replaceAll("\\s+", "");

        for (int i = 0; i < palavra.length() - 1; i++) {
            String bigrama = palavra.substring(i, i + 2);
            resultado.add(bigrama);
        }
        return resultado;
    }

}
