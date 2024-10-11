package org.gmarques.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectRegistry {

  private static final Map<String, String> projectMap = new HashMap<>();
  private static final List<String> EXCLUDE_DIRS = List.of(
      ".git", "git", "fastRequest", ".gradle", "gradle", ".idea", "idea",
      "build", "target", "generated", "tmp", "production"
  );

  static {
    projectMap.put("projeto de voz", "C:\\projects\\voz-ai");
  }

  public static String getProjectPath(String projectName) {
    return projectMap.getOrDefault(projectName, "");
  }

  public static void addProject(String projectName, String projectPath) {
    projectMap.put(projectName, projectPath);
  }

  public static Map<String, String> getAllProjects() {
    return new HashMap<>(projectMap);
  }

  public static String getAllProjectNames() {
    return String.join(", ", projectMap.keySet());
  }

  public static String getFolderStructureByProjectName(String projectName) {
    String projectPath = getProjectPath(projectName);
    return projectPath.isEmpty() ? "Project not found" : getFolderStructure(projectPath);
  }

  public static String getFolderStructure(String projectPath) {
    return buildFolderStructure(projectPath, false);
  }

  public static String getFolderStructureWithFiles(String projectPath) {
    return buildFolderStructure(projectPath, true);
  }

  private static String buildFolderStructure(String projectPath, boolean includeFiles) {
    try {
      return Files.walk(Paths.get(projectPath))
          .filter(path -> shouldIncludePath(path, includeFiles))
          .map(Path::toString)
          .collect(Collectors.joining("\n"));
    } catch (IOException e) {
      return "Error while reading project folder structure: " + e.getMessage();
    }
  }

  private static boolean shouldIncludePath(Path path, boolean includeFiles) {
    boolean isDirectory = Files.isDirectory(path);
    boolean isJavaFile = Files.isRegularFile(path) && path.toString().endsWith(".java");
    boolean isValid = EXCLUDE_DIRS.stream().noneMatch(dir -> path.toString().contains(dir));
    return isValid && (isDirectory || (includeFiles && isJavaFile));
  }
}