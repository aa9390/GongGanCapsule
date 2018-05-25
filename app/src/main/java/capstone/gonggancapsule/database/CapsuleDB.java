package capstone.gonggancapsule.database;



public class CapsuleDB {

    public static final String TABLE_NAME = "capsule";

    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_CREATEDATE = "create_date";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_PICTURE = "picture";

    private double latitude;
    private double longitude;
    private String create_date;
    private String content;
    private String picture;

    //create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_LATITUDE + " DOUBLE NOT NULL, "
                    + COLUMN_LONGITUDE + " DOUBLE NOT NULL, "
                    + COLUMN_CREATEDATE + " STRING, "
                    + COLUMN_CONTENT + " TEXT, "
                    + COLUMN_PICTURE + " STRING, "
                    + " PRIMARY KEY(" + COLUMN_LATITUDE + ", " + COLUMN_LONGITUDE + ")"
                    + ");";

    public CapsuleDB(){
    }

    public CapsuleDB(double latitude, double longitude,
                     String create_date, String content, String picture) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.create_date = create_date;
        this.content = content;
        this.picture = picture;
    }

    //get()
    public double getLatitude(){
        return latitude;
    }
    public double getLongitude(){
        return longitude;
    }
    public String getCreate_date(){
        return create_date;
    }
    public String getContent(){
        return content;
    }
    public String getPicture(){
        return picture;
    }

    //set()
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }
    public void setPicture(String picture) {
        this.picture = picture;
    }

    // -----------------index 추가한 DB------------------------
//    public static final String TABLE_NAME = "capsule";
//
//    public static final String COLUMN_INDEX = "index";
//    public static final String COLUMN_LATITUDE = "latitude";
//    public static final String COLUMN_LONGITUDE = "longitude";
//    public static final String COLUMN_CREATEDATE = "create_date";
//    public static final String COLUMN_CONTENT = "content";
//    public static final String COLUMN_PICTURE = "picture";
//
//    private int index;
//    private double latitude;
//    private double longitude;
//    private String create_date;
//    private String content;
//    private String picture;
//
//    //create table SQL query
//    public static final String CREATE_TABLE =
//            "CREATE TABLE " + TABLE_NAME + "("
//                    + COLUMN_LATITUDE + " INT NOT NULL AUTO_INCREMENT, "
//                    + COLUMN_LATITUDE + " DOUBLE NOT NULL, "
//                    + COLUMN_LONGITUDE + " DOUBLE NOT NULL, "
//                    + COLUMN_CREATEDATE + " STRING, "
//                    + COLUMN_CONTENT + " TEXT, "
//                    + COLUMN_PICTURE + " STRING, "
//                    + " PRIMARY KEY(" + COLUMN_INDEX + ")"
//                    + ");";
//
//    public CapsuleDB(){
//    }
//
//    public CapsuleDB(int index, double latitude, double longitude,
//                     String create_date, String content, String picture) {
//        this.index = index;
//        this.latitude = latitude;
//        this.longitude = longitude;
//        this.create_date = create_date;
//        this.content = content;
//        this.picture = picture;
//    }
//
//
//    //get()
//    public int getIndex() {return index;}
//    public double getLatitude(){
//        return latitude;
//    }
//    public double getLongitude(){
//        return longitude;
//    }
//    public String getCreate_date(){
//        return create_date;
//    }
//    public String getContent(){
//        return content;
//    }
//    public String getPicture(){
//        return picture;
//    }
//
//
//
//    //set()
//    public void setLatitude(double latitude) {
//        this.latitude = latitude;
//    }
//    public void setLongitude(double longitude) {
//        this.longitude = longitude;
//    }
//    public void setCreate_date(String create_date) {
//        this.create_date = create_date;
//    }
//    public void setPicture(String picture) {
//        this.picture = picture;
//    }

}
