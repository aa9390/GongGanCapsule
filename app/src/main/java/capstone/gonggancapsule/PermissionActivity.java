package capstone.gonggancapsule;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class PermissionActivity extends AppCompatActivity {

    boolean perCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_permission );
        ButterKnife.bind( this );

        PermissionListener permissionlistener = new PermissionListener() {

            // 권한을 모두 허용했을 경우
            @Override
            public void onPermissionGranted() {
                Intent intent = new Intent( PermissionActivity.this, SplashActivity.class );
                startActivity( intent );
                finish();
            }

            // 권한이 거부되었을 경우
            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText( PermissionActivity.this, "권한이 거부되었습니다.\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT ).show();
            }
        };

        TedPermission.with( this )
                .setPermissionListener( permissionlistener )
                .setRationaleTitle( "권한 알림" )
                .setDeniedTitle( "권한 거부" )
                .setDeniedMessage( "권한이 거부되었습니다. 확인 버튼을 누르시면 설정 창으로 이동합니다." )
                .setGotoSettingButtonText( "확인" )
                .setPermissions( Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION )
                .check();

    }

    @OnClick(R.id.permisson_btn)
    public void onViewClicked() {
        getPermission();
    }

    // 권한 받아오기
    public void getPermission() {
        PermissionListener permissionlistener = new PermissionListener() {

            // 권한을 모두 허용했을 경우
            @Override
            public void onPermissionGranted() {
                Intent intent = new Intent( PermissionActivity.this, SplashActivity.class );
                startActivity( intent );
                finish();
            }

            // 권한이 거부되었을 경우
            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText( PermissionActivity.this, "권한이 거부되었습니다.\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT ).show();
            }
        };

        TedPermission.with( this )
                .setPermissionListener( permissionlistener )
                .setRationaleTitle( "권한 알림" )
                .setDeniedTitle( "권한 거부" )
                .setDeniedMessage( "권한이 거부되었습니다. 확인 버튼을 누르시면 설정 창으로 이동합니다." )
                .setGotoSettingButtonText( "확인" )
                .setPermissions( Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION )
                .check();
    }

}
