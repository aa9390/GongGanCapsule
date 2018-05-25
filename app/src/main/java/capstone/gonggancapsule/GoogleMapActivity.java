package capstone.gonggancapsule;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);   // 서울역
    GoogleMap map;
    Geocoder geocoder = new Geocoder(this);
    List<Address> addresses;
    private Double latitude;
    private Double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // 작성화면으로 이동하기
        ImageButton checkBtn = findViewById(R.id.check_btn);
        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GoogleMapActivity.this, WriteDiary.class);
                Bundle bundle = new Bundle();
                if (addresses == null) {
                    setResult(RESULT_CANCELED, intent);
                    finish();
                } else {
                    bundle.putString("LOCATION", addresses.get(0).getAddressLine(0));
                    bundle.putDouble("LATITUDE", latitude);
                    bundle.putDouble("LONGITUDE", longitude);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap map) {
//        GPSTracker mGPS = new GPSTracker(this);
//        mGPS.getLocation();
        // 현재 위치로 이동
        map.setMyLocationEnabled(true);

        //위치가 확인 가능하면 지도를 현재 위치로 시작
        //위치가 확인 불가능하면 디폴트 위치에서 시작
//        if (location != null) {
//            LatLng currentLocation = new LatLng(mGPS.getLatitude(), mGPS.getLongitude());
//            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13));
//        }else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 12));
//        }

        // 맵 터치 이벤트
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                map.clear();            //마커가 하나만 뜨도록 누를 때마다 clear

                MarkerOptions mOptions = new MarkerOptions();

                mOptions.title("주소");    // 마커 이름
                latitude = point.latitude;
                longitude = point.longitude;
                // 마커의 스니펫(간단한 텍스트) 설정
                try{
                    addresses = geocoder.getFromLocation(latitude,longitude,1);
                }catch(IOException ioException) {
                    //지오코더 사용불가능일때
                }catch(IllegalArgumentException illegalArgumentException) {
                    //잘못된 GPS 를 가져왔을시
                }
                if (addresses == null || addresses.size() == 0) {
                    //주소 미발견시
                } else {
                    //주소가 존재하면
                    Address address = addresses.get(0);
                    mOptions.snippet(address.getAddressLine(0));
                }
                mOptions.position(new LatLng(latitude,longitude));
                // 마커(핀) 추가
                map.addMarker(mOptions);
            }
        });
    }
}