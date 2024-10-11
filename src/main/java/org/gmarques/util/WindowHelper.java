package org.gmarques.util;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import java.util.HashSet;
import java.util.Set;

public class WindowHelper {
  public static void alterarJanelaWindows(String nomeJanela) {
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
  private static double calcularSimilaridade(String s1, String s2) {
    Set<String> bigramasS1 = criarBigramas(s1);
    Set<String> bigramasS2 = criarBigramas(s2);

    Set<String> intersecao = new HashSet<>(bigramasS1);
    intersecao.retainAll(bigramasS2);

    return (2.0 * intersecao.size()) / (bigramasS1.size() + bigramasS2.size());
  }

  /**
   * Cria um conjunto de bigramas a partir de uma string.
   */
  private static Set<String> criarBigramas(String s) {
    Set<String> resultado = new HashSet<>();
    String palavra = s.replaceAll("\\s+", "");

    for (int i = 0; i < palavra.length() - 1; i++) {
      String bigrama = palavra.substring(i, i + 2);
      resultado.add(bigrama);
    }
    return resultado;
  }
}
