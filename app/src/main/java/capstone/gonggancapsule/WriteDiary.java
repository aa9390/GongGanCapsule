package capstone.gonggancapsule;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

    public String dateString;
    public Uri galleryCaptureUri;
    public Uri cameraCaptureUri;
    public String path;

    int exifOrientation;
    int exifDegree;

    Geocoder geocoder = new Geocoder(this);
    List<Address> locationAddress;
    private Double latitude;
    private Double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_diary);

        // DB
        final DatabaseHelper dbHelper = new DatabaseHelper(WriteDiary.this, "capsule", null, 1);

        selectedPictureIv = (ImageView) findViewById(R.id.selectedPictureIv);
        dateTv = (TextView) findViewById(R.id.dateTv);
        locationTv = (TextView) findViewById(R.id.locationTv);
        writeContentEt = (EditText) findViewById(R.id.writeContentEt);
        saveDiaryBtn = (Button) findViewById(R.id.saveDiaryBtn);
        changeDateBtn = (ImageButton) findViewById(R.id.changeDateBtn);
        changeLocationBtn = (ImageButton) findViewById(R.id.changeLocationBtn);

        Intent intent = getIntent();
        galleryCaptureUri = intent.getParcelableExtra("galleryCaptureUri");
        cameraCaptureUri = intent.getParcelableExtra("cameraCaptureUri");

        if(galleryCaptureUri != null && cameraCaptureUri == null) {
            setGalleryPicture(galleryCaptureUri);
        } else if(galleryCaptureUri == null && cameraCaptureUri != null) {
            setCameraPicture(cameraCaptureUri);
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
//
//                Double latitude = 0.0;
//                Double longitude = 0.0;


                //DB 저장 테스트용
//                Double latitude = Math.random() * 100;
//                Double longitude = Math.random() * 100;

                if(gpsTracker.canGetLocation ){
                    gpsTracker.getLocation();

                    latitude = gpsTracker.getLatitude();
                    longitude = gpsTracker.getLongitude();
                }
//                Double latitude = location.getLatitude();
//                Double longitude = location.getLongitude();
                String create_date = dateTv.getText().toString(); //작성 날짜
                String content = writeContentEt.getText().toString(); //내용
                dbHelper.insertDiary(latitude, longitude, create_date, content, path);

                Intent intent = new Intent(WriteDiary.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    public void setGalleryPicture(Uri galleryCaptureUri) {
        try {
            Bitmap picture = MediaStore.Images.Media.getBitmap(getContentResolver(), galleryCaptureUri);
            selectedPictureIv.setImageBitmap(picture);
            path = galleryCaptureUri.getPath();
        } catch (Exception e) {
            Toast.makeText(WriteDiary.this, "오류 : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // 원래 코드
    public void setCameraPicture(Uri cameraCaptureUri) {
        try {
            Bitmap picture = MediaStore.Images.Media.getBitmap(getContentResolver(), cameraCaptureUri);
            selectedPictureIv.setImageBitmap(rotate(picture));
            path = cameraCaptureUri.getPath();

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

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 M월 dd일");
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

    private Bitmap getRotatedBitmap(Bitmap bitmap, int degree) {
        if(degree != 0 && bitmap != null) {
            Matrix matrix = new Matrix();
            matrix.setRotate(degree, (float)bitmap.getWidth() / 2, (float)bitmap.getHeight() / 2);

            try {
                Bitmap tmpBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, true);

                if(bitmap != tmpBitmap) {
                    bitmap.recycle();
                    bitmap = tmpBitmap;
                }
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

//    private int exifOrientationToDegrees(int exifOrientation) {
//        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
//            return 90;
//        } else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
//            return 180;
//        } else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
//            return 270;
//        }
//        return 0;
//    }

//    public Bitmap rotate(Bitmap bitmap, int degrees) {
//        if(degrees != 0 && bitmap != null) {
//            Matrix m = new Matrix();
//            m.setRotate(degrees, (float)bitmap.getWidth() / 2, (float)bitmap.getHeight() / 2);
//
//            try {
//                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
//                if(bitmap != converted) {
//                    bitmap.recycle();
//                    bitmap = converted;
//                }
//            } catch (OutOfMemoryError e) {
//                Toast.makeText(WriteDiary.this, "오류 : " + e.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        }
//        return  bitmap;
//    }

    // 무조건 90도 회전만
    private Bitmap rotate(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(), bitmap.getHeight(), matrix, true);
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