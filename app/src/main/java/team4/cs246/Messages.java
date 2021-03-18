package team4.cs246;

public class Messages {
    String message;
    long time;

    public Messages(String message, long time) {
        this.message = message;
        this.time = time;
    }

    public Messages() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
