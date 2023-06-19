package com.example.serwer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;



/**
 * Klasa HelloApplication jest klasą główną aplikacji, która dziedziczy po klasie Application z JavaFX.
 * Jest odpowiedzialna za uruchomienie i inicjalizację aplikacji.
 */
public class HelloApplication extends Application {

    public void start(Stage stage) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("loginMenu.fxml"));
        Scene loginMenu = new Scene( root);
        stage.setTitle("Hello!");
        String css=this.getClass().getResource("style.css").toExternalForm();
        loginMenu.getStylesheets().add(css);
        stage.setScene(loginMenu);
        stage.show();

    }

    /**
     * Funkcja main odpowiadająca za uruchomienie aplikacji serwera
     * @param args
     */
    public static void main(String[] args) {
        launch();
            }
    }

