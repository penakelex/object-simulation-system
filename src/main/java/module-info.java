module org.penakelex.objectsimulationsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.lang3;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;

    opens org.penakelex.objectsimulationsystem to javafx.fxml;

    exports org.penakelex.objectsimulationsystem;
    exports org.penakelex.objectsimulationsystem.model.behaviour;
    exports org.penakelex.objectsimulationsystem.model.habitat;
    exports org.penakelex.objectsimulationsystem.ui;
    exports org.penakelex.objectsimulationsystem.model.vehicle;
    exports org.penakelex.objectsimulationsystem.model.vehicle.images;
    exports org.penakelex.objectsimulationsystem.model.vehicle.images.exceptions;
    exports org.penakelex.objectsimulationsystem.model.habitat.exceptions;
    exports org.penakelex.objectsimulationsystem.model.vehicle.exceptions;
    exports org.penakelex.objectsimulationsystem.model.collection;
    exports org.penakelex.objectsimulationsystem.ui.components;
    exports org.penakelex.objectsimulationsystem.viewmodel;
    opens org.penakelex.objectsimulationsystem.viewmodel to javafx.fxml;
    exports org.penakelex.objectsimulationsystem.view;
    opens org.penakelex.objectsimulationsystem.view to javafx.fxml;
    exports org.penakelex.objectsimulationsystem.ui.helpers;
    opens org.penakelex.objectsimulationsystem.ui.helpers to javafx.fxml;
}