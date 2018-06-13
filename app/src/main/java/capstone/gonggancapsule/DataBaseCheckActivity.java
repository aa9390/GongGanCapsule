package capstone.gonggancapsule;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

import capstone.gonggancapsule.database.DatabaseHelper;

public class DataBaseCheckActivity extends AppCompatActivity {

    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_check);

        final DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext(), "capsule", null, 2);

        // 테이블에 있는 모든 데이터 출력
        final TextView result = findViewById(R.id.result);

        final EditText etLatitude = findViewById(R.id.latitude);
        final EditText etLongitude = findViewById(R.id.longitude);
        final TextView etCreateDate = findViewById(R.id.create_date);
        final EditText etContent = findViewById(R.id.content);
        final EditText etPicture = findViewById(R.id.picture);

        // 날짜는 현재 날짜로 고정
        // 현재 시간 구하기
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        // 출력될 포맷 설정
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
        etCreateDate.setText(simpleDateFormat.format(date));

        // DB에 데이터 추가
        Button insert = findViewById(R.id.insert);
        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double latitude = Double.parseDouble(etLatitude.getText().toString());
                Double longitude = Double.parseDouble(etLongitude.getText().toString());
                String create_date = etCreateDate.getText().toString();
                String content = etContent.getText().toString();
                String picture = etPicture.getText().toString();

                dbHelper.insertDiary(latitude, longitude, create_date, content, picture, "제목");

                result.setText(dbHelper.getDiary());
            }
        });


        // DB에 있는 데이터 조회
        Button select = findViewById(R.id.select);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //result.setText(dbHelper.getDiary());

                // getAllDiary()함수 테스트를 위한 코드
                StringBuffer resultAll = new StringBuffer();
                ArrayList<Capsule> capsuleList = dbHelper.getAllDiary();
                for (int i = 0; i < capsuleList.size(); i++) {
                    resultAll.append(capsuleList.get(i).toString());
                }
                result.setText(resultAll.toString());
            }
        });

    }

}
