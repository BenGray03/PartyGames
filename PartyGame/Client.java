import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Client implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    
    @Override
    public void run(){
        try{
            client = new Socket("192.168.86.30", 4999);
            out  = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHadler = new InputHandler();
            Thread t = new Thread(inHadler);
            t.start();

            String inmessage;
            while((inmessage = in.readLine())!= null){
                System.out.println(inmessage);
            }
        }catch(IOException e){
            shutdown();
        }
    }

    public void shutdown(){
        done = true;
        try{
            in.close();
            out.close();
            if(!client.isClosed()){
                client.close();
            }
        }catch(IOException e){
            //
        }
    }

    class InputHandler implements Runnable{

        @Override
        public void run(){
            try{
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                while(!done){
                    String message = inputReader.readLine();
                    if(message.equals("/quit")){
                        out.println(message);
                        inputReader.close();
                        shutdown();
                    }else{
                        out.println(message);
                    }
                }
            }catch(IOException e){
                shutdown();
            }
        }
    }
    public static void main(String[] args){
        Client c = new Client();
        c.run();    
    }
    
}
