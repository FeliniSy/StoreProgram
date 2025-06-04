module org.example.storeprogram {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens org.example.storeprogram to javafx.fxml;
    exports org.example.storeprogram;
}