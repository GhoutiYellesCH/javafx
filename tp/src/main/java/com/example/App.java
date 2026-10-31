package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static final double WIDTH = 900;
    private static final double HEIGHT = 600;

    @Override
    public void start(Stage stage) throws IOException {
        Pane root = new Pane();
        // Set an id so the CSS can provide the background if an image resource is missing
        root.setId("bkg");
        
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        
        // Link the CSS file (Step 2.1)
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm()); 

        // Initialize the Game Controller
        GameController gameController = new GameController(root);
        gameController.setupGame(); // Add Mario, objects, etc.

        // Handle keyboard events (Step 3.2)
        scene.setOnKeyPressed(gameController::handleKeyPress);
        
        stage.setScene(scene);
        stage.setTitle("Super JavaFX Mario TP4/TP5");
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}