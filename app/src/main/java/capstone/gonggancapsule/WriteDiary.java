package capstone.gonggancapsule;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import java.util.List;

import capstone.gonggancapsule.database.DatabaseHelper;

public class WriteDiary extends AppCompatActivity {

    ImageView selectedPictureIv;
    ImageButton changeDateBtn;
    ImageButton changeLocationBtn;
    TextView dateTv;
    TextView locationTv;
    EditText writeContentEt;
    Button saveDiaryBtn;
    EditText title_editText;

    public String dateString;
    public String path;

    Geocoder geocoder = new Geocoder(this);
    List<Address> locationAddress;
    private Double latitude;
    private Double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_diary_2);

        // DB9
        final DatabaseHelper dbHelper = new DatabaseHelper(WriteDiary.this, "capsule", null, 3);

        selectedPictureIv = (ImageView) findViewById(R.id.selectedPictureIv);
        dateTv = (TextView) findViewById(R.id.dateTv);
        locationTv = (TextView) findViewById(R.id.locationTv);
        writeContentEt = (EditText) findViewById(R.id.writeContentEt);
        saveDiaryBtn = (Button) findViewById(R.id.saveDiaryBtn);
        changeDateBtn = (ImageButton) findViewById(R.id.changeDateBtn);
        changeLocationBtn = (ImageButton) findViewById(R.id.changeLocationBtn);
        title_editText = (EditText) findViewById(R.id.title_editText);

        Intent intent = getIntent();
        String galleryPath = intent.getStringExtra("galleryPath");
        String cameraPath = intent.getStringExtra("cameraPath");

        if (galleryPath != null && cameraPath == null) {
            setPicture(galleryPath);
        } else if (galleryPath == null && cameraPath != null) {
            setPicture(cameraPath);
        }

        setDate(); // 날짜 세팅
        setLocation(); // 위치 세팅

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
                if (writeContentEt.getLineCount() >= 9) { //8줄까지 쓸 수 있도록 설정
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
                String title = title_editText.getText().toString(); //제목
                String create_date = dateTv.getText().toString(); //작성 날짜
                String content = writeContentEt.getText().toString(); //내용
                dbHelper.insertDiary(latitude, longitude, create_date, content, path, title);

                if(MainActivity.activity2 != null) {
                    MainActivity activity2 = (MainActivity)MainActivity.activity2;
                    activity2.finish();
                }

                Intent intent = new Intent(WriteDiary.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    public void setPicture(String path) {
        try {
            int degree = getExifOrientation(path);
            Bitmap picture = BitmapFactory.decodeFile(path);
            selectedPictureIv.setImageBitmap(getRotatedBitmap(picture, degree));
            this.path = path;
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
                        dateString = String.format("%d년 %d월 %d일", year, month + 1, dayOfMonth);
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

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 M월 d일");
        return simpleDateFormat.format(date);
    }

    // 위치 설정
    public void setLocation(){
        GPSTracker currentGPS = new GPSTracker(this);
        currentGPS.getLocation();

        // 현재 위치 LocationTextView에 보여주기
        try{
            latitude = currentGPS.getLatitude();
            longitude = currentGPS.getLongitude();
            locationAddress = geocoder.getFromLocation(currentGPS.getLatitude(),currentGPS.getLongitude(),1);
        }catch(IOException ioException) {
            //지오코더 사용불가능일때
        }catch(IllegalArgumentException illegalArgumentException) {
            //잘못된 GPS 를 가져왔을시
        }
        if (locationAddress == null || locationAddress.size() == 0) {
            //주소 미발견시
            locationTv.setText("위치가 없습니다");
        } else {
            //주소가 존재하면
            Address address = locationAddress.get(0);
            locationTv.setText(address.getAddressLine(0));
        }
        locationTv.setBackgroundResource(R.drawable.textview_border);

        // 지도로 이동하는 버튼
        ImageButton mapBtn = findViewById(R.id.changeLocationBtn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( WriteDiary.this , GoogleMapActivity.class );
                startActivityForResult(intent,0);
            }
        });
    }

    private int getExifOrientation(String path) {
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            if(orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90: return 90;
                    case ExifInterface.ORIENTATION_ROTATE_180: return 180;
                    case ExifInterface.ORIENTATION_ROTATE_270: return 270;
                }
            }
        }
        return 0;
    }

    private Bitmap getRotatedBitmap(Bitmap bitmap, int degree) {
        if(degree != 0 && bitmap != null) {
            Matrix matrix = new Matrix();
            matrix.setRotate(degree, (float)bitmap.getWidth()/2, (float)bitmap.getHeight()/2);

            try {
                Bitmap tmpBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                if(bitmap != tmpBitmap) {
                    bitmap.recycle();
                    bitmap = tmpBitmap;
                }
            }catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                locationTv.setText(bundle.getString("LOCATION"));
                latitude = bundle.getDouble("LATITUDE");
                longitude = bundle.getDouble("LONGITUDE");
            } else {   // RESULT_CANCEL
                setLocation();
            }
        }
    }
}