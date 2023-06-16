package com.example.serwer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HelloApplication extends Application {
    public static String user;
    public static float saldo;
    public static String saldostr;

    @Override
    public void start(Stage stage) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("loginMenu.fxml"));
        Scene loginMenu = new Scene( root);
        stage.setTitle("Hello!");
        String css=this.getClass().getResource("style.css").toExternalForm();
        loginMenu.getStylesheets().add(css);
        stage.setScene(loginMenu);
        stage.show();

    }

    public static void main(String[] args) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jdbc-travel-managment-system", "root", "root");
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM klienci WHERE idKlient = 2");

        while (resultSet.next()) {
            System.out.println("Imie: " + resultSet.getString("Imie"));
            System.out.println("Nazwisko: " + resultSet.getString("Nazwisko"));
            System.out.println("Login: " + resultSet.getString("login"));
            System.out.println("Haslo: " + resultSet.getString("haslo"));
            System.out.println("Portfel: " + resultSet.getString("portfel"));
            user = resultSet.getString("Imie");
            saldo = resultSet.getFloat("portfel");
            System.out.println("Imie uzytkownika: "+user);

        }
        saldostr = Float.toString(saldo);
        launch();
        connection.close();
    }
}
