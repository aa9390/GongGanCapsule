package capstone.gonggancapsule;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.Session;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.yongbeam.y_photopicker.util.photopicker.PhotoPagerActivity;
import com.yongbeam.y_photopicker.util.photopicker.PhotoPickerActivity;
import com.yongbeam.y_photopicker.util.photopicker.utils.YPhotoPickerIntent;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    //    private LocationListener locationListener;
    private GLSurfaceView surfaceView;

    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    private final ObjectRenderer virtualObject = new ObjectRenderer();
    private final ObjectRenderer virtualObjectShadow = new ObjectRenderer();
    private final PlaneRenderer planeRenderer = new PlaneRenderer();
    private final PointCloudRenderer pointCloud = new PointCloudRenderer();

    private Session session;
    private GestureDetector gestureDetector;
    private DisplayRotationHelper displayRotationHelper;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // 카메라, 갤러리 실행을 위한 코드
    private FloatingActionButton cameraBtn;
    private FloatingActionButton galleryBtn;

    public final static int CAMERA_REQUEST_CODE = 1;
    public final static int GALLERY_REQUEST_CODE = 2;

    public static ArrayList<String> selectedPhotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        cameraBtn = (FloatingActionButton) findViewById(R.id.cameraBtn);
        galleryBtn = (FloatingActionButton) findViewById(R.id.galleryBtn);

        // 권한 받아오기
        getPermission();

        // 메인 화면 초기화
        initView();

        // 카메라 플로팅 버튼을 클릭했을 때
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 카메라 앱 실행
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

            }
        });

        // 갤러리 플로팅 버튼을 클릭했을 때
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YPhotoPickerIntent intent = new YPhotoPickerIntent(MainActivity.this);
                intent.setMaxSelectCount(1);    // 선택할 수 있는 이미지의 개수 지정
                intent.setShowCamera(false);    // 카메라 실행 버튼 표시 여부
                intent.setShowGif(false);       // gif 이미지도 포함하여 갤러리를 보여줄 것인지
                intent.setSelectCheckBox(true); // 사진 선택할 때 테두리 색 변하기
                intent.setMaxGrideItemCount(3); // 한줄에 몇개의 사진을 보여줄 것인지 설정
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GALLERY_REQUEST_CODE :
                List<String> photos = null;
                if (resultCode == RESULT_OK ) { //&& requestCode == GALLERY_REQUEST_CODE) {
                    if (data != null) {
                        photos = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                    }
                    if (photos != null) {
                        selectedPhotos.addAll(photos);
                    }

                    Intent startActivity = new Intent(this , PhotoPagerActivity.class);
                    startActivity.putStringArrayListExtra("photos" , selectedPhotos);
                    startActivity(startActivity);
                }
                break;

            case CAMERA_REQUEST_CODE :

                break;

        }


    }


    public void getPermission() {
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
    }

    public void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        // 툴바 생성 및 세팅하는 부분
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.open_save_data_btn);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled( false );

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                item.setChecked(true);
                drawerLayout.closeDrawers();
                return true;
            }
        });

        // 메인 진입을 확인하기 위한 임시 토스트 메시지
        Toast.makeText( this, "메인진입", Toast.LENGTH_SHORT ).show();

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.menu_main, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case android.R.id.home:
                drawerLayout.openDrawer( GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // 현재 위치 받아오기
    private Location getMyLocation() {
        Location currentLocation = null;

        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MInteger.REQUEST_CODE_LOCATION);
        } else {
            LocationManager locationManager = (LocationManager) this.getSystemService( Context.LOCATION_SERVICE );

            // Best Provider를 가져오기 위한 criteria 선언
            Criteria criteria = new Criteria();

            // 정확도
            criteria.setAccuracy( Criteria.NO_REQUIREMENT );
            // 전원 소비량
            criteria.setPowerRequirement( Criteria.NO_REQUIREMENT );
            // 고도, 높이 값을 얻어 올지를 결정
            criteria.setAltitudeRequired( false );
            // provider 기본 정보(방위, 방향)
            criteria.setBearingRequired( false );
            // 속도
            criteria.setSpeedRequired( false );
            // 위치 정보를 얻어 오는데 들어가는 금전적 비용
            criteria.setCostAllowed( true );

            String bestProvider = locationManager.getBestProvider( criteria, true );

            currentLocation = locationManager.getLastKnownLocation( bestProvider );

            if (currentLocation != null) {
                // 위도와 경도를 설정
                double longitude = currentLocation.getLongitude();
                double latitude = currentLocation.getLatitude();

                // Log 보기
                Log.d( "Main", "longtitude=" + longitude + ", latitude=" + latitude );

            } else
                Toast.makeText( getBaseContext(), "위치를 찾을 수 없습니다", Toast.LENGTH_SHORT ).show();
        }
        return currentLocation;
    }


    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

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
