//4/27

package capstone.gonggancapsule.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "capsule";

    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // Creating Tables
    // DB를 새로 생성할 때 호출되는 함수
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create capsule table
        db.execSQL(CapsuleDB.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + CapsuleDB.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public void insertDiary(Double latitude, Double longitude, String create_date, String content, String picture) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(CapsuleDB.COLUMN_LATITUDE, latitude);
        values.put(CapsuleDB.COLUMN_LONGITUDE, longitude);
        values.put(CapsuleDB.COLUMN_CREATEDATE, create_date);
        values.put(CapsuleDB.COLUMN_CONTENT, content);
        values.put(CapsuleDB.COLUMN_PICTURE, picture);

        // DB에 입력한 값으로 행 추가
        db.execSQL("INSERT INTO capsule VALUES(" + latitude +  ", " + longitude + ", '" + create_date + "', '" + content + "', '" + picture + "');" );
        db.close();
    }



    public String getDiary() {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        String result = " ";    //db확인하려고 쓰는 result입니다.

        Cursor cursor = db.rawQuery("SELECT * FROM capsule", null);
        while (cursor.moveToNext()) {
            result += " 위도: "
                    + cursor.getDouble(0)
                    + " 경도: "
                    + cursor.getDouble(1)
                    + " 날짜: "
                    + cursor.getString(2)
                    + " 내용: "
                    + cursor.getString(3)
                    + " 사진: "
                    + cursor.getString(4)
                    + "\n";
        }
        return result;
    }

    // ----------------INDEX 추가한 getDiary() -----------------------
//    public String getDiary() {
//        // get readable database as we are not inserting anything
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        String result = " ";    //db확인하려고 쓰는 result입니다.
//
//        Cursor cursor = db.rawQuery("SELECT * FROM capsule", null);
//        while (cursor.moveToNext()) {
//            result += " 인덱스: "
//                    + cursor.getInt(0)
//                    + " 위도: "
//                    + cursor.getDouble(1)
//                    + " 경도: "
//                    + cursor.getDouble(2)
//                    + " 날짜: "
//                    + cursor.getString(3)
//                    + " 내용: "
//                    + cursor.getString(4)
//                    + " 사진: "
//                    + cursor.getString(5)
//                    + "\n";
//        }
//        return result;
//    }



//    public double getLongitude(int indexNum) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        String Query = "SELECT " + CapsuleDB.COLUMN_LONGITUDE +" FROM " + CapsuleDB.TABLE_NAME + " WHERE " + CapsuleDB.COLUMN_INDEX + " = " + indexNum;
//
//        Cursor cursor = db.rawQuery(Query, null);
//        double longitude = cursor.getDouble( 0 );
//        cursor.close();
//
//        return longitude;
//    }
//
//    public double getLatitude(int indexNum) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        String Query = "SELECT " + CapsuleDB.COLUMN_LATITUDE +" FROM " + CapsuleDB.TABLE_NAME + " WHERE " + CapsuleDB.COLUMN_INDEX + " = " + indexNum;
//
//        Cursor cursor = db.rawQuery(Query, null);
//        double latitude = cursor.getDouble( 0 );
//        cursor.close();
//
//        return latitude;
//    }

    public int getDiaryCount() {
        String countQuery = "SELECT  * FROM " + CapsuleDB.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    // *** 작성날짜 *** //
    public ArrayList<String> getDateList() {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<String> dateList = new ArrayList<>();
        String result = null;

        Cursor cursor = db.rawQuery("SELECT create_date FROM " + CapsuleDB.TABLE_NAME, null);
        while (cursor.moveToNext()) {
            result = cursor.getString(0);
            dateList.add(result);
        }

        return dateList;
    }

}