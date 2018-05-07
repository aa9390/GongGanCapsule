package capstone.gonggancapsule;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {
    boolean permissionCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_splash );


//        if (!permissionCheck) getPermission();

//        else {
            Handler handler = new Handler();
            handler.postDelayed( new Runnable() {

                @Override
                public void run() {
                    finish();
                }
            }, 2500 );

    }

    public void getPermission() {
        PermissionListener permissionlistener = new PermissionListener() {

            // 권한을 모두 허용했을 경우
            @Override
            public void onPermissionGranted() {
                permissionCheck = true;
                Toast.makeText( SplashActivity.this, "반갑습니다.", Toast.LENGTH_SHORT ).show();
            }

            // 권한이 거부되었을 경우
            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                permissionCheck = false;
                Toast.makeText( SplashActivity.this, "권한이 거부되었습니다.\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT ).show();
            }
        };

            TedPermission.with( this )
                    .setPermissionListener( permissionlistener )
                .setRationaleTitle( "권한 알림" )
                .setRationaleMessage( "공간캡슐을 실행하기 위해서는 카메라, 위치, 저장소 권한이 필요합니다. 확인을 눌러주세요!" )
                .setDeniedTitle( "권한 거부" )
                .setDeniedMessage( "권한이 거부되었습니다. 확인 버튼을 누르시면 설정 창으로 이동합니다." )
                .setGotoSettingButtonText( "확인" )
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .check();

        };

    }
