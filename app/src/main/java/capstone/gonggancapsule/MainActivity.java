package capstone.gonggancapsule;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import capstone.gonggancapsule.database.DatabaseHelper;
import uk.co.appoly.arcorelocation.LocationMarker;
import uk.co.appoly.arcorelocation.LocationScene;
import uk.co.appoly.arcorelocation.rendering.AnnotationRenderer;
import uk.co.appoly.arcorelocation.rendering.ImageRenderer;
import uk.co.appoly.arcorelocation.utils.Utils2D;

// GLSurfaceView. Renderer가 생성될 때 호출되는 순서
// onSurfaceCreated() -> onSurfaceChanged() -> onDrawFrame()
public class MainActivity extends AppCompatActivity implements GLSurfaceView.Renderer {

    // Database 관련 코드
    // Database 임시 확인을 위한 버튼
    @BindView(R.id.database)
    ImageButton database;
    // Database Helper 선언
    DatabaseHelper dbHelper = new DatabaseHelper(this, "capsule", null, 1);

    // 카메라 프리뷰를 위한 surfaceView 선언
    private GLSurfaceView surfaceView;

    // hello ar의 코드를 이용하기 위해 필요한 코드
    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    private final PlaneRenderer planeRenderer = new PlaneRenderer();
    private final PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();

    // 앵커 관련 코드
//    private final ObjectRenderer virtualObject = new ObjectRenderer();
//    private final ObjectRenderer virtualObjectShadow = new ObjectRenderer();
    private final float[] anchorMatrix = new float[16];
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

    // 작성 날짜를 위한 코드
    private String[] mDatesTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    boolean isOpen = false;
    Animation FabOpen, FabClose;

    // 카메라, 갤러리 실행을 위한 코드
    private FloatingActionButton floatingBtn;
    private FloatingActionButton cameraBtn;
    private FloatingActionButton galleryBtn;
    public final static int CAMERA_REQUEST_CODE = 1;
    public final static int GALLERY_REQUEST_CODE = 2;
    private Uri mImageCaptureUri;
    public String absolutePath;

    private boolean installRequested;
    boolean permissionCheck = false;

    LocationScene locationScene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        ButterKnife.bind( this );

        // 메인 화면 초기화
        initView();

        GPSTracker mGPS = new GPSTracker(this);

        //위치 받아오는지 확인하기 위한 임시코드
        TextView text = findViewById(R.id.longi);
        TextView text2 = findViewById(R.id.lati);

        if(mGPS.canGetLocation ){
            mGPS.getLocation();
            text.setText("Lat"+mGPS.getLatitude());
            text2.setText("Lon"+mGPS.getLongitude());

        }else{
            text.setText("Unabletofind");
            System.out.println("Unable");
        }

        Exception exception = null;
        String message = null;
        try {
            session = new Session(/* context= */ this);
        } catch (UnavailableArcoreNotInstalledException e) {
            message = "Please install ARCore";
            exception = e;
        } catch (UnavailableApkTooOldException e) {
            message = "Please update ARCore";
            exception = e;
        } catch (UnavailableSdkTooOldException e) {
            message = "Please update this app";
            exception = e;
        } catch (Exception e) {
            message = "This device does not support AR";
            exception = e;
        }

        if (message != null) {
            return;
        }

        // Create default config and check if supported.
        Config config = new Config(session);
        if (!session.isSupported(config)) {
        }
        session.configure(config);

        locationScene = new LocationScene( this, this, session );

        LocationMarker test1 = new LocationMarker(
                127.091294, 37.628213, new AnnotationRenderer( "중앙도서관" ) );
        LocationMarker test2 = new LocationMarker(
                127.093161, 37.625952, new AnnotationRenderer( "서울여대 CU" ) );
        LocationMarker test3 = new LocationMarker(
                127.090576, 37.629215,  new ImageRenderer( "icon_capsule.png" ) );
        LocationMarker test4 = new LocationMarker(
                127.093703, 37.625757,   new ImageRenderer( "icon_capsule.png" ) );

        locationScene.mLocationMarkers.add( test1 );
        locationScene.mLocationMarkers.add( test2 );
        locationScene.mLocationMarkers.add( test3 );
        locationScene.mLocationMarkers.add( test4 );

        // Renderer 설정
        surfaceView.setPreserveEGLContextOnPause( true );
        surfaceView.setEGLContextClientVersion( 2 );
        surfaceView.setEGLConfigChooser( 8, 8, 8, 8, 16, 0 ); // Alpha used for plane blending.
        surfaceView.setRenderer( this );
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Toast.makeText( this, "on Resume", Toast.LENGTH_SHORT ).show();

        if(locationScene!=null)
            locationScene.resume();

        if(session !=null) {
            try {
                session.resume();
            } catch (CameraNotAvailableException e) {
                e.printStackTrace();
            }
        }
        surfaceView.onResume();
        displayRotationHelper.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        if(locationScene != null)
            locationScene.pause();
        if(displayRotationHelper!=null)
            displayRotationHelper.onPause();
        surfaceView.onPause();
        if (session != null) {
            session.pause();
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        mDrawerList.setItemChecked(position, true);
        setTitle(mDatesTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
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

        if (resultCode != RESULT_OK) return;

        Intent goWriteIntent = new Intent(MainActivity.this, WriteDiary.class);

        switch (requestCode) {
            case GALLERY_REQUEST_CODE :
                mImageCaptureUri = data.getData();
                goWriteIntent.putExtra("mImageCaptureUri", mImageCaptureUri);
                break;

            case CAMERA_REQUEST_CODE :
                goWriteIntent.putExtra("mImageCaptureUri", mImageCaptureUri);
                String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/GONGGANCAPSULE/" + System.currentTimeMillis() + ".jpg";
                try {
                    Bitmap photo = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageCaptureUri);
                    savePicture(photo, filePath);
                    absolutePath = filePath;
                } catch (Exception e) {

                }

                File f = new File(mImageCaptureUri.getPath());
                if(f.exists()) {
                    f.delete();
                }

                break;
        }
        startActivity(goWriteIntent);

    }

    private void savePicture(Bitmap bitmap, String filePath) {
        String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/GONGGANCAPSULE";
        File directory_GONGGANCAPSULE = new File(dirPath);
        if(!directory_GONGGANCAPSULE.exists())
            directory_GONGGANCAPSULE.mkdir();

        //File copyFile = new File(filePath);
        BufferedOutputStream out = null;

        try {
            directory_GONGGANCAPSULE.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(directory_GONGGANCAPSULE));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    FileProvider.getUriForFile(getBaseContext(), "capstone.gonggancapsule", directory_GONGGANCAPSULE)));

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initView() {
        // 카메라 뷰를 위한 surfaceview 선언
        surfaceView = findViewById( R.id.surfaceview );
        displayRotationHelper = new DisplayRotationHelper(/*context=*/ this );
        tapHelper = new TapHelper(/*context=*/ this);

        surfaceView.setOnTouchListener(tapHelper);

        // 플로팅 버튼 id 가져오기, 클릭 리스너 선언
        floatingBtn = findViewById( R.id.floatingBtn );
        cameraBtn = findViewById( R.id.cameraBtn );
        galleryBtn = findViewById( R.id.galleryBtn );

        floatingBtn.setOnClickListener( clickListener );
        cameraBtn.setOnClickListener( clickListener );
        galleryBtn.setOnClickListener( clickListener );

        FabOpen = AnimationUtils.loadAnimation( this, R.anim.fab_open );
        FabClose = AnimationUtils.loadAnimation( this, R.anim.fab_close );

        // 작성 날짜

        mDatesTitles = getResources().getStringArray(R.array.create_date_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);


        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDatesTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());


        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            // drawer가 닫혔을 때, 호출된다.
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            // drawer가 열렸을 때, 호출된다.
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        // DrawerListener로 drawer toggle을 설정.
        mDrawerLayout.setDrawerListener(mDrawerToggle);

//        if (savedInstanceState == null) {
//            selectItem(0);
//        }

        toolbar = (Toolbar) findViewById( R.id.toolbar );
        drawerLayout = (DrawerLayout) findViewById( R.id.drawer_layout );
        //navigationView = (NavigationView) findViewById( R.id.navigation_view );

        // 툴바 생성 및 세팅하는 부분
        setSupportActionBar( toolbar );
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator( R.drawable.open_save_data_btn );
        actionBar.setDisplayHomeAsUpEnabled( true );
        actionBar.setDisplayShowTitleEnabled( false );

        // 메인 진입을 확인하기 위한 임시 토스트 메시지
        Toast.makeText( this, "메인진입", Toast.LENGTH_SHORT ).show();

        // 카메라 플로팅 버튼을 클릭했을 때
        cameraBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                //임시로 사용할 파일의 경로 생성
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String url = "GongGanCapsule_" + timeStamp + ".jpg";

                mImageCaptureUri = FileProvider.getUriForFile(getBaseContext(), "capstone.gonggancapsule.fileprovider",
                        new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/GONGGANCAPSULE", url));

                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        } );

        // 갤러리 플로팅 버튼을 클릭했을 때
        galleryBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }
        } );
    }

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

        try {
            backgroundRenderer.createOnGlThread(/*context=*/ this );
//            planeRenderer.createOnGlThread(/*context=*/ this, "models/trigrid.png" );
//            pointCloudRenderer.createOnGlThread(/*context=*/ this);
        } catch (IOException e) {
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged( width, height );
        GLES20.glViewport( 0, 0, width, height );

    }

    //    // GLSurfaceView가 생성되고 나면
    //    // surface의 크기가 변경되지 않는 한 onDrawFrame이 반복 호출됨
    //    // GLSurfaceView.onPause()가 호출되면 GLSurfaceView.Renderer interface의 abstract method 들의 호출이 중단되고,
    //    // onResume()이 호출되면 onSurfaceCreated()부터 다시 호출이 된다.
    //    // 즉, surface가 다시 생성된다.
    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null) {
            return;
        }
        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        try {
            session.setCameraTextureName(backgroundRenderer.getTextureId());

            // Obtain the current frame from ARSession. When the configuration is set to
            // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
            // camera framerate.
            Frame frame = session.update();
            Camera camera = frame.getCamera();

            MotionEvent tap = tapHelper.poll();
            if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
//                Log.i(TAG, "HITTEST: Got a tap and tracking");
                Utils2D.handleTap(this, locationScene, frame, tap);
            }
            // Handle taps. Handling only one tap per frame, as taps are usually low frequency
            // compared to frame rate.

            // Draw background.
            backgroundRenderer.draw(frame);
            locationScene.draw(frame);

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
            frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

//            // Visualize tracked points.
//            PointCloud pointCloud = frame.acquirePointCloud();
//            pointCloudRenderer.update(pointCloud);
//            pointCloudRenderer.draw(viewmtx, projmtx);

//            // Application is responsible for releasing the point cloud resources after
//            // using it.
//            pointCloud.release();

//            // Visualize planes.
//            planeRenderer.drawPlanes(
//                    session.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projmtx);

            // Visualize anchors created by touch.
            float scaleFactor = 1.0f;
            for (Anchor anchor : anchors) {
                if (anchor.getTrackingState() != TrackingState.TRACKING) {
                    continue;
                }
                // Get the current pose of an Anchor in world space. The Anchor pose is updated
                // during calls to session.update() as ARCore refines its estimate of the world.
                anchor.getPose().toMatrix(anchorMatrix, 0);
            }

        } catch (Throwable t) {
        }
    }

    @OnClick(R.id.database)
    public void onViewClicked() {
        Intent intent = new Intent( this, DataBaseCheckActivity.class );
        startActivity( intent );
    }
}