package mediaclub.app.appwanderlust.DataModels;

/**
 * Created by Bloom on 31/1/2016.
 */
public class Message {

    private String message;
    private String date;
    private boolean type;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public Message(){}

    public Message(String message, String date, boolean type){
        this.type = type;
        this.message = message;
        this.date = date;
    }
}
