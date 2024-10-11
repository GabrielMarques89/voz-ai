package org.gmarques.util;

import java.io.IOException;

public class FileHelper {
  public static void abrirArquivoNoIntelliJ(String filePath) {
    try {
      String ideaPath = "C:\\Program Files\\JetBrains\\IntelliJ IDEA 243.12818.47\\bin\\idea.bat";
      var command = "\"" + ideaPath + "\" \"" + filePath + "\"";

      Runtime.getRuntime().exec(command);
      System.out.println("Arquivo aberto no IntelliJ.");
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Erro ao abrir o arquivo no IntelliJ IDEAa.");
    }
  }
}
