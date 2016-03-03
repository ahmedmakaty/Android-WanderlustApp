package mediaclub.app.appwanderlust.DataModels;

import java.io.Serializable;

/**
 * Created by Bloom on 31/1/2016.
 */
public class IndividualMessage implements Serializable {
    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    private int userId;
    private String lastMessage;

    public IndividualMessage(){}

    public IndividualMessage(int user, String message){
        this.userId = user;
        this.lastMessage = message;
    }
}
