import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public
class MyClient {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 7777);//Creating socket to connect to the server
            System.out.println("Connected to the server. Port: " + socket.getPort()+" Ip: " + socket.getInetAddress());

            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));//Reaading messages
            PrintWriter serverWriter = new PrintWriter(socket.getOutputStream(),true);//Writing messages

            ChatUI chatUI = new ChatUI(serverWriter);//Creating an instance of a UI based on our PrintWriter

            new Thread(() -> { //Handling incoming messages
                try {
                    String serverMessage;
                    while((serverMessage = serverReader.readLine()) != null){ //If message from server is not null - printing it on ui,
                        //System.out.println(serverMessage);
                        chatUI.appendServerMessage(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Connection lost");
                    throw new RuntimeException(e);
                }
            }).start();


            //Sending messages to the server in this section
            BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                String userInput = userInputReader.readLine();
                serverWriter.println(userInput); // writing message to the server
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
