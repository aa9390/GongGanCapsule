package capstone.gonggancapsule;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

public class WriteDiary extends AppCompatActivity {

    ImageView selectedPictureIv;

    int exifOrientation;
    int exifDegree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_diary);

        selectedPictureIv = (ImageView)findViewById(R.id.selectedPictureIv);

        // uri 받아오기
        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra("uri");
        String pictureFilePath = intent.getStringExtra("pictureFilePath");

        if (uri != null && pictureFilePath == null) {
            try {
                // 받아온 uri로 bitmap 변환!!
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                int nh = (int) (bitmap.getHeight() * (1024.0 / bitmap.getWidth()));
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 1024, nh, true);
                selectedPictureIv.setImageBitmap(scaled);

            } catch (Exception e) {
                Toast.makeText(this, "갤러리에서 사진 불러오기 오류", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else if (uri == null && pictureFilePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(pictureFilePath);
            ExifInterface exif = null;

            try {
                exif = new ExifInterface(pictureFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (exif != null) {
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                exifDegree = exifOrientationToDegrees(exifOrientation);
            } else {
                exifDegree = 0;
            }

            selectedPictureIv.setImageBitmap(rotate(bitmap, exifDegree));
        }
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
}
