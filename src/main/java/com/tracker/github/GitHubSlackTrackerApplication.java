package com.tracker.github;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class GitHubSlackTrackerApplication {
    public static void main(String[] args) {
        loadEnvFile();
        SpringApplication.run(GitHubSlackTrackerApplication.class, args);
    }

    private static void loadEnvFile() {
        try {
            Path envPath = Path.of(".env");
            if (Files.exists(envPath)) {
                Files.readAllLines(envPath).stream()
                    .filter(line -> line.contains("=") && !line.startsWith("#"))
                    .forEach(line -> {
                        String[] parts = line.split("=", 2);
                        if (System.getProperty(parts[0]) == null) {
                            System.setProperty(parts[0], parts[1]);
                        }
                    });
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load .env file");
        }
    }
}
