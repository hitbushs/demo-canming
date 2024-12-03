module org.example.democanting {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens org.example.democanting to javafx.fxml;
    exports org.example.democanting;
}