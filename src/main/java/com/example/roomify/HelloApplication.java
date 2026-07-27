package com.example.roomify;

import javafx.application.Application;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        StageCoordinator.getInstance().showLogin(stage);
    }
}