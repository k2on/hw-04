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
    // Constants for experiment configuration
    private static final int TOTAL_TRIALS = 50;
    private static final double MIN_RADIUS = 20;
    private static final double MAX_RADIUS = 60;
    private static final double MARGIN = 100;
    
    // Instance variables for tracking experiment state
    private int currentTrial = 0;
    private long clickStartTime;
    private Point2D lastCirclePosition;
    private Circle currentCircle;
    private Pane centerPane;
    private Text countdownText;
    private StringBuilder csvData;
    private Random random;
    
    /**
     * Initializes and displays the main application window
     * @param stage the primary stage for this application
     */
    @Override
    public void start(Stage stage) throws IOException {
        // PRE: stage is not null
        assert stage != null : "Stage must not be null";
        
        // Initialize instance variables
        random = new Random();
        csvData = new StringBuilder();
        csvData.append("Trial Number,Target Size (pixels),Distance to Target (pixels),Time to Click (milliseconds)\n");
        
        // Create menu bar with File menu
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem go = new MenuItem("Go!");
        MenuItem quit = new MenuItem("Quit");
        fileMenu.getItems().addAll(go, quit);
        menuBar.getMenus().add(fileMenu);
        
        // Create main layout
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        
        centerPane = new Pane();
        centerPane.setStyle("-fx-background-color: white;");
        root.setCenter(centerPane);
        
        // Set up menu item actions
        quit.setOnAction(e -> stage.close());
        
        go.setOnAction(e -> {
            // PRE: centerPane is initialized
            assert centerPane != null : "Center pane must be initialized";
            
            currentTrial = 0;
            centerPane.getChildren().clear();
            startCountdown(stage);
            
            // POST: trial counter is reset and countdown has started
            assert currentTrial == 0 : "Trial counter should be reset to 0";
        });
        
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Fitts's Law Experiment");
        stage.setScene(scene);
        stage.show();
        
        // POST: stage is visible with proper dimensions
        assert stage.isShowing() : "Stage should be visible";
        assert scene.getWidth() == 800 && scene.getHeight() == 600 : "Scene dimensions should be 800x600";
    }
    
    /**
     * Starts the countdown sequence from 5 to 0 before beginning the experiment
     * @param stage the primary stage
     */
    private void startCountdown(Stage stage) {
        // PRE: stage and centerPane are not null
        assert stage != null : "Stage must not be null";
        assert centerPane != null : "Center pane must not be null";
        
        // Create countdown text display
        countdownText = new Text();
        countdownText.setFont(Font.font(48));
        countdownText.setFill(Color.BLACK);
        
        centerPane.getChildren().add(countdownText);
        
        final int[] countdown = {5};
        
        // Create timeline for countdown animation
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            // PRE: centerPane has valid dimensions
            assert centerPane.getWidth() > 0 && centerPane.getHeight() > 0 : "Center pane must have valid dimensions";
            
            if (countdown[0] > 0) {
                countdownText.setText(String.valueOf(countdown[0]));
                centerPane.layout();
                // Center the text horizontally and vertically
                countdownText.setX((centerPane.getWidth() - countdownText.getBoundsInLocal().getWidth()) / 2);
                countdownText.setY(centerPane.getHeight() / 2);
                countdown[0]--;
            } else {
                countdownText.setText("GO!");
                centerPane.layout();
                countdownText.setX((centerPane.getWidth() - countdownText.getBoundsInLocal().getWidth()) / 2);
                countdownText.setY(centerPane.getHeight() / 2);
            }
            
            // POST: countdown text is positioned at center of pane
            assert countdownText.getX() >= 0 && countdownText.getY() > 0 : "Text should be positioned within pane";
        }));
        
        timeline.setCycleCount(6); // 5, 4, 3, 2, 1, GO!
        timeline.setOnFinished(e -> {
            centerPane.getChildren().clear();
            showNextTarget(stage);
            
            // POST: countdown text has been removed from pane
            assert !centerPane.getChildren().contains(countdownText) : "Countdown text should be removed";
        });
        timeline.play();
    }
    
    /**
     * Displays the next target circle for the user to click
     * @param stage the primary stage
     */
    private void showNextTarget(Stage stage) {
        // PRE: stage and centerPane are not null
        assert stage != null : "Stage must not be null";
        assert centerPane != null : "Center pane must not be null";
        // PRE: currentTrial is within valid range
        assert currentTrial >= 0 && currentTrial <= TOTAL_TRIALS : "Current trial must be between 0 and TOTAL_TRIALS";
        
        // Check if all trials are complete
        if (currentTrial >= TOTAL_TRIALS) {
            finishExperiment(stage);
            return;
        }
        
        // Generate random radius for target circle
        double radius = MIN_RADIUS + random.nextDouble() * (MAX_RADIUS - MIN_RADIUS);
        
        // POST: radius is within specified bounds
        assert radius >= MIN_RADIUS && radius <= MAX_RADIUS : "Radius must be within MIN and MAX bounds";
        
        double x, y;
        
        // Position first target at center, subsequent targets randomly
        if (currentTrial == 0) {
            x = centerPane.getWidth() / 2;
            y = centerPane.getHeight() / 2;
        } else {
            // PRE: centerPane has sufficient dimensions for margins
            assert centerPane.getWidth() > 2 * (MARGIN + radius) : "Pane width must accommodate margins and target";
            assert centerPane.getHeight() > 2 * (MARGIN + radius) : "Pane height must accommodate margins and target";
            
            x = MARGIN + radius + random.nextDouble() * (centerPane.getWidth() - 2 * MARGIN - 2 * radius);
            y = MARGIN + radius + random.nextDouble() * (centerPane.getHeight() - 2 * MARGIN - 2 * radius);
            
            // POST: target is positioned within margins
            assert x >= MARGIN + radius && x <= centerPane.getWidth() - MARGIN - radius : "X position must be within margins";
            assert y >= MARGIN + radius && y <= centerPane.getHeight() - MARGIN - radius : "Y position must be within margins";
        }
        
        // Create the target circle
        currentCircle = new Circle(radius, Color.BLACK);
        currentCircle.setCenterX(x);
        currentCircle.setCenterY(y);
        
        Point2D currentPosition = new Point2D(x, y);
        double distance = 0;
        
        // Calculate distance from previous target (if exists)
        if (lastCirclePosition != null) {
            // PRE: lastCirclePosition is valid
            assert lastCirclePosition.getX() >= 0 && lastCirclePosition.getY() >= 0 : "Last position must be valid";
            
            distance = lastCirclePosition.distance(currentPosition);
            
            // POST: distance is non-negative
            assert distance >= 0 : "Distance must be non-negative";
        }
        
        lastCirclePosition = currentPosition;
        clickStartTime = System.currentTimeMillis();
        
        // POST: click start time is set
        assert clickStartTime > 0 : "Click start time must be positive";
        
        final double finalDistance = distance;
        final double finalRadius = radius;
        
        // Set up click handler for the target
        currentCircle.setOnMouseClicked(me -> {
            // PRE: clickStartTime has been set
            assert clickStartTime > 0 : "Click start time must have been set";
            
            long clickTime = System.currentTimeMillis() - clickStartTime;
            
            // POST: click time is non-negative
            assert clickTime >= 0 : "Click time must be non-negative";
            
            currentTrial++;
            
            // Record trial data in CSV format
            csvData.append(String.format("%d,%.2f,%.2f,%d\n", 
                currentTrial, finalRadius * 2, finalDistance, clickTime));
            
            // POST: trial counter has been incremented
            assert currentTrial > 0 && currentTrial <= TOTAL_TRIALS : "Trial counter must be within valid range";
            
            centerPane.getChildren().clear();
            showNextTarget(stage);
        });
        
        centerPane.getChildren().add(currentCircle);
        
        // POST: circle has been added to the pane
        assert centerPane.getChildren().contains(currentCircle) : "Circle must be added to pane";
    }
    
    /**
     * Completes the experiment by saving data to CSV and displaying completion message
     * @param stage the primary stage
     */
    private void finishExperiment(Stage stage) {
        // PRE: stage is not null
        assert stage != null : "Stage must not be null";
        // PRE: all trials have been completed
        assert currentTrial >= TOTAL_TRIALS : "All trials must be completed before finishing";
        // PRE: csvData contains header and trial data
        assert csvData.length() > 0 : "CSV data must contain data";
        
        try {
            FileWriter writer = new FileWriter("fitts_law_results.csv");
            writer.write(csvData.toString());
            writer.close();
            
            // POST: file has been written successfully
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Experiment Complete");
            alert.setHeaderText("All trials completed!");
            alert.setContentText("Results saved to fitts_law_results.csv");
            alert.showAndWait();
            
            stage.close();
            
            // POST: stage has been closed
            assert !stage.isShowing() : "Stage should be closed";
            
        } catch (IOException e) {
            // Handle file writing errors
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
