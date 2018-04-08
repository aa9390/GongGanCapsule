package capstone.gonggancapsule;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        // 권한 받아오기
        PermissionListener permissionlistener = new PermissionListener() {

            // 권한을 모두 허용했을 경우
            @Override
            public void onPermissionGranted() {
                Toast.makeText( MainActivity.this, "반갑습니다.", Toast.LENGTH_SHORT ).show();
            }

            // 권한이 거부되었을 경우
            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText( MainActivity.this, "권한이 거부되었습니다.\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT ).show();
            }

        };

        TedPermission.with( this )
                .setPermissionListener( permissionlistener )
                .setRationaleTitle( "권한 알림" )
                .setRationaleMessage( "공간캡슐을 실행하기 위해서는 카메라, 위치, 저장소 권한이 필요합니다. 확인을 눌러주세요!" )
                .setDeniedTitle( "권한 거부" )
                .setDeniedMessage( "권한이 거부되었습니다. 확인 버튼을 누르시면 설정 창으로 이동합니다." )
                .setGotoSettingButtonText( "확인" )
                .setPermissions( Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION )
                .check();


        // 메인 진입을 확인하기 위한 임시 토스트 메시지
        Toast.makeText( this, "메인진입", Toast.LENGTH_SHORT ).show();

        // 현재 위치 받아오기
        Location location = getMyLocation();

        // 현재 위치를 확인하기 위한 임시 토스트 메시지
        String longt = String.valueOf( location.getLongitude() );
        String latt = String.valueOf( location.getLatitude() );
        Toast.makeText( getBaseContext(), longt, Toast.LENGTH_SHORT).show();
        Toast.makeText( getBaseContext(), latt, Toast.LENGTH_SHORT).show();

        // 반경 계산
//        setRange()
    }


    // 현재 위치 받아오기
    private Location getMyLocation() {
        Location currentLocation = null;

        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MInteger.REQUEST_CODE_LOCATION);
        }

        else {
            LocationManager locationManager = (LocationManager) this.getSystemService( Context.LOCATION_SERVICE);

            // Best Provider를 가져오기 위한 criteria 선언
            Criteria criteria = new Criteria();

            // 정확도
            criteria.setAccuracy(Criteria.NO_REQUIREMENT);
            // 전원 소비량
            criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
            // 고도, 높이 값을 얻어 올지를 결정
            criteria.setAltitudeRequired(false);
            // provider 기본 정보(방위, 방향)
            criteria.setBearingRequired(false);
            // 속도
            criteria.setSpeedRequired(false);
            // 위치 정보를 얻어 오는데 들어가는 금전적 비용
            criteria.setCostAllowed(true);

            String bestProvider = locationManager.getBestProvider(new Criteria(), true);

            currentLocation = locationManager.getLastKnownLocation( bestProvider );

            if(currentLocation!=null) {
                // 위도와 경도를 설정
                double longitude = currentLocation.getLongitude();
                double latitude = currentLocation.getLatitude();

                // Log 보기
                Log.d( "Main", "longtitude=" + longitude + ", latitude=" + latitude );

            }
            else
                Toast.makeText( getBaseContext(), "위치를 찾을 수 없습니다", Toast.LENGTH_SHORT ).show();
        }
        return currentLocation;
    }


}
