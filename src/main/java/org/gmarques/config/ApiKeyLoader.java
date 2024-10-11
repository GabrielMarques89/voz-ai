package org.gmarques.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ApiKeyLoader {

    public static String loadApiKey() throws IOException {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("src/main/resources/config.properties")) {
            properties.load(input);
        }
        return properties.getProperty("api.key");
    }

    public static void main(String[] args) {
        try {
            String apiKey = loadApiKey();
            System.out.println("API Key: " + apiKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
