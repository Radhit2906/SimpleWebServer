/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.serverweb.server;

/**
 *
 * @author Acer
 */
import com.mycompany.serverweb.main.Main;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.prefs.Preferences;

public class WebServer {
    private int port;
    private final String webDirectory;
    private final String logDirectory;
    private ServerSocket serverSocket;
    private boolean running;
    
    //constructor class WebServer untuk inisialisasi variable ketika buat objek WebServer
    public WebServer(int port, String webDirectory, String logDirectory) {
        this.port = port;
        this.webDirectory = webDirectory;
        this.logDirectory = logDirectory;
    }
    //Method Start ketika memulai server, membuat objek ServerSocket dan menerima koneksi dari Client
    public void start() {
//        Preferences prefs = Preferences.userNodeForPackage(Main.class);
//        int defaultPort = prefs.getInt("port", 8080);
//        String defaultWebDir = prefs.get("webdir", "D:/Localhost/");
//        String defaultLogDir = prefs.get("logdir", "D:/Web Logs");
        if (!running) {
            try {
                serverSocket = new ServerSocket(port, 0, InetAddress.getByName("localhost"));
                System.out.println("Web server started on port " + port);
                running = true;
                
//                int port = Integer.parseInt(portField.getText());
//                String webDirectory = 
                
                //ketika diterima, server membuat objek clienthandler dan thread baru untuk menangani koneksi
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    Thread thread = new Thread(new ClientHandler(clientSocket));
                    thread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //Method Stop untuk menghentikan Server  dengan menutup serversocket
    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Web server stopped");
    }
    //inner class ClientHandler untuk menangani setiap koneksi klien yang diterima oleh server.
    //membaca permintaan client dan catat di log
    private class ClientHandler implements Runnable { 
        private Socket clientSocket;
        //Constructor ini menginisialisasi dg socket klien yg diterima 
        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }
    //permintaan diproses dengan cara memanggil method handlerequest  
        @Override
        //Method Run Method ini menangani proses komunikasi dengan klien, membaca permintaan client 
        //implementasi runnable 
        public void run() {
            //menerima permintaan dari client dan mengirimkan respon ke client
            try {
                //input / membaca data yang dikirim client
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                //output / menulis balasan / menulis respon
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                
                //membaca permintaan request , disimpan pada variable request, membaca permintaan dengan in.readline
                String request = in.readLine();
                System.out.println("Received request: " + request);

                // Log request to access log, memanggil method untuk mencatat
                //butuh alamat ip dan permintaan yang diterima 
                logAccess(clientSocket.getInetAddress().getHostAddress(), request);

                // request dibagi menjadi token-token menggunakan spasi sebagai pemisah
                //bagian array akan diisi path dari sumber daya yang diminta
                String[] tokens = request.split(" ");
                //tokens[1] merupakan bagian permintaan GET
                String requestedPath = tokens[1];

                // Handle GET request
                //permintaan ditangani dengan memanggil method handleRequest()
                handleRequest(requestedPath, out);

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        //Metode ini mencatat setiap akses ke server dalam file log. Setiap entri log berisi tanggal, alamat IP klien, dan permintaan yang diterima.
        private void logAccess(String ipAddress, String request) {
            //Tanggal Saat ini 
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String logFileName = dateFormat.format(new Date()) + ".log";    //Nama file log dengan format "YYYY-MM-DD.log"
            String logFilePath = Paths.get(logDirectory, logFileName).toString();
            try {
                File logFile = new File(logFilePath);
                if (!logFile.exists()) {
                    //Membuat FileLog
                    logFile.createNewFile();
                }
                //Format log entry dengan tanggal, alamat IP klien, dan permintaan
                String logEntry = String.format("[%s] %s - %s\n", new Date(), ipAddress, request);
                //Tulis log entry ke dalam file log
                Files.write(Paths.get(logFilePath), logEntry.getBytes(), java.nio.file.StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Method HandleRequest
        //Metode ini bertanggung jawab untuk menangani permintaan yang diterima dari klien. Permintaan diproses sesuai dengan path yang diminta oleh klien.
        private void handleRequest(String requestedPath, BufferedWriter out) throws IOException {
            //file baru yang merepresentasikan file atau direktori yang diminta oleh klien
            File file = new File(webDirectory, requestedPath.substring(1));
            //Jika File Sudah ada
            if (file.exists()) {
                //Jika file yang diminta oleh klien adalah sebuah direktori,
                if (file.isDirectory()) {
                    //  maka server akan mengirimkan daftar file dalam direktori tersebut kepada klien.
                    sendDirectoryListing(file, out);
            //Jika belum ada 
                } else {
                    // Jika file yang diminta oleh klien adalah file dengan ekstensi .html, 
                    //maka konten dari file tersebut akan dikirimkan kepada klien dengan menggunakan metode sendHTMLFileContent.
                    if (requestedPath.endsWith(".html")) {
                        sendHTMLFileContent(file, out);
                        //Jika file yang diminta oleh klien bukanlah file HTML, 
                        //maka konten dari file tersebut akan dikirimkan kepada klien dengan menggunakan metode sendFileContent.
                    } else {
                        sendFileContent(file, clientSocket.getOutputStream());
                    }
                }
            } else {
                // Jika file tidak ditemukan, kirim respons 404 Not Found
                send404Response(out);
            }
        }
        //Metode sendDirectoryListing bertanggung jawab untuk mengirim daftar file dalam sebuah direktori kepada klien dalam format HTML.
        private void sendDirectoryListing(File directory, BufferedWriter out) throws IOException {
            //Metode ini menggunakan Files.list(directory.toPath()) untuk mendapatkan daftar path dari semua file dalam direktori yang diberikan
            List<String> fileList = Files.list(directory.toPath())
                    //dengan menggunakan .map() dan .collect(), daftar path tersebut diubah menjadi daftar nama file.
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
            //header HTTP untuk respons, menetapkan status kode 200 OK, dan menetapkan tipe konten sebagai text/html.
            StringBuilder responseBuilder = new StringBuilder();
            responseBuilder.append("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n");
            
            //--------------------------------------------//
            
            responseBuilder.append("<h1>Directory Listing:</h1>");
            responseBuilder.append("<ul>");
            //for untuk menghasilkan elemen <li> HTML untuk setiap file dalam daftar

            for (String fileName : fileList) {
                File file = new File(directory, fileName);
                //jika file tersebut adalah sebuah direktori, sebuah link dengan tanda "/" diakhirnya akan ditambahkan supaya dapat masuk
                if (file.isDirectory()) {
                    responseBuilder.append("<li><a href=\"").append(fileName).append("/\">").append(fileName).append("/</a></li>");
                    //Jika bukan, link tersebut hanya akan menampilkan nama file tanpa tanda "/"
                } else {
                    responseBuilder.append("<li><a href=\"").append(fileName).append("\">").append(fileName).append("</a></li>");
                }
            }
            responseBuilder.append("</ul>");
            
            //Kode ini mengirimkan daftar HTML yang dibuat ke klien melalui BufferedWriter out
            out.write(responseBuilder.toString());
            out.flush();
        }
        //mengirim konten dari sebuah file kepada klien
        //mengirim konten selain html
        private void sendFileContent(File file, OutputStream outputStream) throws IOException { //IOException adalah pengecualian yang umumnya dilemparkan ketika terjadi kesalahan dalam operasi input/output (I/O)
            // Mendapatkan tipe konten berdasarkan ekstensi file
            String contentType = Files.probeContentType(file.toPath());

            // Mengirim respons HTTP dengan tipe konten yang sesuai
            outputStream.write(("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n").getBytes());

            // Baca dan kirim konten file
            byte[] fileContent = Files.readAllBytes(file.toPath()); //digunakan untuk membaca seluruh konten dari file 
            outputStream.write(fileContent);
            outputStream.flush(); //memastikan semua data sudah dikirim 
        }

        
        //mengirim respons 404 Not Found kepada klien jika file yang diminta tidak ditemukan di server
        private void send404Response(BufferedWriter out) throws IOException {
            out.write("HTTP/1.1 404 Not Found\r\n\r\n");
            //Kode ini memastikan bahwa semua data yang telah ditulis ke BufferedWriter telah dikirim ke klien dengan memanggil metode flush()
            out.flush();
        }
        
        //mengirim konten dari sebuah file HTML kepada klien
        private void sendHTMLFileContent(File file, BufferedWriter out) throws IOException {
            // Mendapatkan tipe konten berdasarkan ekstensi file
            String contentType = "text/html";

            // Mengirim respons HTTP dengan tipe konten yang sesuai
            out.write("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n");
            out.flush();

            // Baca dan kirim konten file
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                out.write(line);
                out.newLine();
            }
            reader.close();
            out.flush();
        }

    }
}





