package com.frostburg.pjgeiger0;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here
        ServerSocket server;
        int port_number = 4000;

        server = new ServerSocket(port_number);
        while(true){
            Socket client = null;
            try{
                client = server.accept();

                System.out.println("A new client has connected: " + client);

                DataInputStream input = new DataInputStream(client.getInputStream());
                DataOutputStream output = new DataOutputStream(client.getOutputStream());

                System.out.println("Assigning new thread to this client");

                Thread t = new ClientHandler(client, input, output);

                t.start();
            }
            catch(Exception e){
                client.close();
                e.printStackTrace();
            }
        }

    }
}

class ClientHandler extends Thread
{
    final DataInputStream input_stream;
    final DataOutputStream output_stream;
    final Socket client_socket;

    public ClientHandler(Socket client, DataInputStream input, DataOutputStream output){
        input_stream = input;
        output_stream = output;
        client_socket = client;
    }

    @Override
    public void run(){
        String receiving;
        String returning;

        while(true){
            try {
                output_stream.writeUTF("Pong");
                receiving = input_stream.readUTF();

                if(receiving.equals("Exit")){
                    System.out.println("Closing connection with: " + client_socket);
                    client_socket.close();
                    System.out.println("Connection closed");
                    break;
                }
                if(receiving.equals("Ping")){
                    returning = "Pong";
                    output_stream.writeUTF(returning);
                    break;
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        try{
            input_stream.close();
            output_stream.close();
            client_socket.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

}
