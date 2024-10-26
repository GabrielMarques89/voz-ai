package org.gmarques;

import static org.gmarques.util.ProjectRegistry.getFolderStructureWithFiles;

import lombok.SneakyThrows;

public class Maintestes {


  @SneakyThrows
  public static void main(String[] args) {

    var result = getFolderStructureWithFiles("C:\\projetcs\\voz-ai");
    var r = "r";
  }
}
