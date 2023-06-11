module com.example.serwer {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.serwer to javafx.fxml;
    exports com.example.serwer;
}