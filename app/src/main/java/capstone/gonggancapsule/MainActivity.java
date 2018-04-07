package capstone.gonggancapsule;

import android.Manifest;
import android.content.pm.PackageManager;
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

        // LocationManager, LocationListener 설정
        settingGPS();

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

    // LocationManager, LocationListener 설정
    private void settingGPS() {
        locationManager = (LocationManager) this.getSystemService( this.LOCATION_SERVICE );
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

    }


    // 현재 위치 받아오기
    private Location getMyLocation() {
        Location currentLocation = null;

        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MInteger.REQUEST_CODE_LOCATION);
        }

        // 각각 NETWORT PROVIDER, GPS PROVIDER 에서 정보 얻어오기
        else {
            locationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 10, 0, locationListener );
            locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 10, 0, locationListener );

            // 실내, 실외 모두 작동되게 하려면 아래 문장을 if문으로 돌릴것
            String locationProvider = LocationManager.NETWORK_PROVIDER;
//            String locationProvider = LocationManager.GPS_PROVIDER;


            currentLocation = locationManager.getLastKnownLocation( locationProvider );

            if(currentLocation!=null) {

                // 위도와 경도를 설정
                double longitude = currentLocation.getLongitude();
                double latitude = currentLocation.getLatitude();

                // Log 보기
                Log.d( "Main", "longtitude=" + longitude + ", latitude=" + latitude );

            }
        }
        return currentLocation;
    }


}
