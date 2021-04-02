package team4.cs246;

/**
 * Helper class to retrieve Messages objects
 */
public class Messages {
    private String message, type;
    private long time;
    private String from;

    public Messages(String from) {
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public Messages(String message, long time, String from, String type) {
        this.message = message;
        this.time = time;
        this.from = from;
        this.type = type;
    }

    public Messages() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
