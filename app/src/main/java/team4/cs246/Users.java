package team4.cs246;

public class Users {

    //These have to correspond to the names in my database, just like JSON
    public String name;


    public Users(String name) {
        this.name = name;

    }

    public Users() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
