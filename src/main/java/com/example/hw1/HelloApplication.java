package com.example.hw1;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.geometry.Point2D;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class HelloApplication extends Application {
    private static final int TOTAL_TRIALS = 50;
    private static final double MIN_RADIUS = 20;
    private static final double MAX_RADIUS = 60;
    private static final double MARGIN = 100;
    
    private int currentTrial = 0;
    private long clickStartTime;
    private Point2D lastCirclePosition;
    private Circle currentCircle;
    private Pane centerPane;
    private Text countdownText;
    private StringBuilder csvData;
    private Random random;
    
    @Override
    public void start(Stage stage) throws IOException {
        random = new Random();
        csvData = new StringBuilder();
        csvData.append("Trial Number,Target Size (pixels),Distance to Target (pixels),Time to Click (milliseconds)\n");
        
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem go = new MenuItem("Go!");
        MenuItem quit = new MenuItem("Quit");
        fileMenu.getItems().addAll(go, quit);
        menuBar.getMenus().add(fileMenu);
        
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        
        centerPane = new Pane();
        centerPane.setStyle("-fx-background-color: white;");
        root.setCenter(centerPane);
        
        quit.setOnAction(e -> stage.close());
        
        go.setOnAction(e -> {
            currentTrial = 0;
            centerPane.getChildren().clear();
            startCountdown(stage);
        });
        
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Fitts's Law Experiment");
        stage.setScene(scene);
        stage.show();
    }
    
    private void startCountdown(Stage stage) {
        countdownText = new Text();
        countdownText.setFont(Font.font(48));
        countdownText.setFill(Color.BLACK);
        
        centerPane.getChildren().add(countdownText);
        
        final int[] countdown = {5};
        
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (countdown[0] > 0) {
                countdownText.setText(String.valueOf(countdown[0]));
                centerPane.layout();
                countdownText.setX((centerPane.getWidth() - countdownText.getBoundsInLocal().getWidth()) / 2);
                countdownText.setY(centerPane.getHeight() / 2);
                countdown[0]--;
            } else {
                countdownText.setText("GO!");
                centerPane.layout();
                countdownText.setX((centerPane.getWidth() - countdownText.getBoundsInLocal().getWidth()) / 2);
                countdownText.setY(centerPane.getHeight() / 2);
            }
        }));
        
        timeline.setCycleCount(6);
        timeline.setOnFinished(e -> {
            centerPane.getChildren().clear();
            showNextTarget(stage);
        });
        timeline.play();
    }
    
    private void showNextTarget(Stage stage) {
        if (currentTrial >= TOTAL_TRIALS) {
            finishExperiment(stage);
            return;
        }
        
        double radius = MIN_RADIUS + random.nextDouble() * (MAX_RADIUS - MIN_RADIUS);
        double x, y;
        
        if (currentTrial == 0) {
            x = centerPane.getWidth() / 2;
            y = centerPane.getHeight() / 2;
        } else {
            x = MARGIN + radius + random.nextDouble() * (centerPane.getWidth() - 2 * MARGIN - 2 * radius);
            y = MARGIN + radius + random.nextDouble() * (centerPane.getHeight() - 2 * MARGIN - 2 * radius);
        }
        
        currentCircle = new Circle(radius, Color.BLACK);
        currentCircle.setCenterX(x);
        currentCircle.setCenterY(y);
        
        Point2D currentPosition = new Point2D(x, y);
        double distance = 0;
        
        if (lastCirclePosition != null) {
            distance = lastCirclePosition.distance(currentPosition);
        }
        
        lastCirclePosition = currentPosition;
        clickStartTime = System.currentTimeMillis();
        
        final double finalDistance = distance;
        final double finalRadius = radius;
        
        currentCircle.setOnMouseClicked(me -> {
            long clickTime = System.currentTimeMillis() - clickStartTime;
            
            currentTrial++;
            csvData.append(String.format("%d,%.2f,%.2f,%d\n", 
                currentTrial, finalRadius * 2, finalDistance, clickTime));
            
            centerPane.getChildren().clear();
            showNextTarget(stage);
        });
        
        centerPane.getChildren().add(currentCircle);
    }
    
    private void finishExperiment(Stage stage) {
        try {
            FileWriter writer = new FileWriter("fitts_law_results.csv");
            writer.write(csvData.toString());
            writer.close();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Experiment Complete");
            alert.setHeaderText("All trials completed!");
            alert.setContentText("Results saved to fitts_law_results.csv");
            alert.showAndWait();
            
            stage.close();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to save results: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    public static void main(String[] args) {
        launch();
    }
}
