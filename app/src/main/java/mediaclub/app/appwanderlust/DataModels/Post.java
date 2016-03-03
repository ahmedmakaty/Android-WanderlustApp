package mediaclub.app.appwanderlust.DataModels;

import java.io.Serializable;

/**
 * Created by Bloom on 8/2/2016.
 */
public class Post implements Serializable{

    private String id;
    private String date;
    private String title;
    private String description;
    private String imageUrl;

    public Post(String id, String date, String title,String description,String url){
        this.id = id;
        this.date = date;
        this.title = title;
        this.description = description;
        this.imageUrl = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
