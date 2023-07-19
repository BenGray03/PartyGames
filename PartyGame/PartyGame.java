import java.util.Scanner;
import java.util.ArrayList;
import java.net.*;
import java.io.*;

public class PartyGame{

    public static void main(String [] args){
        setUp();
    }

    public static void setUp(){ //works for now maybe add error msg
        boolean signedIn = false;

        Scanner input = new Scanner(System.in);
        while(!signedIn){
        System.out.println("Please input a name:");
        String name = input.nextLine();
            if(name.length() > 3){
                signedIn = true;
                System.out.println("Accepted name");
                Player user = new Player(name);
                createMenu(user);
            }else{
                System.out.println("name too short try again:");
            }
        }
    }


    public static void createMenu(Player user){
        Scanner input = new Scanner(System.in);
        boolean chosing = true;
        while(chosing){
            System.out.println("Welcome " + user.getName());
            System.out.println("1. Rap Battle\n2. TBA\n3. EXIT\n(type the number)");
            int choice = input.nextInt();
            switch(choice){
                case 1:
                    Rap(user);
                    break;
                case 2:
                    break;
                case 3:
                    chosing = false;
                    break;
                default:
                    System.out.println("not a valid input");
                    break;
            }
        }
    }

    public static void Rap(Player user){
        Scanner input = new Scanner(System.in);
        boolean chosing  = true;
        while(chosing){
            System.out.println("Host or Join?");
            String hoj = input.nextLine();
            if(hoj.equalsIgnoreCase("host")){
                chosing = false;
                HostServerThread host = new HostServerThread();
                Thread server = new Thread(()-> {
                    try{
                        host.createServer();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                });
                server.start();
                try{
                    user.setHost(true);
                    Socket s = new Socket("localhost", 4999);
                    Client c = new Client(user, s);
                    c.sendName();
                    c.startChat();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }else if(hoj.equalsIgnoreCase("join")){
                chosing = false;
                try{
                    Socket s = new Socket("localhost", 4999);
                    Client c = new Client(user, s);
                    System.out.println("sucessfully connected");
                    c.sendName();
                    c.startChat();
                }catch(IOException e){
                    e.printStackTrace();
                }
                
            }

            
        }
    }

    public static void rapHost(HostServerThread host){

    }

}