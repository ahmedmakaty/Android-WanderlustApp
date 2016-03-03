package mediaclub.app.appwanderlust.DataModels;

/**
 * Created by Bloom on 17/2/2016.
 */
public class ChatItem {

    private String id;
    private String nickname;
    private String image;
    private String last_message;
    private String counter;
    private String date;

    public ChatItem(String id, String nickname, String image, String last_message, String counter, String date){
        this.id = id;
        this.nickname = nickname;
        this.image = image;
        this.last_message = last_message;
        this.counter = counter;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public String getCounter() {
        return counter;
    }

    public void setCounter(String counter) {
        this.counter = counter;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
