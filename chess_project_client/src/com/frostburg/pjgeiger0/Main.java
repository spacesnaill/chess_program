package com.frostburg.pjgeiger0;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
	// write your code here
        int port_number = 4000;
        try{
            Scanner system_input = new Scanner(System.in);

            InetAddress ip = InetAddress.getByName("localhost");

            Socket client = new Socket(ip, port_number);
            DataInputStream input = new DataInputStream(client.getInputStream());
            DataOutputStream output = new DataOutputStream(client.getOutputStream());

            while(true){
                System.out.println(input.readUTF());
                String sending = system_input.nextLine();
                output.writeUTF(sending);

                if(sending.equals("Exit")){
                    System.out.println("closing the connection: " + client);
                    client.close();
                    System.out.println("connection closed");
                    break;
                }
            }
            system_input.close();
            input.close();
            output.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }
}
