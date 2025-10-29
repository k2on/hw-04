package com.example.hw1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;





public class HelloApplication extends Application {

    private Color getRandomColor() {
        Color[] colors = new Color[]{
                Color.RED,
                Color.YELLOW,
                Color.GREEN,
                Color.ORANGE,
                Color.BLUE,
        };
        int rnd = new Random().nextInt(colors.length);
        return colors[rnd];
    }


    @Override
    public void start(Stage stage) throws IOException {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem go = new MenuItem("Go!");
        MenuItem quit = new MenuItem("Quit");



        fileMenu.getItems().addAll(go, quit);

        // Add menu to the MenuBar
        menuBar.getMenus().add(fileMenu);
        BorderPane root = new BorderPane();
        root.setTop(menuBar);

        quit.setOnAction(e -> stage.close());
        go.setOnAction(e -> {
            Circle cir = new Circle(stage.getWidth() / 8, getRandomColor());
            cir.setOnMouseClicked(me -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Clicked");
                alert.setContentText("The circle has been clicked");
                alert.showAndWait();
                stage.close();

            });
            root.setCenter(cir);
        });

        Scene scene = new Scene(root, 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
