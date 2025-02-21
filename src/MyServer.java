import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public
class MyServer {
    private int port;
    private String serverName;
    private List<String> bannedPhrases;
    private static HashMap<String, ClientHandler> clients = new HashMap<>(); //Shared client list. HashMap for faster access
    private static String rules = "=-=-=-=-=-=-=-=-=-=-=-=-=-=\n" +
            "Welcome to the server! Here are the commands you can type in:\n" +
            "@all <message> message all available users online;\n" +
            "@user1,user2 <message> message only marked users;\n" +
            "@!user1,user2 <message> message all users except marked ones;\n" +
            "/quit - disconnect from the server;\n" +
            "/bp - preview banned phrases;\n" +
            "/lu - preview all users currently online;\n" +
            "/rules - preview command list once more.\n" +
            ">>>Enjoy your stay<<<\n" +
            "=-=-=-=-=-=-=-=-=-=-=-=-=-=";


    public MyServer(){
        clients = new HashMap<>();
        loadconfig("C:\\Users\\Dimka\\Documents\\IdeaProjects\\utpProject02\\src\\serverConfig.txt");
    }

    private void loadconfig(String path){
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));

            //setting the port number:
            this.port = Integer.parseInt(bufferedReader.readLine().trim()); //trim removes whitespaces

            //setting the server name:
            this.serverName = bufferedReader.readLine().trim();

            //Creating a temporary line for banned words (using trim() to clear spaces outside quotes)
            String bannedPhrasesLine = bufferedReader.readLine().trim();
            this.bannedPhrases = new ArrayList<>();

            //splitting phrases based on comma as a separator:
            String[] phrases = bannedPhrasesLine.split(",");//splitting into multiple element of array when comma is met (our separator in the file)
            for(String phrase : phrases){
                bannedPhrases.add(phrase); // from temporary string array we put our splitted strings into our ArrayList.
            }

            System.out.println("Config loaded: " + path +
                    "\nPort: " + port +
                    " Server Name: " + serverName+
                    "\nBanned phrases: "+String.join(", ", bannedPhrases)); // notifying that the config was successfully loaded!;)

        } catch (FileNotFoundException e) {
            System.out.println("Config file not found.");
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("Reading failed.");
            throw new RuntimeException(e);
        }

    }

    public void start(){
        try {
            ServerSocket ss = new ServerSocket(port); //creating an instance that will listen for connection based on provided port. Works only if the port is not being already used. Otherwise throws an exception.
            System.out.println("Server " + serverName + " is running on port: " + port+". Waiting for connection...");//and we now saying that it actially waits for a connection on a specific given port number.


            //Listening continuously for connection of any client.
            while(true){ //infinite loop
                Socket clientSocket = ss.accept(); // this client socket now will allow us to communicate with the client once the connection is established.
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);  // if the message was received on the server we pass it to our client handler class to operate on it further
                new Thread(clientHandler).start(); // thread allows to handle multiple clients INDEPENDENTLY instead of blockind and serving one by one.
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    //Getter
    public static synchronized HashMap<String, ClientHandler> getClients() {
        return clients;
    }

    public List<String> getBannedPhrases() {
        return bannedPhrases;
    }
    public static String getRules(){
        return rules;
    }

    public synchronized void addClient(String clientName, ClientHandler handler){
        clients.put(clientName, handler);
    }

    public synchronized void removeClient(String clientName){
        clients.remove(clientName);
    }



    public static void main(String[] args) {
        MyServer server = new MyServer(); // creating an instance of our class and then the loading configuration function is called from a constructor.
        server.start();
    }
}
