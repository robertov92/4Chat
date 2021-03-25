package team4.cs246;

public class Users {

    //These have to correspond to the names in my database, just like JSON
    public String name;
    public String status;

    public Users() {
    }

    public Users(String name, String status) {
        this.name = name;
        this.status = status;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
