package org.gmarques;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AccountCreationPostmanRunner {

  public static void main(String[] args) {
    createAccount();
  }

  public static void createAccount() {
    try {
      String collectionPath = "C:\\projetcs\\voz-ai\\src\\main\\resources\\Conta (Automático).postman_collection.json";
      String envPath = "C:\\projetcs\\voz-ai\\src\\main\\resources\\Conta (Automático).postman_collection.json";

      ProcessBuilder processBuilder = new ProcessBuilder(
          "C:\\Users\\Gabriel Marques\\AppData\\Roaming\\npm\\newman.cmd", "run", collectionPath,
          "-e", envPath, "--reporter-cli-no-summary"
      );

      Process process = processBuilder.start();



      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      String cpf = null;
      final int totalLines = 58; 
      int linesRead = 0;

      while ((line = reader.readLine()) != null) {
        linesRead++;

        
        int progress = (int) (((double) linesRead / totalLines) * 100);

        
        int progressBars = progress / 2; 
        StringBuilder progressBar = new StringBuilder();
        progressBar.append("\rProgress: [");
        for (int i = 0; i < 50; i++) { 
          if (i < progressBars) {
            progressBar.append("#");
          } else {
            progressBar.append(" ");
          }
        }
        progressBar.append("] ").append(progress).append("%");

        
        System.out.print(progressBar.toString());
        System.out.flush();

        
        if (line.contains("CPF")) {
          cpf = line.split(":")[1].trim().replaceAll("\"", "").replaceAll("'", "");
        }
      }

      
      System.out.println("\nProcessing complete.");

      process.waitFor();

      if (cpf != null) {
        copyToClipboard(cpf);
        System.out.println("Cpf copiado para o clipboard: " + cpf);
      } else {
        System.out.println("Cpf não encontrado.");
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void copyToClipboard(String text) {
    StringSelection selection = new StringSelection(text);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
  }
}
