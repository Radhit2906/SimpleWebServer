package com.mycompany.serverweb.main;

import com.mycompany.serverweb.server.WebServer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;

import java.io.File;
import java.util.prefs.Preferences;

//implementasikan antarmuka Application
public class WebServerGUI extends Application {
    private boolean serverRunning = false;
    private WebServer server;
    private Thread serverThread;
    private Preferences prefs;

//    public static void main(String[] args) {
//        launch(args);
//    }

    @Override
    //method start 
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Web Server");

        prefs = Preferences.userNodeForPackage(Main.class);
        //Port
        int defaultPort = prefs.getInt("port", 8080);
        //direktori webserver
        String defaultWebDir = prefs.get("webdir", "D:/Localhost/");
        //direktori log
        String defaultLogDir = prefs.get("logdir", "D:/Localhost/Logs");
        
        //pengaturan grid
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(10);
        grid.setHgap(10);
        
        // Port
        Label portLabel = new Label("Port:");
        GridPane.setConstraints(portLabel, 0, 0);
        TextField portField = new TextField();
        portField.setText(String.valueOf(defaultPort));
        GridPane.setConstraints(portField, 1, 0);

        // Web Directory
        Label directoryLabel = new Label("Web Directory:");
        GridPane.setConstraints(directoryLabel, 0, 1);
        TextField directoryField = new TextField();
        directoryField.setEditable(false);
        directoryField.setText(defaultWebDir);
        GridPane.setConstraints(directoryField, 1, 1);
        Button browseWebDirectoryButton = new Button("Browse");
        browseWebDirectoryButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Web Directory");
            File selectedDirectory = directoryChooser.showDialog(primaryStage);
            if (selectedDirectory != null) {
                directoryField.setText(selectedDirectory.getAbsolutePath());
            }
        });
        GridPane.setConstraints(browseWebDirectoryButton, 2, 1);

        // Log Directory
        Label logLabel = new Label("Log Directory:");
        GridPane.setConstraints(logLabel, 0, 2);
        TextField logField = new TextField();
        logField.setEditable(false);
        logField.setText(defaultLogDir);
        GridPane.setConstraints(logField, 1, 2);
        Button browseLogDirectoryButton = new Button("Browse");
        browseLogDirectoryButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Log Directory");
            File selectedDirectory = directoryChooser.showDialog(primaryStage);
            if (selectedDirectory != null) {
                logField.setText(selectedDirectory.getAbsolutePath());
            }
        });
        GridPane.setConstraints(browseLogDirectoryButton, 2, 2);
        
        //Log TextArea
        TextArea logTextArea = new TextArea();
        logTextArea.setEditable(false); // Buat TextArea tidak dapat diedit oleh pengguna
        logTextArea.setWrapText(true); // Mengatur agar teks wrap ke baris baru jika tidak cukup lebar
        logTextArea.setPrefRowCount(10); // Tentukan jumlah baris yang ditampilkan secara default
        GridPane.setConstraints(logTextArea, 0, 5, 3, 1); // Atur posisi dan span untuk logTextArea
        
        
        // Menambahkan logTextArea ke dalam grid
        grid.getChildren().add(logTextArea);

        //label untuk server running
        Label serverStatusLabel = new Label("Server Status: Not Running");
        GridPane.setConstraints(serverStatusLabel, 0, 4); // Atur posisi label
        grid.getChildren().add(serverStatusLabel); // Tambahkan label ke grid

        // Start/Stop Button
        Button startStopButton = new Button("Start");
        serverThread = null; // Inisialisasi di sini
        startStopButton.setOnAction(e -> {
            if (!serverRunning) {
                String port = portField.getText();
                String webDirectory = directoryField.getText();
                String logDirectory = logField.getText();

                // Simpan preferensi yang diubah oleh pengguna
                prefs.putInt("port", Integer.parseInt(port));
                prefs.put("webdir", webDirectory);
                prefs.put("logdir", logDirectory);

                server = new WebServer(Integer.parseInt(port), webDirectory, logDirectory);
                serverThread = new Thread(() -> server.start());
                serverThread.setDaemon(true);
                serverThread.start();
                startStopButton.setText("Stop");
                serverRunning = true;
                
                //teks status
                serverStatusLabel.setText("Server Status: Running on Port " + port);
                // Menambahkan pesan ke logTextArea
                logTextArea.appendText("Web server started on port " + port + "\n");
                // Menambahkan pesan lain ke logTextArea
                //logTextArea.appendText("Received request: " + request + "\n");
            } else {
                server.stop();
                if (serverThread != null) {
                    serverThread.interrupt();
                }
                startStopButton.setText("Start");
                serverRunning = false;
                serverStatusLabel.setText("Server Status: Not Running");
                //Menambahkan pesan lain ke logTextArea
                logTextArea.appendText("Server stopped\n");
            }
        });
        GridPane.setConstraints(startStopButton, 0, 3);

        grid.getChildren().addAll(portLabel, portField, directoryLabel, directoryField, browseWebDirectoryButton,
                logLabel, logField, browseLogDirectoryButton, startStopButton);

        Scene scene = new Scene(grid, 500, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
