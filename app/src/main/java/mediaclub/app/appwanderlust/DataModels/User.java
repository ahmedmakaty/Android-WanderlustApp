package mediaclub.app.appwanderlust.DataModels;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by Bloom on 31/1/2016.
 */
public class User implements Serializable {

    private String id;
    private String name;
    private String image;
    private String nationality;
    private String distance;
    private String nickname;

    public String getDistance() {
        return distance;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public User(){}

    public User(String name, String id,String image, String nat,String distance, String nickname){
        this.name = name;
        this.id = id;
        this.nationality = nat;
        this.image = image;
        this.distance = distance;
        this.nickname = nickname;
    }

}
