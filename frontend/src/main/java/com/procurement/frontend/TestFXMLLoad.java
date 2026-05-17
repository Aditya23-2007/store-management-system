package com.procurement.frontend;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import java.net.URL;

public class TestFXMLLoad {
    public static void main(String[] args) {
        System.out.println("Starting JavaFX toolkit...");
        try {
            Platform.startup(() -> {
                System.out.println("JavaFX toolkit started successfully.");
                try {
                    URL resource = TestFXMLLoad.class.getResource("/fxml/finance.fxml");
                    System.out.println("Resource URL: " + resource);
                    if (resource == null) {
                        System.err.println("ERROR: Could not find /fxml/finance.fxml");
                        System.exit(1);
                    }
                    FXMLLoader loader = new FXMLLoader(resource);
                    Object root = loader.load();
                    System.out.println("SUCCESS: FXML loaded successfully! Class: " + root.getClass().getName());
                    System.exit(0);
                } catch (Throwable t) {
                    System.err.println("--- EXCEPTION CAUGHT DURING FXML LOADING ---");
                    t.printStackTrace();
                    System.exit(1);
                }
            });
        } catch (Throwable t) {
            System.err.println("FAILED to start JavaFX toolkit:");
            t.printStackTrace();
            System.exit(1);
        }
    }
}
