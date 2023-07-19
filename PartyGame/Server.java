import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Server implements Runnable{
    private ArrayList<ConnectionHandler>  connectedClients;
    private ServerSocket ss;
    private boolean done;
    private ExecutorService pool;
    private Client host;


    public Server(){
        connectedClients = new ArrayList<>();
        done = false;
    }

    @Override
    public void run(){
        try{
            ss = new ServerSocket(4999);
            pool = Executors.newCachedThreadPool();
            while(!done){
                Socket client = ss.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connectedClients.add(handler);
                pool.execute(handler);
            }
        }catch(Exception e){
            shutdown();
        }
    }

    public void broadcast(String message){
        for(ConnectionHandler ch: connectedClients){
            if(ch != null){
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown(){
        try{
            done = true;
            pool.shutdown();
            if(!ss.isClosed()){
                ss.close();
            }
            for(ConnectionHandler ch: connectedClients){
                ch.shutdown();
            }
        }catch(IOException e){
            //cannot handle
        }
    }

    class ConnectionHandler implements Runnable{
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String name;
        private boolean isHost;
        private String lastmsg;
        private String invited;
        private String rps;
        private boolean ready;

        public ConnectionHandler(Socket client){
            this.client = client;
            this.isHost = connectedClients.size() == 0;
            this.ready = true;
        }
        @Override
        public void run(){
            try{
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                if(isHost){
                    out.println("You are the host!");
                }
                out.println("Please Enter you name:");//add check so 2 people cant have the same name
                name = in.readLine();
                
                System.out.println(name + " joined");
                broadcast(name + " joined the game!");
                String message;
                while((message  = in.readLine())!= null){
                    if(message.startsWith("/nick ")){
                        String[] messageSplit  = message.split(" ", 2);
                        if(messageSplit.length == 2){
                            broadcast(name + " renamed to " + messageSplit[1]);
                            System.out.println(name + " renamed to " + messageSplit[1]);
                            name = messageSplit[1];
                            out.println("Name has been changed sucessfully!");
                        }else{
                            out.println("no name was provided!");
                        }
                    }else if(message.startsWith("/quit")){
                        if(isHost && connectedClients.size() > 1){
                            connectedClients.get(1).isHost = true;
                            connectedClients.get(1).sendMessage("You are the new Host!");
                        }
                        broadcast(name + " left the chat!");
                        removeClient();
                        shutdown();
                       
                    }else if(message.startsWith("/rps ")){ // 
                        String[] messageSplit  = message.split(" ", 2);
                        for(ConnectionHandler ch : connectedClients){
                            if((ch.name).equals(messageSplit[1])){
                                ch.out.println(name + " has invited you to a game of rps type /accept to accpet.");
                                invited = ch.name;
                                rps(ch, false);
                                break;
                            }
                        }
                    }else if(message.startsWith("/accept")){
                        for(ConnectionHandler ch : connectedClients){
                            if((ch.invited).equals(name)){
                                broadcast(name + " has accpeted " + ch.name + "'s rps game!");
                                rps(ch, true);
                                break;
                            }
                        }
                    }else if(message.startsWith("/help")){ //need to update
                        help();
                    }else if(message.startsWith("/number")){
                        out.println("There is " + noPlayers() + " person(s) currently connected.");
                    }else if(message.startsWith("/players")){
                        connectedPlayers();
                    }else if(message.startsWith("/gethost")){
                        out.println("The host is currently " + getHost() + ".");
                    }else if(message.startsWith("/host")){
                        String[] messageSplit  = message.split(" ", 2);
                        setHost(messageSplit[1]);
                    }else if(message.startsWith("/last ")){
                        String[] messageSplit  = message.split(" ", 2);
                        out.println(messageSplit[1]+"'s last message was: " + getLastMsg(messageSplit[1]));
                    }else if(message.charAt(0) == '/'){
                        out.println("Invalid command type /help for list of commands");
                    }else{
                        broadcast(name + ": " + message);
                        lastmsg = message;
                    }
                }
            }catch(IOException e){
                shutdown();
            }
        } 

        public void rps(ConnectionHandler opponent, boolean challenger){
            ready = false;
            while(opponent.ready){
                try{
                    TimeUnit.SECONDS.sleep(1);
                }catch(Exception e){

                }
            }
            boolean chosing = true;
            String choice;
            while(chosing){
                try{
                out.println("Rock Paper or Scissors?");
                choice = in.readLine();
                if(choice.equalsIgnoreCase("rock") || choice.equalsIgnoreCase("paper") || choice.equalsIgnoreCase("scissors")){
                    chosing = false;
                    rps = choice;
                    break;
                }else{
                    out.print("That is not a valid input try again");
                }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            rpsCompare(opponent, challenger);
        }



        public void rpsCompare(ConnectionHandler opponent, boolean challenger){
            ready = true;
            out.println("waiting for opponent...");
            while(opponent.ready == false){
                try{
                    TimeUnit.SECONDS.sleep(1);
                }catch(Exception e){

                }
            }
            if(challenger){
                int winner = checkWinner(opponent);
                if(winner == 0){
                    broadcast("Both chose " + rps + " its a draw!");
                }else if(winner == 1){
                    broadcast(name + " chose " + rps + " and " + opponent.name + " chose " + opponent.rps + "!\n" + name + " wins!");
                }else if(winner == 2){
                    broadcast(name + " chose " + rps + " and " + opponent.name + " chose " + opponent.rps + "!\n" + opponent.name + " wins!");
                }else if(winner == 3){
                    broadcast("Error with rps...");
                }
                
            }
            reset();
        }

        public int checkWinner(ConnectionHandler opponent){
            if(rps.equalsIgnoreCase(opponent.rps)){
                return 0;
            }else if((rps.equalsIgnoreCase("rock") && (opponent.rps).equalsIgnoreCase("scissors")) || (rps.equalsIgnoreCase("paper") && (opponent.rps).equalsIgnoreCase("rock")) || (rps.equalsIgnoreCase("scissors") && (opponent.rps).equalsIgnoreCase("paper"))){
                return 1;
            }else if((rps.equalsIgnoreCase("scissors") && (opponent.rps).equalsIgnoreCase("rock")) || (rps.equalsIgnoreCase("rock") && (opponent.rps).equalsIgnoreCase("paper")) || (rps.equalsIgnoreCase("paper") && (opponent.rps).equalsIgnoreCase("scissors"))){
                return 2;
            }
            return 3;
        }

        public void reset(){
            invited = null;
            rps = null;
        }

        public void setHost(String newHostName){
            for(ConnectionHandler ch : connectedClients){
                if((ch.name).equals(newHostName)){
                    ch.setHost();
                    isHost = false;
                    out.println("Host changed sucessfully");
                    ch.sendMessage("You are now the host!");
                    return;
                }
            }
            out.println("Error!\nCheck name and try again!");
        }

        public void connectedPlayers(){
            out.println("Here are all the connected players:");
            for(ConnectionHandler ch : connectedClients){
                if(ch.name == name){
                    out.println(ch.name + " (you)");
                }else{
                    out.println(ch.name);
                }
            }
        }

        public void sendMessage(String message){
            out.println(message);
        }

        public int noPlayers(){
            int num = 0;
            for(ConnectionHandler ch: connectedClients){
                num++;
            }
            return num;
        } 

        public void help(){ // needs updating
            out.println("Hello and welcome to the help menu\nCommands:\n/nick \"Example name\"\nThis changes your name to what you specify.\nWARNING - speechmarks not required\n\n/quit\nLeaves the lobby and program\n\n/start\nStarts game\nThank you for reading!");
        }

        public void removeClient(){
            connectedClients.remove(this);
        }

        public void shutdown(){
            try{
                System.out.println(name + " left!");
                in.close();
                out.close();
                if(!client.isClosed()){
                    client.close();
                }
            }catch(IOException e){

            }
        }

        public void setHost(){
            isHost = true;
        }

        public String getHost(){
            for(ConnectionHandler ch : connectedClients){
                if(ch.isHost){
                    if(ch.name == name){
                        return "you";
                    }else{
                        return ch.name;
                    }
                }
            }
            return null;
        }
        
        public String getLastMsg(String username){
            for(ConnectionHandler ch: connectedClients){
                if(username.equals(ch.name)){
                    return ch.lastmsg;
                }
            }
            return "---NO MESSAGE---";
        }

    }

    public static void main(String[] args){
        Server s = new Server();
        s.run();
    }
}