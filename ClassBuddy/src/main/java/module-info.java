module classbuddy {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;

    opens com.classbuddy to javafx.fxml;
    opens com.classbuddy.controller to javafx.fxml;
    opens com.classbuddy. model to javafx.fxml;
    opens com.classbuddy.service to javafx.fxml;
    opens com.classbuddy.util to javafx.fxml;

    exports com.classbuddy;
    exports com. classbuddy.controller;
    exports com.classbuddy.model;
    exports com.classbuddy.service;
    exports com.classbuddy.util;
}