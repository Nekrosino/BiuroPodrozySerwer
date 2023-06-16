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
import java.io.*;
import java.net.*;



/////
//// gdy chcemy dodać labela ktory sie inicjalizuje (funkcja initialize) to musi byc w kazdym pliku FXML zwlaszcza w tym ktory odpala sie jako pierwszy
////    u nas to hello-view
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



        try {
            // Tworzenie gniazda serwera
            ServerSocket serverSocket = new ServerSocket(1234);
            System.out.println("Serwer nasłuchuje na porcie 1234...");

            // Oczekiwanie na połączenie klienta
            Socket clientSocket = serverSocket.accept();
            System.out.println("Połączono z klientem: " + clientSocket.getInetAddress().getHostAddress());

            // Inicjalizacja strumieni wejścia/wyjścia
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);





            ///////////////////////////
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/biuropodrozy", "root", "root");
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

            //odczytywanie zapytan
            String request, response;
            while ((request = in.readLine()) != null) {
                System.out.println("Otrzymano zapytanie od klienta: " + request);
                response = "" + user;
                out.println(response);
                response = "" + saldo;
                out.println(response);
            }

            launch();
            connection.close();


            //////////////////////////

            // Zamknięcie połączenia
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
