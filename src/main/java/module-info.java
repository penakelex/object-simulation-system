module org.penakelex.objectsimulationsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.lang3;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;

    opens org.penakelex.objectsimulationsystem to javafx.fxml;

    exports org.penakelex.objectsimulationsystem;
    exports org.penakelex.objectsimulationsystem.behaviour;
    exports org.penakelex.objectsimulationsystem.habitat;
    exports org.penakelex.objectsimulationsystem.ui;
    exports org.penakelex.objectsimulationsystem.vehicle;
    exports org.penakelex.objectsimulationsystem.vehicle.images;
}