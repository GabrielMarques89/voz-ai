package org.gmarques.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ProjectRegistry {
    public static final Map<String, String> projectMap = new HashMap<>();

    static {
        projectMap.put("projeto de voz", "C:\\projetcs\\voz-ai");
    }

    public static String getProjectPath(String projectName) {
        return projectMap.get(projectName);
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
        var projectPath = projectMap.get(projectName);
      return getFolderStructure(projectPath);
    }

    public static String getFolderStructure(String projectPath) {

        StringBuilder sb = new StringBuilder();
        try {
            Files.walk(Paths.get(projectPath))
                .filter(Files::isDirectory)
                .forEach(path -> sb.append(path.toString()).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String getFolderStructureWithFiles(String projectPath) {
        StringBuilder sb = new StringBuilder();
        try {
            Files.walk(Paths.get(projectPath))
                .filter(path -> Files.isDirectory(path) || (Files.isRegularFile(path) && path.toString().endsWith(".java")))
                .forEach(path -> sb.append(path.toString()).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
