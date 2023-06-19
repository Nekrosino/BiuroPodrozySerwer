package com.example.serwer;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.sql.*;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.UUID;


/**
 *
 Klasa HelloController implementuje interfejs Initializable z JavaFX i jest kontrolerem dla widoku loginMenu.fxml
 */
public class HelloController implements Initializable {

    private Stage stage;

    @FXML
    Label WelcomeLabel;
    @FXML
    Button ProfileBtn;
    @FXML
    Label SaldoLabel;
    private ServerSocket serverSocket;
    private Connection connection;


    private String baseUrl = "jdbc:mysql://localhost:3306/biuropodrozy";
    private String baseLogin = "root";

    private String basePassword = "root";

    private String returnedUsername;
    private String returnedPassword;
    private String returnedSurname;
    private String returnedSaldo;

    private String nazwaWycieczki;
    private String cenaWycieczki;
    private String dataRozpoczecia;
    private String dataZakonczenia;

    /**
     * Zapytania kierowane do bazy danych
     */
    String sql;
    /**
     *  kolekcja sesji klientów
     */
    private Map<String, String> sessions = new HashMap<>();

    /**
     * Metoda inicjalizująca serwer. Tworzy gniazdo serwera, nasłuchuje na połączenia klientów i obsługuje żądania.
     * @throws IOException
     * @throws SQLException
     */
    public void start_server() throws IOException, SQLException {

        // Tworzenie gniazda serwera
        serverSocket = new ServerSocket(1234);
        System.out.println("Serwer nasłuchuje na porcie 1234...");

        while (true) {
            // Oczekiwanie na połączenie klienta
            Socket clientSocket = serverSocket.accept();
            System.out.println("Połączono z klientem: " + clientSocket.getInetAddress().getHostAddress());

            // Tworzenie strumieni wejścia/wyjścia dla klienta
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Obsługa żądań klienta
            String request = in.readLine();
            System.out.println("Otrzymane żądanie: "+request);
            // Logowanie klienta
            if (request.startsWith("LOGIN")) {
                String[] parts = request.split(" ");
                String username = parts[1];
                String password = parts[2];
                openBase();
                sql = "Select login,haslo from klienci where login = ? and haslo = ?";
                ResultSet resultSet = executeQuery(sql,username,password);
                while (resultSet.next()) {
                     returnedUsername = resultSet.getString("login");
                     returnedPassword = resultSet.getString("haslo");

                }
                closeBase();
                System.out.println("Login z bazy:"+returnedUsername);
                System.out.println("Haslo z bazy:"+returnedPassword);
                // Sprawdzenie poprawności danych logowania
                if (checkCredentials(username, password)) {
                    // Generowanie identyfikatora sesji
                    String sessionId = generateSessionId();
                    sessions.put(sessionId, username);

                    // Wysłanie identyfikatora sesji do klienta
                    out.println("SESSION_ID " + sessionId);
                } else {
                    out.println("LOGIN_FAILED");
                }
            }

            // Wylogowanie klienta
            else if (request.startsWith("LOGOUT")) {
                String sessionId = request.split(" ")[1];

                // Sprawdzenie, czy podany identyfikator sesji jest poprawny
                if (sessions.containsKey(sessionId)) {
                    sessions.remove(sessionId); // Usunięcie sesji klienta
                    out.println("LOGOUT_SUCCESS");
                } else {
                    out.println("LOGOUT_FAILED");
                }
            }

            else if(request.startsWith("PROFILE"))
            {
                String[] parts = request.split(" ");
                String username = parts[1];
                String password = parts[2];
                System.out.println("Otrzymane dane"+parts[1]);
                System.out.println("Otrzymane dane"+parts[2]);
                openBase();
                sql = "Select * from klienci where login = ? and haslo = ?";
                ResultSet resultSet = executeQuery(sql,username,password);
                while (resultSet.next()) {
                    returnedUsername = resultSet.getString("Imie");
                    returnedSurname = resultSet.getString("Nazwisko");
                    returnedSaldo = resultSet.getString("portfel");

                }
                closeBase();
                System.out.println(returnedUsername);
                System.out.println(returnedSurname);
                System.out.println(returnedSaldo);
                out.println("PROFILEDATA " +returnedUsername+" "+returnedSurname+" "+returnedSaldo);
            }
            else if(request.startsWith("REGISTERUSER"))
            {


                String[] parts = request.split(" ");
                int idklienta = 3 ;
                String name = parts[1];
                String surname = parts[2];
                String adres = parts[3];
                String numertel = parts[4];
                String email = parts[5];
                String login = parts[6];
                String haslo = parts[7];
                String portfel = parts[8];

                openBase();
                sql = "INSERT INTO klienci (idKlient, Imie, Nazwisko, Adres, NumerTel,Email,login,haslo,portfel) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                int AffectedRows = executeUpdate(sql,idklienta,name,surname,adres,numertel,email,login,haslo,portfel);
                closeBase();

            }

            // Obsługa innych żądań...

            else if(request.startsWith("GETWYCIECZKA"))
            {
                String[] parts = request.split(" ");
                String idWycieczki = parts[1];

                openBase();
                sql = "SELECT * FROM wycieczki WHERE idwycieczki = ? ";

                ResultSet resultSet = executeQuery(sql,idWycieczki);
                while (resultSet.next()) {
                     nazwaWycieczki = resultSet.getString("nazwa");
                     dataRozpoczecia = resultSet.getString("data_rozpoczecia");
                     dataZakonczenia = resultSet.getString("data_zakonczenia");
                     cenaWycieczki = resultSet.getString("cena");

                }
                closeBase();
                out.println("GETWYCIECZKA " + nazwaWycieczki +" "+dataRozpoczecia+" "+dataZakonczenia+" "+cenaWycieczki);

            }


            clientSocket.close();
        }
    }

    /**
     * Metoda sprawdzająca poprawność danych logowania na podstawie zwróconych wartości z bazy danych.
     * @param username nazwa użytkownika
     * @param password hasło użytkownika
     * @return zwraca wartość true albo false
     */
    private boolean checkCredentials(String username, String password) {


        return username.equals(returnedUsername) && password.equals(returnedPassword);
    }

    /**
     *  Metoda generująca unikalny identyfikator sesji.
     * @return zwraca unikalny identyfikator
     */
    private String generateSessionId() {

        return UUID.randomUUID().toString();
    }

    /**
     * Metoda obsługująca przełączenie na widok menu głównego.
     * @param event obsługa zdarzenia
     * @throws IOException
     */
    public void switchToMenu(ActionEvent event) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("loginMenu.fxml"));
        Scene loginMenu = new Scene( root);
        stage.setTitle("Hello!");
        String css=this.getClass().getResource("style.css").toExternalForm();
        loginMenu.getStylesheets().add(css);
        stage.setScene(loginMenu);
        stage.show();
    }


    /**
     *  Metoda inicjalizująca kontroler. Uruchamia serwer i ustawia odpowiednie wartości w interfejsie użytkownika.
     * @param url
     * @param resourceBundle
     */
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            start_server();
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * Metoda zamykająca gniazdo serwera.
     * @throws IOException
     */
    public void stop() throws IOException {
        // Zamknięcie gniazda serwera
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    /**
     * Metoda otwierająca połączenie z bazą danych
     * @throws SQLException
     */
    public void openBase() throws SQLException {
        connection = DriverManager.getConnection(baseUrl,baseLogin,basePassword);
    }

    /**
     *  Metoda przyjmuje zapytanie SQL oraz opcjonalne parametry i wykonuje to zapytanie na połączeniu z bazą danych.
     * @param sql zapaytanie języka SQL
     * @param parameters parametry zapytania
     * @return zwraca wynik zapytania jako obiekt typu ResultSet
     * @throws SQLException
     */
    public ResultSet executeQuery(String sql, Object... parameters) throws  SQLException{
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for(int i=0;i<parameters.length;i++)
        {
            preparedStatement.setObject(i+1,parameters[i]);
        }

        return preparedStatement.executeQuery();
    }

    /**
     * Metoda executeUpdate przyjmuje zapytanie SQL oraz opcjonalne parametry i wykonuje to zapytanie na połączeniu z bazą danych.
     * @param sql zapaytanie języka SQL
     * @param parameters parametry zapytania
     * @return zwraca liczbę zmienionych rekordów jako wynik wykonanego zapytania.
     * @throws SQLException
     */
    public int executeUpdate(String sql, Object... parameters) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for(int i = 0; i < parameters.length; i++) {
            preparedStatement.setObject(i + 1, parameters[i]);
        }

        return preparedStatement.executeUpdate();
    }

    /**
     * Metoda zamykająca połączenie z bazą
     * @throws SQLException
     */
    public void closeBase() throws  SQLException{
        if(connection!=null)
        {
            connection.close();
        }
    }



}
