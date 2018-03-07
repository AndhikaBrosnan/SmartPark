package alif.parking.qrscanner.models;

/**
 * Created by brad on 2017/01/27.
 */

public class Users {
    private String user;
    private String email;
    private String photUrl;
    private String Uid;
    private String platNumb;

    public Users() {
    }

    public Users(String user, String email, String photUrl, String uid, String platNumb) {
        this.user = user;
        this.email = email;
        this.photUrl = photUrl;
        Uid = uid;
        this.platNumb = platNumb;
    }

    public Users(String user, String email, String photUrl, String uid) {
        this.user = user;
        this.email = email;
        this.photUrl = photUrl;
        Uid = uid;
    }

    public void setPlatNumb(String platNumb) {
        this.platNumb = platNumb;
    }

    public String getPlatNumb() {
        return platNumb;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotUrl() {
        return photUrl;
    }

    public void setPhotUrl(String photUrl) {
        this.photUrl = photUrl;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }
}
