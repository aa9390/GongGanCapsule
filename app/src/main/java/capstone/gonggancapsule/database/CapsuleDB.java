package capstone.gonggancapsule.database;



public class CapsuleDB {

    public static final String TABLE_NAME = "capsule";

    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_CREATEDATE = "create_date";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_PICTURE = "picture";
    public static final String COLUMN_ID = "id";

    private double latitude;
    private double longitude;
    private String create_date;
    private String content;
    private String picture;
    private int id;

    //create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_LATITUDE + " DOUBLE NOT NULL, "
                    + COLUMN_LONGITUDE + " DOUBLE NOT NULL, "
                    + COLUMN_CREATEDATE + " STRING, "
                    + COLUMN_CONTENT + " TEXT, "
                    + COLUMN_PICTURE + " STRING, "
                    + COLUMN_ID + " INTEGER AUTO_INCREMENT,"
                    + " PRIMARY KEY(" + COLUMN_LATITUDE + ", " + COLUMN_LONGITUDE + ")"
                    + ");";

    public CapsuleDB(){
    }

    public CapsuleDB(double latitude, double longitude,
                     String create_date, String content, String picture, int id) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.create_date = create_date;
        this.content = content;
        this.picture = picture;
        this.id = id;
    }


    //get()
    public int getId(){
        return id;
    }
    public double getLatitude(){
        return latitude;
    }
    public double getLongitude(){
        return longitude;
    }
    public String getTimestamp(){
        return create_date;
    }
    public String setContent(){
        return content;
    }
    public String getPicture(){
        return picture;
    }



    //set()
    public void setIndex(int id) {
        this.id = id;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public void setTimeStamp(String create_date) {
        this.create_date = create_date;
    }
    public void setPicture(String picture) {
        this.picture = picture;
    }

}
