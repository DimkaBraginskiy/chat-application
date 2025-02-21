import java.util.List;
import java.util.Map;

public
class MessageHandler {//not implementing runnable because the class is only for processing messages
    private Map<String, ClientHandler> clients;//clients already were put in ClientHandler so we can use a normal Map here. Also we can store additional data e.g. status of user etc....
    private List<String> bannedPhrases;
    private boolean isMessageSent = false;
    public MessageHandler(Map<String, ClientHandler> clients, List<String> bannedPhrases){
        this.clients = clients;
        this.bannedPhrases = bannedPhrases;
    }


    public void handleMessage(String message, String senderName, ClientHandler sender){
        if(containsBannedPhrase(message)){
            sender.getWriter().println("Message contains banned phrase(s) how dare you???? :<");
            return; //exiting method - not proceeding with any kind of checking.
        }


        //Automatic sending to all without any command so we will not need to write @all anymore.
        if(!message.startsWith("@") && !message.startsWith("/")){
            sendToAll(message, senderName, sender);
        }
        if(message.startsWith("@")){

            if(message.startsWith("@!")){
                String content = message.substring(2).trim();
                String[] parts = content.split(" ", 2);
                String[] exclClients = parts[0].split(",");
                String messageContent = "";
                if(parts.length > 1){
                    messageContent = parts[1];
                }else{
                    messageContent = "";
                }

                sendToAllExcept(exclClients, messageContent, sender);
            }
            else if(message.startsWith("@")){
                String content = message.substring(1);
                String[] parts = content.split(" ", 2);
                String[] clientsToSend = parts[0].split(",");
                String messageContent;
                if(parts.length > 1){
                    messageContent = parts[1];
                }else{
                    messageContent = "";
                }
                sendToSpecificClients(clientsToSend, messageContent, sender);
            }
        }else if(message.startsWith("/quit")){
            sender.getWriter().println("Bye-Bye.........");
            sender.disconnectClient();
        }else if(message.startsWith("/bp")){
            showBannedPhrases(sender);
        }else if(message.startsWith("/lu")){
            String userList = getUsersList(senderName);
            sender.getWriter().println(userList);
        }else if(message.startsWith("/rules")){
            sender.getWriter().println(MyServer.getRules());
        }

    }

    private boolean containsBannedPhrase(String message){
        String[] parts = new String[2];
        if(message.startsWith("@")){ // if message starts with @ we will split it to our clients name will not be checker for banned phrases.
            parts = message.split(" ");
            for(String phrase : bannedPhrases){
                if(parts[1].toLowerCase().contains(phrase.toLowerCase())){
                    return true;
                }
            }
        }else{
            for(String phrase : bannedPhrases){
                if(message.toLowerCase().contains(phrase.toLowerCase())){
                    return true;
                }
            }
        }

        return false;
    }

    private void sendToAll(String message, String senderName, ClientHandler sender){
        isMessageSent = false;

        synchronized (MyServer.getClients()){
            for(ClientHandler client : MyServer.getClients().values()){
                if(client != sender){
                    client.getWriter().println("private message from "+senderName+":\n> "+message);
                    isMessageSent = true;
                }
            }
            if(isMessageSent){
                sender.getWriter().println("Message sent successfully");
            }
        }
    }

    private void sendToAllExcept(
            String[] exclClients, String message, ClientHandler sender){
        isMessageSent = false;
        synchronized (MyServer.getClients()){
            for(ClientHandler client : MyServer.getClients().values()){ //values = clientHandler objects/instances. Foreach loop over instances.
                boolean isExcluded = false;
                for(String excluded : exclClients){//foreach loop over all Strings representing names (nicknames)
                    if(client.getClientName().equals(excluded.trim())){
                        isExcluded = true;
                        break;
                    }
                }
                if(!isExcluded && client != sender){
                    client.getWriter().println("Private message from "+sender.getClientName()+":\n>"+message);
                    isMessageSent = true;
                }
            }
            if(isMessageSent){
                sender.getWriter().println("The message was sent to everyone excluding specified user(s).");
            }
        }
    }

    private void sendToSpecificClients(String[] clientsToSend, String message, ClientHandler sender){
        isMessageSent = false;

        synchronized (MyServer.getClients()){
            for(String clientName : clientsToSend){
                for(ClientHandler client : MyServer.getClients().values()){
                    if(client.getClientName().equals(clientName.trim()) && client != sender){
                        client.getWriter().println("Private message from "+sender.getClientName()+":\n>"+message);
                        isMessageSent = true;
                        break;
                    }
                }
            }
            if (isMessageSent) {
                sender.getWriter().println("The message was sent to specified user(s).");
            } else {
                sender.getWriter().println("No such users found or they are the sender.");
            }
        }
    }

    private void showBannedPhrases(ClientHandler sender) {
        StringBuilder bannedList = new StringBuilder("Banned Phrases:\n");
        for(String phrase : bannedPhrases) {
            bannedList.append(phrase).append(", ");
        }
        sender.getWriter().println(bannedList.toString());
    }

    public synchronized String getUsersList(String requestingClName){
        StringBuilder userList = new StringBuilder("Users Online:\n");
        synchronized (clients){//StringBuilder does not guarantee synchronization
                for(String name : clients.keySet()){
                    if(name.equals(requestingClName)){
                        userList.append(name+" (You)\n");
                    }else{
                        userList.append(name+"\n");
                    }
                }
        }
        return userList.toString();
    }
}
