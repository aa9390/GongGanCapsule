package capstone.gonggancapsule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import capstone.gonggancapsule.database.DatabaseHelper;
import uk.co.appoly.arcorelocation.LocationMarker;
import uk.co.appoly.arcorelocation.LocationScene;
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper;

// ARCore 1.2Ver로 바뀜으로 인해 openGL코드 삭제
public class MainActivity extends AppCompatActivity {
    Activity activity = this;
    Context context = this;

    private boolean installRequested;

    // Database 임시 확인을 위한 버튼
    @BindView(R.id.database)
    ImageButton database;

    // Database Helper 선언
    DatabaseHelper dbHelper = new DatabaseHelper( this, "capsule", null, 1 );

    // ARSceneform 관련 코드 (라이브러리 사용)
    private ArSceneView arSceneView;
    private ViewRenderable diaryLayoutRenderable;
    private ViewRenderable diaryLayoutRenderable2;
    private ArrayList<ViewRenderable> diaryRenderableList = new ArrayList<ViewRenderable>(  );
    //    private ArrayList<Layout> diaryLayoutList = new ArrayList<Layout>(  );
    private ArrayList<CompletableFuture<ViewRenderable>> diaryLayoutList = new ArrayList<CompletableFuture<ViewRenderable>>(  );
    private LocationScene locationScene;

    // 메인화면 툴바, 작성 날짜, FAB를 위한 코드
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    boolean isOpen = false;
    Animation FabOpen, FabClose;

    // 작성 날짜를 위한 코드
    private String[] mDatesTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private TextView totalTv;

    // 카메라, 갤러리 실행을 위한 코드
    private FloatingActionButton floatingBtn;
    private FloatingActionButton cameraBtn;
    private FloatingActionButton galleryBtn;
    public final static int CAMERA_REQUEST_CODE = 1;
    public final static int GALLERY_REQUEST_CODE = 2;
    private Uri mImageCaptureUri;
    public String absolutePath;

    // 캡슐 객체 관련 코드
    ArrayList<Capsule> capsuleList;
    final ArrayList<Capsule> capsuleRangeList = new ArrayList<>(  );
    Capsule capsule;

    Glide glide;
    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        ButterKnife.bind( this );

        arSceneView = findViewById(R.id.ar_scene_view);
        capsuleList = dbHelper.getAllDiary();

        // 메인 화면 초기화
        initView();

        // *** 주영 5/28 추가코드
        GPSTracker mGPS = new GPSTracker( this );
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int range = 50;
                switch (position) {
                    case 0 : range = 50; break;
                    case 1 : range = 100; break;
                    case 2 : range = 200; break;
                    case 3 : range = 300; break;
                }

                if (capsuleList != null) {
                    if(capsuleRangeList != null) {
                        capsuleRangeList.clear();
                    }

                    for (int i=0; i<capsuleList.size(); i++) {
                        capsule = capsuleList.get(i);
                        if(getDistance(mGPS.getLatitude(), mGPS.getLongitude(), capsule.getLatitude(), capsule.getLongitude()) < 1000) {
                            capsuleRangeList.add(capsule);
                        }
                    }
                }

                // 반경에 해당하는 일기를 확인하기 위한 테스트 코드
                StringBuffer sb = new StringBuffer();
                for (int i=0; i<capsuleRangeList.size(); i++) {
                    sb.append(capsuleRangeList.get(i).toString());
                }
                Toast.makeText(MainActivity.this, sb, Toast.LENGTH_LONG).show();

                // -------------------------------------------
                // 일기장 보여줄 레이아웃 임시 설정

                CompletableFuture<ViewRenderable> diaryLayout =
                        ViewRenderable.builder()
                                .setView(getApplicationContext(), R.layout.activity_diary_test)
                                .build();

                CompletableFuture<ViewRenderable> diaryLayout2 =
                        ViewRenderable.builder()
                                .setView(getApplicationContext(), R.layout.activity_diary_test)
                                .build();

                CompletableFuture<ViewRenderable> diaryLayout3 =
                        ViewRenderable.builder()
                                .setView(getApplicationContext(), R.layout.activity_diary_test)
                                .build();

//                ViewRenderable.builder()
//                        .setView(context, R.layout.activity_diary_test)
//                        .build()
//                        .thenAccept(renderable -> diaryLayoutRenderable = renderable);

                diaryLayoutList.add( diaryLayout );
                diaryLayoutList.add( diaryLayout2 );
                diaryLayoutList.add( diaryLayout3 );


                CompletableFuture.allOf(
                        diaryLayout, diaryLayout2, diaryLayout3)
                        .handle(
                                (notUsed, throwable) -> {
                                    if (throwable != null) {
                                        DemoUtils.displayError(
                                                getApplicationContext(), "Unable to load renderables", throwable);
                                        return null;
                                    }

                                    try {

                                        // 캡슐 리스트가 널이 아니면 렌더러를 불러온다.
                                        // 렌더러 동적 생성 필요.
                                        if(capsuleRangeList.size()!=0) {
                                            for (int i = 0; i < capsuleRangeList.size(); i++)
                                                diaryRenderableList.add( diaryLayoutList.get( i ).get() );
//                                            diaryLayoutRenderable = diaryLayout.get();
//                                            diaryLayoutRenderable2 = diaryLayout2.get();
                                        }

                                    } catch (InterruptedException | ExecutionException ex) {
                                        DemoUtils.displayError(
                                                getApplicationContext(), "Unable to load renderables", ex);
                                    }

                                    return null;
                                });

                arSceneView
                        .getScene()
                        .setOnUpdateListener(
                                (FrameTime frameTime) -> {
                                    if (locationScene == null) {
                                        locationScene = new LocationScene(context, activity, arSceneView);

                                        Toast.makeText( getApplicationContext(), "불러올 데이터가" + capsuleRangeList.size() + "개 있습니다.", Toast.LENGTH_SHORT ).show();

//                                        ArrayList<LocationMarker> locationMarker = new ArrayList<LocationMarker>();
                                        LocationMarker [] locationMarker = new LocationMarker[10];

//                                        if(capsuleRangeList.size()<=3) {
                                            if(capsuleRangeList.size()==2) {

                                            // DB에서 값 받아와 출력
                                            locationMarker[0] = new LocationMarker( capsuleRangeList.get( 0 ).getLongitude(), capsuleRangeList.get( 0 ).getLatitude(), getDiaryView(0) );

                                            locationMarker[0].setRenderEvent( node -> {
                                                View eView = diaryRenderableList.get( 0 ).getView();
                                                TextView content = eView.findViewById( R.id.showContentTv );
                                                ImageView pic = eView.findViewById( R.id.showPictureIv );
                                                pic.setImageURI( Uri.parse( capsuleRangeList.get( 0 ).getPicture() ) );
                                                content.setText( capsuleRangeList.get( 0 ).getContent() );
                                                TextView distanceTextView = eView.findViewById( R.id.distance );
                                                distanceTextView.setText( node.getDistance() + "M" );
                                            } );

                                            locationScene.mLocationMarkers.add( locationMarker[0] );

                                                // DB에서 값 받아와 출력
                                                locationMarker[1] = new LocationMarker( capsuleRangeList.get( 1 ).getLongitude(), capsuleRangeList.get( 1 ).getLatitude(), getDiaryView(1) );

                                                locationMarker[1].setRenderEvent( node -> {
                                                    View eView = diaryRenderableList.get( 1 ).getView();
                                                    TextView content = eView.findViewById( R.id.showContentTv );
                                                    ImageView pic = eView.findViewById( R.id.showPictureIv );
//                                                    pic.setImageURI( Uri.parse( capsuleRangeList.get( 1 ).getPicture() ) );
                                                    glide.with(context).load(Uri.parse( capsuleRangeList.get( 1 ).getPicture())).into(pic);
                                                    content.setText( capsuleRangeList.get( 1 ).getContent() );
                                                    TextView distanceTextView = eView.findViewById( R.id.distance );
                                                    distanceTextView.setText( node.getDistance() + "M" );
                                                } );

                                                locationScene.mLocationMarkers.add( locationMarker[1] );

//                                            // DB에서 값 받아와 출력
//                                            locationMarker[0] = new LocationMarker( capsuleRangeList.get( 0 ).getLongitude(), capsuleRangeList.get( 0 ).getLatitude(), getDiaryView(0) );
//                                            LocationMarker test = new LocationMarker(
//                                                    capsuleRangeList.get( 0 ).getLongitude(), capsuleRangeList.get( 0 ).getLatitude(), getDiaryView(0) );
//
//                                            test.setRenderEvent( node -> {
//                                                View eView = diaryRenderableList.get( 0 ).getView();
//                                                TextView content = eView.findViewById( R.id.showContentTv );
//                                                ImageView pic = eView.findViewById( R.id.showPictureIv );
//                                                pic.setImageResource( R.drawable.icon_capsule );
//                                                content.setText( capsuleRangeList.get( 0 ).getContent() );
//                                                TextView distanceTextView = eView.findViewById( R.id.distance );
//                                                distanceTextView.setText( node.getDistance() + "M" );
//                                            } );
//
//                                            locationScene.mLocationMarkers.add( test );



//                                            LocationMarker test1 = new LocationMarker(
//                                                    capsuleRangeList.get( 1 ).getLongitude(), capsuleRangeList.get( 1 ).getLatitude(), getDiaryView(1) );
//
//                                            test1.setRenderEvent( node -> {
//                                                View eView = diaryRenderableList.get( 1 ).getView();
//                                                TextView content = eView.findViewById( R.id.showContentTv );
//                                                ImageView pic = eView.findViewById( R.id.showPictureIv );
//                                                pic.setImageResource( R.drawable.icon_capsule );
//                                                content.setText( capsuleRangeList.get( 1 ).getContent() );
//                                                TextView distanceTextView = eView.findViewById( R.id.distance );
//                                                distanceTextView.setText( node.getDistance() + "M" );
//                                            } );
//
//                                            locationScene.mLocationMarkers.add( test1 );
//
//                                            LocationMarker test2 = new LocationMarker(
//                                                    capsuleRangeList.get( 2 ).getLongitude(), capsuleRangeList.get( 2 ).getLatitude(), getDiaryView(2) );
//
//                                            test2.setRenderEvent( node -> {
//                                                View eView = diaryRenderableList.get( 2 ).getView();
//                                                TextView content = eView.findViewById( R.id.showContentTv );
//                                                ImageView pic = eView.findViewById( R.id.showPictureIv );
//                                                pic.setImageResource( R.drawable.icon_capsule );
//                                                content.setText( capsuleRangeList.get( 2 ).getContent() );
//                                                TextView distanceTextView = eView.findViewById( R.id.distance );
//                                                distanceTextView.setText( node.getDistance() + "M" );
//                                            } );
////
//                                            locationScene.mLocationMarkers.add( test2 );
                                        }

                                        Toast.makeText( activity, "레이아웃", Toast.LENGTH_SHORT ).show();
                                    }

                                    Frame frame = arSceneView.getArFrame();

                                    if (frame == null) {
                                        return;
                                    }

                                    if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                                        return;
                                    }

                                    if (locationScene != null) {
                                        locationScene.processFrame(frame);
                                    }

                                });

                ARLocationPermissionHelper.requestPermission(MainActivity.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //위치 받아오는지 확인하기 위한 임시코드
//        TextView text = findViewById( R.id.longi );
//        TextView text2 = findViewById( R.id.lati );
//
//        if (mGPS.canGetLocation) {
//            mGPS.getLocation();
//            text.setText( "Lat" + mGPS.getLatitude() );
//            text2.setText( "Lon" + mGPS.getLongitude() );
//        } else {
//            text.setText( "Unabletofind" );
//            System.out.println( "Unable" );
//        }
    }

    private Node getDiaryView() {
        Node base = new Node();
        base.setRenderable(diaryLayoutRenderable);

        return base;
    }

    private Node getDiaryView2() {
        Node base = new Node();
        base.setRenderable(diaryLayoutRenderable2);

        return base;
    }

    private Node getDiaryView(int i) {
        Node base = new Node();
        base.setRenderable(diaryRenderableList.get( i ));

        return base;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (locationScene != null) {
            locationScene.resume();
        }

        if (arSceneView.getSession() == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                Session session = DemoUtils.createArSession(this, installRequested);
                if (session == null) {
                    installRequested = ARLocationPermissionHelper.hasPermission(this);
                    return;
                } else {
                    arSceneView.setupSession(session);
                }
            } catch (UnavailableException e) {
                DemoUtils.handleSessionException(this, e);
            }
        }

        try {
            arSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            DemoUtils.displayError(this, "Unable to get camera", ex);
            finish();
            return;
        }

        if (arSceneView.getSession() != null) {
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (locationScene != null) {
            locationScene.pause();
        }

        arSceneView.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        arSceneView.destroy();
    }



    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem( position );
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Bundle args = new Bundle();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked( position, true );
        setTitle( mDatesTitles[position] );
        mDrawerLayout.closeDrawer( mDrawerList );
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

        Intent goWriteIntent = new Intent( MainActivity.this, WriteDiary.class );

        switch (requestCode) {
            case GALLERY_REQUEST_CODE:
                mImageCaptureUri = data.getData();
                goWriteIntent.putExtra( "galleryCaptureUri", mImageCaptureUri );
                break;

            case CAMERA_REQUEST_CODE:
                //goWriteIntent.putExtra("mImageCaptureUri", mImageCaptureUri);
                goWriteIntent.putExtra( "cameraCaptureUri", mImageCaptureUri );
                String picturePath = mImageCaptureUri.getPath();

                try {
                    Bitmap photo = MediaStore.Images.Media.getBitmap( getContentResolver(), mImageCaptureUri );
                    savePicture( photo, picturePath ); //저장

                } catch (Exception e) {
                    Toast.makeText( MainActivity.this, e.getMessage(), Toast.LENGTH_LONG ).show();
                }

                File f = new File( mImageCaptureUri.getPath() );
                if (f.exists()) {
                    f.delete();
                }

                break;
        }
        startActivity( goWriteIntent );

    }

    private void savePicture(Bitmap photo, String picturePath) {
        FileOutputStream fos = null;
        File saveFile = null;

        try {
            saveFile = new File( picturePath );
            fos = new FileOutputStream( saveFile );
            photo.compress( Bitmap.CompressFormat.JPEG, 100, fos );
        } catch (Exception e) {

        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {

            }
        }
    }

    public void initView() {
//                // *** 주영 5/28 추가코드
//                GPSTracker GPS = new GPSTracker( this );
//    //            ArrayList<Capsule> capsuleRangeList = new ArrayList<>();  // 반경에 해당하는 일기만 담은 ArrayList
//                Spinner spinner = (Spinner)findViewById(R.id.spinner);
//                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                    @Override
//                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                        int range = 50;
//                        switch (position) {
//                            case 0 : range = 50; break;
//                            case 1 : range = 100; break;
//                            case 2 : range = 200; break;
//                            case 3 : range = 300; break;
//                        }
//
//                        ArrayList<Capsule> capsuleList = dbHelper.getAllDiary();  // 모든 일기를 담은 ArrayList
//                        if (capsuleList != null) {
//                            if(capsuleRangeList != null) {
//                                capsuleRangeList.clear();
//                            }
//                            Capsule capsule;
//                            for (int i=0; i<capsuleList.size(); i++) {
//                                capsule = capsuleList.get(i);
//                                if(getDistance(GPS.getLatitude(), GPS.getLongitude(), capsule.getLatitude(), capsule.getLongitude())
//                                        < range) {
//                                    capsuleRangeList.add(capsule);
//                                }
//                            }
//                        }
//
//                        // 반경에 해당하는 일기를 확인하기 위한 테스트 코드
//                        StringBuffer sb = new StringBuffer();
//                        for (int i=0; i<capsuleRangeList.size(); i++) {
//                            sb.append(capsuleRangeList.get(i).toString());
//                        }
//                        Toast.makeText(MainActivity.this, sb, Toast.LENGTH_LONG).show();
//
//                    }
//
//                    @Override
//                    public void onNothingSelected(AdapterView<?> parent) {
//
//                    }
//                });
        //

        // 플로팅 버튼 id 가져오기, 클릭 리스너 선언
        floatingBtn = findViewById( R.id.floatingBtn );
        cameraBtn = findViewById( R.id.cameraBtn );
        galleryBtn = findViewById( R.id.galleryBtn );

        floatingBtn.setOnClickListener( clickListener );
        cameraBtn.setOnClickListener( clickListener );
        galleryBtn.setOnClickListener( clickListener );

        FabOpen = AnimationUtils.loadAnimation( this, R.anim.fab_open );
        FabClose = AnimationUtils.loadAnimation( this, R.anim.fab_close );

        // 작성 날짜 및 개수 받아오기
        //final DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this, "capsule", null, 1);
        ArrayList<String> dateList = dbHelper.getDateList();
        int totalDiary = dbHelper.getDiaryCount();

        totalTv = (TextView) findViewById( R.id.totalTv );
        totalTv.setText( "총 " + totalDiary + "개" );
        mDrawerLayout = (DrawerLayout) findViewById( R.id.drawer_layout );
        mDrawerList = (ListView) findViewById( R.id.left_drawer );

        //mDatesTitles = getResources().getStringArray(R.array.create_date_array);
        //mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mDatesTitles));
        mDrawerList.setAdapter( new ArrayAdapter<String>( this, R.layout.drawer_list_item, dateList ) );
        mDrawerList.setOnItemClickListener( new DrawerItemClickListener() );

        mDrawerToggle = new ActionBarDrawerToggle( this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close ) {

            //drawer가 닫혔을 때, 호출된다.
            public void onDrawerClosed(View view) {
                super.onDrawerClosed( view );
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            // drawer가 열렸을 때, 호출된다.
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened( drawerView );
                //getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        // DrawerListener로 drawer toggle을 설정.
        mDrawerLayout.setDrawerListener( mDrawerToggle );

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
                // 카메라 호출
                Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );

                String timeStamp = new SimpleDateFormat( "yyyyMMddHHmmss" ).format( new Date() );
                String url = "GongGanCapsule_" + timeStamp + ".jpg";

                // 저장 경로에 파일 생성 - 촬영한 이미지 파일을 저장하기 위해 경로 설정
                File storageDir = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES );
                mImageCaptureUri = FileProvider.getUriForFile( getBaseContext(), "capstone.gonggancapsule.fileprovider",
                        new File( storageDir + "/GongGanCapsule", url ) );
                String dirPath = storageDir.getAbsolutePath() + "/GONGGANCAPSULE";

                File directory_GONGGANCAPSULE = new File( dirPath );
                if (!directory_GONGGANCAPSULE.exists())
                    directory_GONGGANCAPSULE.mkdir();

                Log.d( "path", "path : " + mImageCaptureUri.toString() );

                intent.putExtra( MediaStore.EXTRA_OUTPUT, mImageCaptureUri );
                startActivityForResult( intent, CAMERA_REQUEST_CODE );
            }
        } );

        // 갤러리 플로팅 버튼을 클릭했을 때
        galleryBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( Intent.ACTION_PICK );
                intent.setType( "image/*" );
                startActivityForResult( intent, GALLERY_REQUEST_CODE );
            }
        } );
    }


    // *** 주영 5/28 추가코드
    public double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double distance;

        Location locationA = new Location("pointA");
        locationA.setLatitude(lat1);
        locationA.setLongitude(lon1);

        Location locationB = new Location("point B");
        locationB.setLatitude(lat2);
        locationB.setLongitude(lon2);

        distance = locationA.distanceTo(locationB);

        return distance;
    }
    //
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

    @OnClick(R.id.database)
    public void onViewClicked() {
        Intent intent = new Intent( this, DataBaseCheckActivity.class );
        startActivity( intent );
    }
}