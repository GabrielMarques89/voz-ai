package org.gmarques;

import static org.gmarques.util.ProjectRegistry.getFolderStructure;
import static org.gmarques.util.ProjectRegistry.getFolderStructureWithFiles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Maintestes {


  public static void main(String[] args) {
    String directoryPath = "C:\\projetcs\\voz-ai";
    System.out.println(getFolderStructureWithFiles(directoryPath));

  }
}
