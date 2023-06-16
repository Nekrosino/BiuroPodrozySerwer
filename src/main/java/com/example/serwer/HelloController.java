package com.example.serwer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.example.serwer.HelloApplication.user;
import static com.example.serwer.HelloApplication.saldostr;

public class HelloController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    Label WelcomeLabel;
    @FXML
    Button ProfileBtn;
    @FXML
    Label SaldoLabel;


    public void switchToMenu(ActionEvent event) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("hello-view.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        // scene.getStylesheets().add(css);
        //setUsername(username);
        stage.setScene(scene);
        stage.show();




    }

    public void onProfileButtonClick(ActionEvent e)
    {
        SaldoLabel.setText(saldostr);
    }


    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        WelcomeLabel.setText("Hello "+ user+"!");


    }
}
