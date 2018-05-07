package capstone.gonggancapsule;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.Point.OrientationMode;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// GLSurfaceView. Renderer가 생성될 때 호출되는 순서
// onSurfaceCreated() -> onSurfaceChanged() -> onDrawFrame()
public class MainActivity extends AppCompatActivity implements GLSurfaceView.Renderer {

    boolean permissionCheck = false;
    @BindView(R.id.database)
    ImageButton database;
    private LocationManager locationManager;
    private GLSurfaceView surfaceView;

    // hello ar의 코드를 이용하기 위해 필요한 코드
    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    private final PlaneRenderer planeRenderer = new PlaneRenderer();
    private final PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();

    // 현재는 앵커(Object)를 만들지 않았기 때문에 필요없는 코드.
    private final ObjectRenderer virtualObject = new ObjectRenderer();
    private final ObjectRenderer virtualObjectShadow = new ObjectRenderer();


    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private final float[] anchorMatrix = new float[16];

    // Anchors created from taps used for object placing.
    private final ArrayList<Anchor> anchors = new ArrayList<>();

    private TapHelper tapHelper;

    // Session은 필요 유무에 따라 삭제
    private Session session;
    private GestureDetector gestureDetector;
    private DisplayRotationHelper displayRotationHelper;

    // 메인화면 툴바, 작성 날짜를 위한 코드
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // 카메라, 갤러리 실행을 위한 코드
    private FloatingActionButton floatingBtn;
    private FloatingActionButton cameraBtn;
    private FloatingActionButton galleryBtn;

    private String pictureFilePath;
    private Uri pictureUri;
    boolean isOpen = false;
    Animation FabOpen, FabClose;

    public final static int CAMERA_REQUEST_CODE = 1;
    public final static int GALLERY_REQUEST_CODE = 2;

    // 위치 확인용
    private TextView longi;
    private TextView lati;


    private boolean installRequested;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        ButterKnife.bind( this );

        //위치 받아오기
        GPSTracker mGPS = new GPSTracker(this);

        //위치 받아오는지 확인하기 위한 임시코드
        TextView text = (TextView) findViewById(R.id.longi);
        TextView text2 = (TextView) findViewById(R.id.lati);

        if(mGPS.canGetLocation ){
            mGPS.getLocation();
            text.setText("Lat"+mGPS.getLatitude());
            text2.setText("Lon"+mGPS.getLongitude());
        }else{
            text.setText("Unabletofind");
            System.out.println("Unable");
        }

        //splash 화면 띄우기
        startActivity(new Intent(this, SplashActivity.class));

        // 카메라 뷰를 위한 surfaceview 선언
        surfaceView = findViewById( R.id.surfaceview );
        displayRotationHelper = new DisplayRotationHelper(/*context=*/ this );
        tapHelper = new TapHelper(/*context=*/ this);

        // 플로팅 버튼 id 가져오기, 클릭 리스너 선언
        floatingBtn = findViewById( R.id.floatingBtn );
        cameraBtn = findViewById( R.id.cameraBtn );
        galleryBtn = findViewById( R.id.galleryBtn );

        floatingBtn.setOnClickListener( clickListener );
        cameraBtn.setOnClickListener( clickListener );
        galleryBtn.setOnClickListener( clickListener );

        FabOpen = AnimationUtils.loadAnimation( this, R.anim.fab_open );
        FabClose = AnimationUtils.loadAnimation( this, R.anim.fab_close );

        longi = findViewById( R.id.longi );
        lati = findViewById( R.id.lati );


        // 메인 화면 초기화
        initView();

        // 카메라 플로팅 버튼을 클릭했을 때
        cameraBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
                if (cameraIntent.resolveActivity( getPackageManager() ) != null) {
                    File pictureFile = null;

                    try {
                        pictureFile = createImageFile(); //카메라로 찍은 사진 받아오기
                    } catch (IOException e) {
                        Toast.makeText( MainActivity.this, "카메라 실행 오류", Toast.LENGTH_SHORT ).show();
                    }

                    if (pictureFile != null) {
                        pictureUri = FileProvider.getUriForFile( getApplicationContext(), getPackageName(), pictureFile );
                        cameraIntent.putExtra( MediaStore.EXTRA_OUTPUT, pictureUri );
                        startActivityForResult( cameraIntent, CAMERA_REQUEST_CODE );
                    }
                }
            }
        } );

        // 갤러리 플로팅 버튼을 클릭했을 때
        galleryBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent( Intent.ACTION_GET_CONTENT );
                galleryIntent.setType( "image/*" );
                startActivityForResult( galleryIntent, GALLERY_REQUEST_CODE ); //갤러리앱 실행
            }
        } );

        // Renderer 설정
        surfaceView.setPreserveEGLContextOnPause( true );
        surfaceView.setEGLContextClientVersion( 2 );
        surfaceView.setEGLConfigChooser( 8, 8, 8, 8, 16, 0 ); // Alpha used for plane blending.
        surfaceView.setRenderer( this );
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat( "yyyyMMdd_HHmmss" ).format( new Date() );
        String pictureFileName = "GongGan_" + timeStamp;
        File storageDir = getExternalFilesDir( Environment.DIRECTORY_PICTURES );
        File image = File.createTempFile(
                pictureFileName, ".jpg", storageDir
        );
        pictureFilePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onPause() {
        super.onPause();

        Toast.makeText( this, "on Pause", Toast.LENGTH_SHORT ).show();

        surfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 권한을 다 받았으면 권한 받아오기를 실행하지 않음
        if (!permissionCheck) getPermission();
        else {
            Toast.makeText( this, "on Resume", Toast.LENGTH_SHORT ).show();
            session = new Session( this );
            session.resume();
            surfaceView.onResume();
            displayRotationHelper.onResume();
        }
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.floatingBtn:
                    if (!isOpen) {
                        cameraBtn.startAnimation( FabOpen );
                        galleryBtn.startAnimation( FabOpen );
                        isOpen = true;
                    } else {
                        cameraBtn.startAnimation( FabClose );
                        galleryBtn.startAnimation( FabClose );
                        isOpen = false;
                    }
                    break;

            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if (resultCode != RESULT_OK) {
            return;
        }

        Intent intent = new Intent( this, WriteDiary.class );

        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                // 카메라로 찍은 사진의 파일 경로를 WriteDiary 레이아웃으로 보내준다.
                intent.putExtra( "pictureFilePath", pictureFilePath );
                startActivity( intent );
                break;

            case GALLERY_REQUEST_CODE:
                Uri galleryUri = data.getData(); //선택된 이미지의 uri를 받아온다.

                // 선택된 이미지의 uri를 WriteDiary 레이아웃으로 보내준다.
                intent.putExtra( "uri", galleryUri );
                startActivity( intent );
                break;
        }
    }


    // 권한을 받기 위한 코드
    public void getPermission() {
        PermissionListener permissionlistener = new PermissionListener() {

            // 권한을 모두 허용했을 경우
            @Override
            public void onPermissionGranted() {
                permissionCheck = true;
                Toast.makeText( MainActivity.this, "반갑습니다.", Toast.LENGTH_SHORT ).show();
            }

            // 권한이 거부되었을 경우
            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                permissionCheck = false;
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
        toolbar = (Toolbar) findViewById( R.id.toolbar );
        drawerLayout = (DrawerLayout) findViewById( R.id.drawer_layout );
        navigationView = (NavigationView) findViewById( R.id.navigation_view );

        // 툴바 생성 및 세팅하는 부분
        setSupportActionBar( toolbar );
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator( R.drawable.open_save_data_btn );
        actionBar.setDisplayHomeAsUpEnabled( true );
        actionBar.setDisplayShowTitleEnabled( false );

        navigationView.setNavigationItemSelectedListener( new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                item.setChecked( true );
                drawerLayout.closeDrawers();
                return true;
            }
        } );

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

        switch (id) {
            case android.R.id.home:
                drawerLayout.openDrawer( GravityCompat.START );
                return true;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor( 0.1f, 0.1f, 0.1f, 1.0f );

        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            // Create the texture and pass it to ARCore session to be filled during update().
            backgroundRenderer.createOnGlThread(/*context=*/ this );
            planeRenderer.createOnGlThread(/*context=*/ this, "models/trigrid.png" );
            pointCloudRenderer.createOnGlThread(/*context=*/ this);

      virtualObject.createOnGlThread(/*context=*/ this, "models/andy.obj", "models/andy.png");
      virtualObject.setMaterialProperties(0.0f, 2.0f, 0.5f, 6.0f);
//
      virtualObjectShadow.createOnGlThread(
          /*context=*/ this, "models/andy_shadow.obj", "models/andy_shadow.png");
      virtualObjectShadow.setBlendMode( ObjectRenderer.BlendMode.Shadow);
      virtualObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f);
//
        } catch (IOException e) {
//            Log.e(TAG, "Failed to read an asset file", e);
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged( width, height );
        GLES20.glViewport( 0, 0, width, height );

    }

    // GLSurfaceView가 생성되고 나면
    // surface의 크기가 변경되지 않는 한 onDrawFrame이 반복 호출됨
    // GLSurfaceView.onPause()가 호출되면 GLSurfaceView.Renderer interface의 abstract method 들의 호출이 중단되고,
    // onResume()이 호출되면 onSurfaceCreated()부터 다시 호출이 된다.
    // 즉, surface가 다시 생성된다.
    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear( GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT );

        // save 버튼을 클릭하고 메인화면 오류없이 돌아가기
        if(session == null) {
            return;
        }

        displayRotationHelper.updateSessionIfNeeded( session );

        try {
            session.setCameraTextureName(backgroundRenderer.getTextureId());

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            Frame frame = session.update();
            Camera camera = frame.getCamera();

            // Handle taps. Handling only one tap per frame, as taps are usually low frequency
            // compared to frame rate.

            MotionEvent tap = tapHelper.poll();
            if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
                for (HitResult hit : frame.hitTest(tap)) {
                    // Check if any plane was hit, and if it was hit inside the plane polygon
                    Trackable trackable = hit.getTrackable();
                    // Creates an anchor if a plane or an oriented point was hit.
                    if ((trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))
                            || (trackable instanceof Point
                            && ((Point) trackable).getOrientationMode()
                            == OrientationMode.ESTIMATED_SURFACE_NORMAL)) {
                        // Hits are sorted by depth. Consider only closest hit on a plane or oriented point.
                        // Cap the number of objects created. This avoids overloading both the
                        // rendering system and ARCore.
                        if (anchors.size() >= 20) {
                            anchors.get(0).detach();
                            anchors.remove(0);
                        }
                        // Adding an Anchor tells ARCore that it should track this position in
                        // space. This anchor is created on the Plane to place the 3D model
                        // in the correct position relative both to the world and to the plane.
                        anchors.add(hit.createAnchor());
                        break;
                    }
                }
            }

            // Draw background.
            backgroundRenderer.draw(frame);

            // If not tracking, don't draw 3d objects.
            if (camera.getTrackingState() == TrackingState.PAUSED) {
                return;
            }

            // Get projection matrix.
            float[] projmtx = new float[16];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

            // Get camera matrix and draw.
            float[] viewmtx = new float[16];
            camera.getViewMatrix(viewmtx, 0);

            // Compute lighting from average intensity of the image.
            // The first three components are color scaling factors.
            // The last one is the average pixel intensity in gamma space.
            final float[] colorCorrectionRgba = new float[4];
//            frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

            // Visualize tracked points.
            PointCloud pointCloud = frame.acquirePointCloud();
            pointCloudRenderer.update(pointCloud);
            pointCloudRenderer.draw(viewmtx, projmtx);

            // Application is responsible for releasing the point cloud resources after
            // using it.
            pointCloud.release();

//            // Check if we detected at least one plane. If so, hide the loading message.
//            if (messageSnackbarHelper.isShowing()) {
//                for (Plane plane : session.getAllTrackables(Plane.class)) {
//                    if (plane.getType() == com.google.ar.core.Plane.Type.HORIZONTAL_UPWARD_FACING
//                            && plane.getTrackingState() == TrackingState.TRACKING) {
//                        messageSnackbarHelper.hide(this);
//                        break;
//                    }
//                }
//            }

            // Visualize planes.
            planeRenderer.drawPlanes(
                    session.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projmtx);

            // Visualize anchors created by touch.
            float scaleFactor = 1.0f;
            for (Anchor anchor : anchors) {
                if (anchor.getTrackingState() != TrackingState.TRACKING) {
                    continue;
                }
                // Get the current pose of an Anchor in world space. The Anchor pose is updated
                // during calls to session.update() as ARCore refines its estimate of the world.
                anchor.getPose().toMatrix(anchorMatrix, 0);

                // Update and draw the model and its shadow.
                virtualObject.updateModelMatrix(anchorMatrix, scaleFactor);
                virtualObjectShadow.updateModelMatrix(anchorMatrix, scaleFactor);
                virtualObject.draw(viewmtx, projmtx, colorCorrectionRgba);
                virtualObjectShadow.draw(viewmtx, projmtx, colorCorrectionRgba);
            }

        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
//            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }


    @OnClick(R.id.database)
    public void onViewClicked() {
        Intent intent = new Intent( this, DataBaseCheckActivity.class );
        startActivity( intent );
    }
}
