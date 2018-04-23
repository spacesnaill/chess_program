package com.frostburg.pjgeiger0;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
	// write your code here
        ServerSocket server;
        Socket client;
        PrintWriter output;
        BufferedReader input;
        int port_number = 4000;
        try {
            server = new ServerSocket(port_number);
            System.out.println("Server listening on port: " + port_number);
            client = server.accept();
            output = new PrintWriter(client.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));

            System.out.println("Client connected on port " + port_number + ", servicing reuqests.");
            String input_line;
            while((input_line = input.readLine()) != null){
                System.out.println(("Received message " + input_line + " from " + client.toString()));
                output.println(input_line);
            }

            server.close();
            client.close();
            output.close();
            input.close();
        }
        catch(IOException e){
            System.out.println(e);
        }
    }
}
