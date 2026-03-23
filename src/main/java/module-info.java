module com.album {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.album to javafx.fxml;
    exports com.album;
}