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
    private ServerSocket serverSocket;
    private Connection connection;
    private Statement statement;

    private PreparedStatement preparedStatement;

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


    String sql;
    private Map<String, String> sessions = new HashMap<>(); // kolekcja sesji klientów
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
               // System.out.println(returnedUsername);
               // System.out.println(returnedSurname);
                //System.out.println(returnedSaldo);
                out.println("GETWYCIECZKA " + nazwaWycieczki +" "+dataRozpoczecia+" "+dataZakonczenia+" "+cenaWycieczki);

            }

            // Zamknięcie połączenia z klientem
            clientSocket.close();
        }
    }

    private boolean checkCredentials(String username, String password) {
        // Sprawdzenie poprawności danych logowania
        // (możesz zaimplementować własną logikę weryfikacji danych)

        return username.equals(returnedUsername) && password.equals(returnedPassword);
    }

    private String generateSessionId() {
        // Generowanie unikalnego identyfikatora sesji
        // (możesz zaimplementować własną logikę generowania identyfikatorów sesji)
        return UUID.randomUUID().toString();
    }


    public void switchToMenu(ActionEvent event) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("loginMenu.fxml"));
        Scene loginMenu = new Scene( root);
        stage.setTitle("Hello!");
        String css=this.getClass().getResource("style.css").toExternalForm();
        loginMenu.getStylesheets().add(css);
        stage.setScene(loginMenu);
        stage.show();




    }

    public void onProfileButtonClick(ActionEvent e)
    {
        SaldoLabel.setText(saldostr);
    }



    public void initialize(URL url, ResourceBundle resourceBundle) {
        //WelcomeLabel.setText("Hello "+ user+"!");

        try {
            start_server();
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }


    }

    public void stop() throws IOException {
        // Zamknięcie gniazda serwera
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    public void openBase() throws SQLException {
        connection = DriverManager.getConnection(baseUrl,baseLogin,basePassword);
    }

    public ResultSet executeQuery(String sql, Object... parameters) throws  SQLException{
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for(int i=0;i<parameters.length;i++)
        {
            preparedStatement.setObject(i+1,parameters[i]);
        }

        return preparedStatement.executeQuery();
    }

    public int executeUpdate(String sql, Object... parameters) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for(int i = 0; i < parameters.length; i++) {
            preparedStatement.setObject(i + 1, parameters[i]);
        }

        return preparedStatement.executeUpdate();
    }

    public void closeBase() throws  SQLException{
        if(connection!=null)
        {
            connection.close();
        }
    }



}
