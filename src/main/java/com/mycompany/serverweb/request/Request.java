///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.mycompany.serverweb.request;
//
///**
// *
// * @author Acer
// */
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.InputStreamReader;
//import java.io.OutputStreamWriter;
//import java.net.Socket;
//
//public class Request {
//    private String method;
//    private String url;
//
//    public Request(String method, String url) {
//        this.method = method;
//        this.url = url;
//    }
//
//    public String sendRequest(String ipAddress, int port) {
//        try {
//            // Membuat koneksi ke server
//            Socket socket = new Socket(ipAddress, port);
//
//            // Mengirim permintaan HTTP
//            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//            out.write(method + " " + url + " HTTP/1.1\r\n");
//            out.write("Host: localhost\r\n");
//            out.write("\r\n");
//            out.flush();
//
//            // Membaca respons dari server
//            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            StringBuilder response = new StringBuilder();
//            String line;
//            while ((line = in.readLine()) != null) {
//                response.append(line).append("\n");
//            }
//
//            // Menutup koneksi
//            socket.close();
//
//            return response.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "Error: " + e.getMessage();
//        }
//    }
//
//    // Metode lain jika diperlukan
//}
