package capstone.gonggancapsule;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import capstone.gonggancapsule.database.DatabaseHelper;

public class WriteDiary extends AppCompatActivity {

    ImageView selectedPictureIv;
    ImageButton changeDateBtn;
    TextView dateTv;
    EditText writeContentEt;
    Button saveDiaryBtn;

    public String dateString;
    public Uri mImageCaptureUri;
    int exifOrientation;
    int exifDegree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_diary);

        // DB
        final DatabaseHelper dbHelper = new DatabaseHelper(WriteDiary.this, "capsule", null, 1);

        selectedPictureIv = (ImageView)findViewById(R.id.selectedPictureIv);
        dateTv = (TextView)findViewById(R.id.dateTv);
        writeContentEt = (EditText)findViewById(R.id.writeContentEt);
        saveDiaryBtn = (Button)findViewById(R.id.saveDiaryBtn);
        changeDateBtn = (ImageButton)findViewById(R.id.changeDateBtn);

        Intent intent = getIntent();
        mImageCaptureUri = intent.getParcelableExtra("mImageCaptureUri");

        setPicture(mImageCaptureUri); // 불러온 이미지 세팅
        setDate(); // 날짜 세팅

//        // uri 받아오기
//        Intent intent = getIntent();
//        Uri uri = intent.getParcelableExtra("uri");
//        pictureFilePath = intent.getStringExtra("pictureFilePath");
//
//        if (uri != null && pictureFilePath == null) {
//            try {
//                // 받아온 uri로 bitmap 변환!!
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//                int nh = (int) (bitmap.getHeight() * (1024.0 / bitmap.getWidth()));
//                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 1024, nh, true);
//                selectedPictureIv.setImageBitmap(scaled);
//
//            } catch (Exception e) {
//                Toast.makeText(this, "갤러리에서 사진 불러오기 오류", Toast.LENGTH_LONG).show();
//                e.printStackTrace();
//            }
//        } else if (uri == null && pictureFilePath != null) {
//            Bitmap bitmap = BitmapFactory.decodeFile(pictureFilePath);
//            ExifInterface exif = null;
//
//            try {
//                exif = new ExifInterface(pictureFilePath);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            if (exif != null) {
//                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//                exifDegree = exifOrientationToDegrees(exifOrientation);
//            } else {
//                exifDegree = 0;
//            }
//
//            selectedPictureIv.setImageBitmap(rotate(bitmap, exifDegree));
//        }

        // Content EditText 라인 수 제한하기 위함(입력가능한 최대 라인수)
        writeContentEt.addTextChangedListener(new TextWatcher() {
            String previousString = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousString = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(writeContentEt.getLineCount() >= 9) { //8줄까지 쓸 수 있도록 설정
                    writeContentEt.setText(previousString);
                    writeContentEt.setSelection(writeContentEt.length());
                }
            }
        });

        // save 버튼을 누르면 DB에 데이터 저장 (INSERT)
        saveDiaryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GPSTracker gpsTracker = new GPSTracker(WriteDiary.this);

//                Double latitude = 0.0;
//                Double longitude = 0.0;

//                DB 저장 테스트용
                Double latitude = Math.random() * 100;
                Double longitude = Math.random() * 100;

//                if(gpsTracker.canGetLocation ){
//                    gpsTracker.getLocation();
//
//                    latitude = gpsTracker.getLatitude();
//                    longitude = gpsTracker.getLongitude();
//                }
                String create_date = dateTv.getText().toString(); //작성 날짜
                String content = writeContentEt.getText().toString(); //내용
//                String picture = mImageCaptureUri.toString(); // 사진 URI
                String picture = mImageCaptureUri.getPath(); //사진 경로

                dbHelper.insertDiary(latitude, longitude, create_date, content, picture);

                Intent intent = new Intent(WriteDiary.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    public void setPicture(Uri mImageCaptureUri) {
        ExifInterface exif = null;
        try {
            Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageCaptureUri);

            if (exif != null) {
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                exifDegree = exifOrientationToDegrees(exifOrientation);
            } else {
                exifDegree = 0;
            }

            selectedPictureIv.setImageBitmap(rotate(photo, exifDegree));

            //selectedPictureIv.setImageBitmap(photo);
        } catch (Exception e) {
            Toast.makeText(WriteDiary.this, "오류 : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // 날짜 변경할 수 있는 DatePickerDialog
    public void setDate() {
        final Calendar cal = Calendar.getInstance();
        String nowDateString = getDateString();
        dateTv.setText(nowDateString);
        dateTv.setBackgroundResource(R.drawable.textview_border);

        changeDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(WriteDiary.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dateString = String.format("%d년 %d월 %d일", year, month+1, dayOfMonth);
                        dateTv.setText(dateString);
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
                dialog.getDatePicker().setMaxDate(new Date().getTime());
                dialog.show();
            }
        });
    }

    // 현재 날짜 세팅
    public String getDateString() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일"); //출력 포맷
        return simpleDateFormat.format(date);
    }

    // 사진을 띄울 때 회전되지 않게 보여주기 위함(exifOrientationToDegrees, rotate 함수)
    // 상수를 받아 각도로 변환시켜주는 메소드
    private int exifOrientationToDegrees(int exifOrientation) {
        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    // 비트맵을 각도대로 회전시켜 결과 반환
    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public String getPathFromUri(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex("_data"));
        cursor.close();

        return path;
    }
}
