package org.gmarques.util;

import java.io.File;

public class FolderHelper {

  public String getProjectStructure(String basePath) {
    StringBuilder structure = new StringBuilder();
    File baseDir = new File(basePath);
    listFiles(baseDir, structure, "");
    return structure.toString();
  }

  private void listFiles(File dir, StringBuilder structure, String indent) {
    if (dir.isDirectory()) {
      structure.append(indent).append(dir.getName()).append("/\n");
      File[] files = dir.listFiles();
      if (files != null) {
        for (File file : files) {
          listFiles(file, structure, indent + "  ");
        }
      }
    } else {
      structure.append(indent).append(dir.getName()).append("\n");
    }
  }
}
