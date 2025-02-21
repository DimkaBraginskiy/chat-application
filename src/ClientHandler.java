import java.io.*;
import java.net.Socket;
import java.util.Map;

public
class ClientHandler
implements Runnable{
    private int clientPort;
    private Socket clientSocket;
    private String clientName;
    private PrintWriter writer;
    private BufferedReader reader;
    private MyServer server;
    private static MessageHandler messageHandler;


    public ClientHandler(Socket socket, MyServer server){ // Constructor of the class and what should we pass for it
        this.clientSocket = socket; // needed to operate on a specific client.
        this.server = server;
        clientPort = clientSocket.getPort(); // Registering client's port.
        messageHandler = new MessageHandler(MyServer.getClients(), server.getBannedPhrases());//Creating an instance of message handler

    }


    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);//flushable-true



            while(true){
                writer.println("Enter your name: ");
                clientName = reader.readLine();


                synchronized (MyServer.getClients()){
                    if(!(MyServer.getClients().containsKey(clientName)) && !(clientName.contains(" "))){

                        MyServer.getClients().put(clientName, this);
                        writer.println("Welcome: "+clientName+"!");
                        writer.println(MyServer.getRules());//sending rules
                        System.out.println("Client connected: "+clientName+"; Port: "+clientPort);



                        notifyClients(clientName+" has joined the chat and is online", this);


                        break;
                    }
                }
                writer.println("This name is already being used or was written in a wrong way. " +
                        "Choose another one. " +
                        "\nConsider prohibition of spaces and Banned Phrases.");

            }

            String message;
            while((message = reader.readLine()) != null){
                messageHandler.handleMessage(message, clientName, this);
            }

        } catch (IOException e) {
            System.err.println("Connection with client lost.");
            //throw new RuntimeException(e);
        } finally {
            disconnectClient();
            closeConnection();//closing connection so all resources were released.
        }
    }

    public String receiveMessage() {
        try {
            return reader.readLine(); // Получение сообщения с сервера
        } catch (IOException e) {
            System.err.println("Error receiving message: " + e.getMessage());
            return null;
        }
    }

    public void notifyClients(String message, ClientHandler excludeClient){
        synchronized (MyServer.getClients()){
            for(Map.Entry<String, ClientHandler> entry : MyServer.getClients().entrySet()){
                ClientHandler client = entry.getValue();//name of client
                if(client != excludeClient){
                    client.sendMessage(message);
                }
            }
        }
    }

    public void disconnectClient(){
        synchronized(MyServer.getClients()){
            if(clientName != null){
                MyServer.getClients().remove(clientName);
                System.out.println("Client disconnected: "+clientName);
                notifyClients(clientName+" has left the chat and offline now.", this);
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    public PrintWriter getWriter(){
        return writer;
    }

    public BufferedReader getReader(){
        return reader;
    }

    public String getClientName(){
        return clientName;
    }

    public void sendMessage(String message){
        writer.println(message);
    }

    public void closeConnection(){

        try {
            if(reader != null){
                reader.close();
            }
            if(writer != null){
                writer.close();
            }
            if(clientSocket != null){
                clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Error occured while closing the connection for: "+clientName);
            //throw new RuntimeException(e);
        }

    }
}
