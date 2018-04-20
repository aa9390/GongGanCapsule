package capstone.gonggancapsule;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by User on 2018-04-16.
 */


public class CapsuleDB extends SQLiteOpenHelper{

    private Context context;

    public CapsuleDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super( context, name, factory, version );
    }

//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        StringBuffer sb = new StringBuffer();
//        sb.append("CREATE TABLE IF NOT EXISTS CAPSULE_TABLE ( ");
//        sb.append("index INTEGER PRIMARY KEY AUTOINCREMENT, ");
//        sb.append("latitude DOUBLE NOT NULL, ");
//        sb.append("longtitude DOUBLE NOT NULL, ");
//        sb.append("create_date DATETIME NOT NULL, ");
//        sb.append("content TEXT NOT NULL, ");
//        sb.append("picture BLOB NOT NULL); ");
//    }

    // 위도, 경도를 PRIMARY KEY로 지정
    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE IF NOT EXISTS CAPSULE_TABLE ( ");
        sb.append("index INTEGER AUTOINCREMENT, ");
        sb.append("latitude DOUBLE NOT NULL, ");
        sb.append("longitude DOUBLE NOT NULL, ");
        sb.append("create_date DATETIME NOT NULL, ");
        sb.append("content TEXT NOT NULL, ");
        sb.append("picture BLOB NOT NULL); ");
        sb.append("PRIMARY KEY(latitude, longitude); ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Toast.makeText(context, "Version 올라감", Toast.LENGTH_SHORT).show();
    }
}
