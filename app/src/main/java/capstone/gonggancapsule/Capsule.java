package capstone.gonggancapsule;

public class Capsule {
    private int capsule_id;
    private double latitude;
    private double longitude;
    private String create_date;
    private String content;
    private String picture;
    private String title;

    @Override
    public String toString() {
        return "Capsule{" +
                "capsule_id=" + capsule_id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", create_date='" + create_date + '\'' +
                ", content='" + content + '\'' +
                ", picture='" + picture + '\'' +
                '}';
    }

    public int getCapsule_id() {
        return capsule_id;
    }

    public void setCapsule_id(int capsule_id) {
        this.capsule_id = capsule_id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
