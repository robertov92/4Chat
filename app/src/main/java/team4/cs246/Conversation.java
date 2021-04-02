package team4.cs246;

/**
 * Helper class to retrieve conversation objects
 */
public class Conversation {
    public long timestamp;

    public Conversation(){}
    public long getTimestamp(){return timestamp;}
    public void setTimestamp(long timestamp){this.timestamp = timestamp;}
    public Conversation(long timestamp){
        this.timestamp = timestamp;
    }
}
