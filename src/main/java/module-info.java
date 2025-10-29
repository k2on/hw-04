module com.example.hw1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.hw1 to javafx.fxml;
    exports com.example.hw1;
}