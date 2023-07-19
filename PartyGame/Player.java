public class Player{
    private String id;
    private String name;
    private int points;
    private boolean host;

    public Player(String n){
        name = n;
        points = 0;
        id = null;
        host = false;
    }

    public String getName(){
        return name;
    }

    public void setName(String n){
        name = n;
    }

    public int getPoints(){
        return points;
    }

    public void setPoints(int p){
        points = p;
    }

    public void resetPoints(){
        points = 0;
    }

    public String getId(){
        return id;
    }

    public void setId(String i){
        id = i;
    }

    public boolean getHost(){
        return host;
    }
    public void setHost(boolean h){
        host = h;
    }
}