package com.frostburg.pjgeiger0;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

public class Main {

    //volatile private ArrayList<ClientHandler> clients = new ArrayList<>();
    //volatile private ArrayList<String> client_names = new ArrayList<>();

    volatile private Hashtable<String, ClientHandler> clients= new Hashtable<>();

    public static void main(String[] args) throws IOException {
        // write your code here
        new Main().startTheServer();

    }

    public void startTheServer() throws IOException {
        ServerSocket server;
        int port_number = 4000;

        server = new ServerSocket(port_number);

        while (true) {
            Socket client = null;
            try {
                client = server.accept();

                System.out.println("A new client has connected: " + client);

                DataInputStream input = new DataInputStream(client.getInputStream());
                DataOutputStream output = new DataOutputStream(client.getOutputStream());

                System.out.println("Assigning new thread to this client");
                ClientHandler t = new ClientHandler(client, input, output);
                t.start();
            } catch (Exception e) {
                client.close();
                e.printStackTrace();
            }
        }
    }

    class MatchHandler extends Thread {
        private final ClientHandler player_one;
        private final ClientHandler player_two;
        private boolean turn = false; //false = white, 1 = black
        private final boolean sides; //false = player one is white and player two is black, true = player one is black and player two is white

        public MatchHandler(ClientHandler sender, ClientHandler receiver) {
            Random coin_flip = new Random();
            player_one = sender;
            player_two = receiver;
            if (coin_flip.nextInt(100) > 50) {
                sides = false; //player one is white, player two is black
            } else {
                sides = true; //player one is white, player two is black
            }
        }

        //might reverse this so the coordinates can be multiplied by each other to get the location in the array
        String[][] board = new String[][]{
                {"A8", "BR1"}, {"B8", "BK1"}, {"C8", "BB1"}, {"D8", "BKing"}, {"E8", "BQueen"}, {"F8", "BB2"}, {"G8", "BK2"}, {"H8", "BR2"},
                {"A7", "BP1"}, {"B7", "BP2"}, {"C7", "BP3"}, {"D7", "BP4"}, {"E7", "BP5"}, {"F7", "BP6"}, {"G7", "BP7"}, {"H7", "BP8"},
                {"A6", ".."}, {"B6", "__"}, {"C6", ".."}, {"D6", "__"}, {"E6", ".."}, {"F6", "__"}, {"G6", ".."}, {"H6", "__"},
                {"A5", "__"}, {"B5", ".."}, {"C5", "__"}, {"D5", ".."}, {"E5", "__"}, {"F5", ".."}, {"G5", "__"}, {"H5", ".."},
                {"A4", ".."}, {"B4", "__"}, {"C4", ".."}, {"D4", "__"}, {"E4", ".."}, {"F4", "__"}, {"G4", ".."}, {"H4", "__"},
                {"A3", "__"}, {"B3", ".."}, {"C3", "__"}, {"D3", ".."}, {"E3", "__"}, {"F3", ".."}, {"G3", "__"}, {"H3", ".."},
                {"A2", "WP1"}, {"B2", "WP2"}, {"C2", "WP3"}, {"D2", "WP4"}, {"E2", "WP5"}, {"F2", "WP6"}, {"G2", "WP7"}, {"H2", "WP8"},
                {"A1", "WR1"}, {"B1", "WK1"}, {"C1", "WB1"}, {"D1", "WKing"}, {"E1", "WQueen"}, {"F1", "WB2"}, {"G1", "WK2"}, {"H1", "WR2"},
        };


        @Override
        public void run() {

        }

        public void movePiece(String piece, String currentLocation, String destination) {

        }
    }

    //inner class
    //Here the ClientHandler thread allows for multiple clients to join the server at once
    //Each client is given a ClientHandler thread
    //The ClientHandler thread then has a ClientHandlerInput thread that will watch for input from the client
    //Depending on the input the Client provides, the ClientHandlerInput thread may call a method from ClientHandler
    //ClientHandler can worry about sending data to the Client, since that doesn't really block up the flow of data like
    //listening for input does
    class ClientHandler extends Thread {
        private final DataInputStream input_stream;
        private final DataOutputStream output_stream;
        private final Socket client_socket;
        private String user_name;
        private boolean in_game = false; // am I in a game?
        private boolean my_turn = false; // is it my turn to go?

        public ClientHandler(Socket client, DataInputStream input, DataOutputStream output) throws IOException {
            input_stream = input;
            output_stream = output;
            client_socket = client;
        }

        public String getUser_name() {
            return user_name;
        }

        public void listUsers() throws IOException {
            output_stream.writeUTF(client_names.toString());
        }

        public void sendPing(String userName, String message) throws IOException {
            for (ClientHandler var : clients) {
                if (var.getUser_name().equals(userName)) {
                    var.receivePing(message);
                    break;
                }
            }
        }

        public void receivePing(String message) throws IOException {
            output_stream.writeUTF(message);
        }

        public void setMy_turn() {
            my_turn = true;
        }

        @Override
        public void run() {
            String receiving;
            String returning;
            //this is a sort of login, user provides a username and it's added to the list of users
            try {
                output_stream.writeUTF("Enter a username: ");
                user_name = input_stream.readUTF();
                clients.put(user_name, this);

            } catch (IOException e) {
                e.printStackTrace();
            }
            //commands are picked up by this thread
            ClientHandlerInput input = new ClientHandlerInput(client_socket, input_stream);
            input.start();
        }

        public void closeSocket(){
            //everything is closed if it was open
            try

            {
                input_stream.close();
                output_stream.close();
                client_socket.close();
            }
            catch(
                    IOException e)

            {
                e.printStackTrace();
            }
        }



        class ClientHandlerInput extends Thread{
            private final Socket socket;
            private final DataInputStream input;

            public ClientHandlerInput(Socket clientInput, DataInputStream clientInputStream){
                socket = clientInput;
                input = clientInputStream;
            }

            @Override
            public void run(){
                String receiving;
                while(true){
                    try{
                        receiving = input.readUTF();
                        System.out.println(receiving);
                        if(receiving.equals("exit")){
                            closeSocket();
                            break;
                        }
                        else if(receiving.split(" ")[0].equals("message")){
                            sendPing(receiving.split(" ")[1], receiving.split(" ")[2]);
                        }
                        //typing in names provides a list of all the users connected to the server
                        if(receiving.equals("names")){
                            listUsers();
                            continue;
                        }

                    }
                    catch(IOException e){
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }

    }//end of ClientHandler thread

//        class ClientHandlerOutput extends Thread{
//            private final Socket socket;
//            private final DataOutputStream output;
//
//            public ClientHandlerOutput(Socket clientOutput, DataOutputStream clientOutputStream){
//                socket = clientOutput;
//                output = clientOutputStream;
//            }
//
//            @Override
//            public void run(){
//                while(true){
//
//                }
//            }
//        }


}


