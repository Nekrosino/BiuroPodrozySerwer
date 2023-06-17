package com.example.serwer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.io.*;
import java.net.*;



/////
//// gdy chcemy dodać labela ktory sie inicjalizuje (funkcja initialize) to musi byc w kazdym pliku FXML zwlaszcza w tym ktory odpala sie jako pierwszy
////    u nas to hello-view
public class HelloApplication extends Application {
    public static String user;
    public static float saldo;
    public static String saldostr;
    public static String password;

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
            while(true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Połączono z klientem: " + clientSocket.getInetAddress().getHostAddress());




                ///////////////////////////
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jdbc-travel-managment-system", "root", "root");
                Statement statement = connection.createStatement();
               // ResultSet resultSet = statement.executeQuery("SELECT * FROM klienci WHERE idKlient = 2");

//                while (resultSet.next()) {
//                    System.out.println("Imie: " + resultSet.getString("Imie"));
//                    System.out.println("Nazwisko: " + resultSet.getString("Nazwisko"));
//                    System.out.println("Login: " + resultSet.getString("login"));
//                    System.out.println("Haslo: " + resultSet.getString("haslo"));
//                    System.out.println("Portfel: " + resultSet.getString("portfel"));
//                    user = resultSet.getString("Imie");
//                    saldo = resultSet.getFloat("portfel");
//                    password = resultSet.getString("haslo");
//                    System.out.println("Nazwa uzytkownika: " + user);
//                    System.out.println("Haslo uzytkownika: " + password);
//
//                }

                saldostr = Float.toString(saldo);

                //odczytywanie zapytan
//            String request, response;
//            while ((request = in.readLine()) != null) {
//                System.out.println("Otrzymano zapytanie od klienta: " + request);
//                response = "" + user;
//                out.println(response);
//                response = "" + saldo;
//                out.println(response);
//                response = "" + password;
//                out.println(response);
//            }
                // Inicjalizacja strumieni wejścia/wyjścia
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String query1 = in.readLine();
                System.out.println("Otrzymane zapytanie login: " + query1);
               // String zapytanielogin = query1;
                String query2 = in.readLine();
                System.out.println("Otrzymane zapytanie haslo: " + query2);
                String zapytanie = "Select login,haslo from klienci where login = ? and haslo = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(zapytanie);
                preparedStatement.setString(1, query1);
                preparedStatement.setString(2, query2);
                ResultSet resultSet = preparedStatement.executeQuery();

                while(resultSet.next())
                {
                    String login = resultSet.getString("login");
                    out.println(login);
                    String haslo = resultSet.getString("haslo");
                    out.println(haslo);

                  System.out.println("Haslo: " + resultSet.getString("haslo"));
                }



                // Przetwarzanie zapytania (np. sprawdzanie loginu i hasła)

                // Odpowiedź serwera (może być wysłana z powrotem do klienta)

                // Zamknięcie strumieni i gniazda klienta
                //in.close();

                launch();
                preparedStatement.close();
                connection.close();


                //////////////////////////

                // Zamknięcie połączenia
                in.close();
                //out.close();
                clientSocket.close();
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
