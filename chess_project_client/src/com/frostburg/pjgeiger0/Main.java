package com.frostburg.pjgeiger0;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
	// write your code here
        String host_name = "127.0.0.1";
        int port_number = 4000;
        try {
            Socket socket = new Socket(host_name, port_number);
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedReader standard_input = new BufferedReader(new InputStreamReader(System.in));

            String user_input;
            while((user_input = standard_input.readLine()) != null){
                output.println(user_input);
                System.out.println("echo: " + input.readLine());
            }

            socket.close();
            output.close();
            input.close();
            standard_input.close();
        }
        catch(IOException e){
            System.out.println(e);
            System.exit(1);
        }
    }
}
