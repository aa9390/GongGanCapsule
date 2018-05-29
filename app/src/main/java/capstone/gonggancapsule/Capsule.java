package capstone.gonggancapsule;

public class Capsule {
    private double latitude;
    private double longitude;
    private String create_date;
    private String content;
    private String picture;

    @Override
    public String toString() {
        return "Capsule{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", create_date='" + create_date + '\'' +
                ", content='" + content + '\'' +
                ", picture='" + picture + '\'' +
                '}';
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
}
