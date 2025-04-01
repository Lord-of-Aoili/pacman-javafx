package pacman;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import pacman.model.engine.GameEngine;
import pacman.model.engine.GameEngineImpl;
import pacman.view.GameWindow;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(event -> {
        System.out.println("Closing application...");
        Platform.exit();
        System.exit(0); // Ensure JVM halts
    });
        GameEngine model = new GameEngineImpl("src/main/resources/config.json");
        GameWindow window = new GameWindow(model, 448, 576);

        primaryStage.setTitle("Pac-Man");
        primaryStage.setScene(window.getScene());
        primaryStage.show();

        window.run();
    }
}