package com.frostburg.pjgeiger0;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
	// write your code here
        new Main().startClient();

    }

    public void startClient(){
        int port_number = 4000;
        try{
            Scanner system_input = new Scanner(System.in);

            InetAddress ip = InetAddress.getByName("localhost");

            Socket client = new Socket(ip, port_number);
            DataOutputStream output = new DataOutputStream(client.getOutputStream());
            IncomingData input = new IncomingData(client);
            input.start();

            while(true){
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
            output.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    class IncomingData extends Thread {
        private final Socket clientSocket;
        private final DataInputStream input;

        public IncomingData(Socket client) throws IOException{
            clientSocket = client;
            input = new DataInputStream(clientSocket.getInputStream());
        }

        @Override
        public void run(){
            String receiving;
            while (true) {
                try{
                    receiving = input.readUTF();
                    System.out.println(receiving);
                }
                catch(IOException e){
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
