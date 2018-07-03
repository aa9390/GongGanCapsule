package capstone.gonggancapsule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;
import capstone.gonggancapsule.database.DatabaseHelper;
import uk.co.appoly.arcorelocation.LocationMarker;
import uk.co.appoly.arcorelocation.LocationScene;
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper;

public class MainActivity extends AppCompatActivity {
    Activity activity = this;
    Context context = this;

    public static MainActivity activity2 = null;

    private boolean installRequested;

    // Database 임시 확인을 위한 버튼
    @BindView(R.id.database)
    ImageButton database;

    // Database Helper 선언
    DatabaseHelper dbHelper = new DatabaseHelper(this, "capsule", null, 3);

    // ARSceneform 관련 코드
    private ArSceneView arSceneView;
    private ArrayList<ViewRenderable> diaryRenderableList = new ArrayList<>();
    private ArrayList<ModelRenderable> capsuleRenderableList = new ArrayList<>();
    private ArrayList<ViewRenderable> infoCapsuleRenderableList = new ArrayList<>();
    private ArrayList<CompletableFuture<ViewRenderable>> diaryLayoutList = new ArrayList<>();
    private ArrayList<CompletableFuture<ModelRenderable>> objCapsuleList = new ArrayList<>();
    private ArrayList<CompletableFuture<ViewRenderable>> infoLayoutList = new ArrayList<>();
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
    public String mCurrentPhotoPath;

    // 캡슐 객체 관련 코드
    public ArrayList<Capsule> capsuleList = null;
    public ArrayList<Capsule> capsuleRangeList = null;
    Capsule capsule;
    Glide glide;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        capsuleRangeList = new ArrayList<>();
        activity2 = this;
        arSceneView = findViewById(R.id.ar_scene_view);

        // 메인 화면 초기화
        initView();
        capsuleList = dbHelper.getAllDiary();

        GPSTracker mGPS = new GPSTracker(this);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int range = 50;
                switch (position) {
                    case 0:
                        range = 50;
                        break;
                    case 1:
                        range = 100;
                        break;
                    case 2:
                        range = 200;
                        break;
                    case 3:
                        range = 300;
                        break;
                }

                renderable(range, capsuleList, mGPS);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void renderable(int range, ArrayList<Capsule> capsuleList, GPSTracker mGPS) {
        if (capsuleList != null) {
            capsuleRangeList.clear();
            // 선택한 반경 안의 캡슐만 list에 add
            for (int i = 0; i < capsuleList.size(); i++) {
                capsule = capsuleList.get(i);
                if (getDistance(mGPS.getLatitude(), mGPS.getLongitude(), capsule.getLatitude(), capsule.getLongitude()) < range) {
                    capsuleRangeList.add(capsule);
                }
            }
        }

        // 일기장 보여줄 레이아웃 설정
        if (capsuleRangeList.size() != 0) {

            // 반경 안에 있는 일기장 개수 만큼
            // diaryLayoutList, objCapsuleList, infoLayoutList 생성
            for (int i = 0; i < capsuleRangeList.size(); i++) {
                diaryLayoutList.add(ViewRenderable.builder().setView(context, R.layout.item_diary_3).build());
                objCapsuleList.add(ModelRenderable.builder().setSource(context, Uri.parse("obj_capsule.sfb")).build());
                infoLayoutList.add(ViewRenderable.builder().setView(context, R.layout.item_capsule_top).build());
            }

            CompletableFuture<ViewRenderable> diary = new CompletableFuture<>();
            CompletableFuture<ModelRenderable> objCapsule = new CompletableFuture<>();
            CompletableFuture<ViewRenderable> infoCapsule = new CompletableFuture<>();

            for (int i = 0; i < capsuleRangeList.size(); i++) {
                diary = diaryLayoutList.get(i);
                objCapsule = objCapsuleList.get(i);
                infoCapsule = infoLayoutList.get(i);
            }

            CompletableFuture
                    .allOf(diary, objCapsule, infoCapsule)
                    .handle(
                            (notUsed, throwable) -> {
                                if (throwable != null) {
                                    DemoUtils.displayError(
                                            getApplicationContext(), "Unable to load renderables", throwable);
                                    return null;
                                }
                                try {
                                    if (capsuleRangeList.size() != 0) {
                                        for (int i = 0; i < capsuleRangeList.size(); i++) {
                                            diaryRenderableList.add(diaryLayoutList.get(i).get());
                                            capsuleRenderableList.add(objCapsuleList.get(i).get());
                                            infoCapsuleRenderableList.add(infoLayoutList.get(i).get());
                                        }
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

                                    Toast.makeText(getApplicationContext(), "내 주변에 캡슐이 " + capsuleRangeList.size() + "개 있습니다.", Toast.LENGTH_SHORT).show();

                                    LocationMarker[] locationMarker = new LocationMarker[100];

                                    for (int i = 0; i < capsuleRangeList.size(); i++) {
                                        // node 객체 생성
                                        Node base = new Node();

                                        base.setRenderable(capsuleRenderableList.get( i ));

                                        Node info = new Node();
                                        info.setRenderable( infoCapsuleRenderableList.get( i ) );
                                        info.setParent( base );
                                        info.setLocalPosition( new Vector3( 0.0f, 0.5f, 0.0f ));

                                        // DB에서 위도, 경도를 받아와 location Marker에 저장.
                                        locationMarker[i] = new LocationMarker(
                                                capsuleRangeList.get( i ).getLongitude(), capsuleRangeList.get( i ).getLatitude(), base );

                                        int finalI = i;
                                        AtomicBoolean touched = new AtomicBoolean( false );

                                        // Renderable 터치 시 이벤트 구현
                                        base.setOnTapListener( (hitTestResult, motionEvent) -> {
                                            if(!touched.get()) {
                                                base.setRenderable( diaryRenderableList.get( finalI ) );
                                                info.setEnabled( false );
                                                touched.set( true );
                                            }
                                            else if (touched.get()) {
                                                base.setRenderable( capsuleRenderableList.get( finalI ) );
                                                info.setEnabled( true );
                                                touched.set( false );
                                            }
                                        } );

                                        locationMarker[i].setRenderEvent( node -> {
                                                    // DB에서 날짜, 내용등을 불러와 diary에 띄움.
                                                    View eView = diaryRenderableList.get( finalI ).getView();
                                                    TextView content = eView.findViewById( R.id.showContentTv );
                                                    ImageView pic = eView.findViewById( R.id.showPictureIv );
                                                    TextView date = eView.findViewById(R.id.showDateTv);
                                                    TextView title = eView.findViewById( R.id.showTitleTv );
                                                    date.setText(capsuleRangeList.get( finalI ).getCreate_date());
                                                    Glide.with(MainActivity.this).load(capsuleRangeList.get(finalI).getPicture()).into(pic);
                                                    content.setText( capsuleRangeList.get( finalI ).getContent() );
                                                    title.setText( capsuleRangeList.get( finalI ).getTitle() );
                                                    TextView distanceTextView = eView.findViewById( R.id.distance );
                                                    distanceTextView.setText( node.getDistance() + "M" );

                                                    View infoView = infoCapsuleRenderableList.get( finalI ).getView();
                                                    TextView distance = infoView.findViewById( R.id.distance );
                                                    distance.setText( node.getDistance() + "M" );
                                                }
                                        );

                                        locationScene.mLocationMarkers.add( locationMarker[i] );
                                    }
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

        } else if(capsuleRangeList.size() == 0) {
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (locationScene != null) {
            locationScene.resume();
        }

        if (arSceneView.getSession() == null) {
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
            selectItem(position);
        }

        private void selectItem(int position) {
            Log.d("position", "position : " + position);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.floatingBtn:
                    if (!isOpen) {
                        cameraBtn.startAnimation(FabOpen);
                        galleryBtn.startAnimation(FabOpen);
                        isOpen = true;
                    } else {
                        cameraBtn.startAnimation(FabClose);
                        galleryBtn.startAnimation(FabClose);
                        isOpen = false;
                    }
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        Intent goWriteIntent = new Intent(MainActivity.this, WriteDiary.class);

        switch (requestCode) {
            case GALLERY_REQUEST_CODE:
                mImageCaptureUri = data.getData();
                String galleryPath = getPath(mImageCaptureUri);
                goWriteIntent.putExtra("galleryPath", galleryPath);
                break;

            case CAMERA_REQUEST_CODE:
                String cameraPath = mCurrentPhotoPath;
                Log.d("path", cameraPath);
                goWriteIntent.putExtra("cameraPath", cameraPath);
                break;
        }
        startActivity(goWriteIntent);

    }

    // 갤러리 사진 경로 변환
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(columnIndex);
    }

    public void initView() {
        // 플로팅 버튼 id 가져오기, 클릭 리스너 선언
        floatingBtn = findViewById(R.id.floatingBtn);
        cameraBtn = findViewById(R.id.cameraBtn);
        galleryBtn = findViewById(R.id.galleryBtn);

        floatingBtn.setOnClickListener(clickListener);
        cameraBtn.setOnClickListener(clickListener);
        galleryBtn.setOnClickListener(clickListener);

        FabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);

        // 작성 날짜 및 개수 받아오기
        totalTv = (TextView) findViewById(R.id.totalTv);
        setTotalCount();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        capsuleList = dbHelper.getAllDiary();
        TitleAdapter titleAdapter = new TitleAdapter(MainActivity.this, capsuleList);
        mDrawerList.setAdapter(titleAdapter);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            //drawer가 닫혔을 때, 호출된다.
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            // drawer가 열렸을 때, 호출된다.
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };
        // DrawerListener로 drawer toggle을 설정.
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // 툴바 생성 및 세팅하는 부분
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.btn_menu_2);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);


        // 카메라 플로팅 버튼을 클릭했을 때
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 카메라 호출
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                File pictureFile = null;
                try {
                    pictureFile = createImageFile();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "pictureFile 에러", Toast.LENGTH_LONG).show();
                }

                if (pictureFile != null) {
                    mImageCaptureUri = FileProvider.getUriForFile(MainActivity.this, "capstone.gonggancapsule.fileprovider", pictureFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                    startActivityForResult(intent, CAMERA_REQUEST_CODE);
                }
            }
        });

        // 갤러리 플로팅 버튼을 클릭했을 때
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }
        });
    }

    private File createImageFile() throws IOException {
        //Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "GongGanCapsule_" + timeStamp + ".jpg";
        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/GongGanCapsule/" + imageFileName);

        File directory_GONGGANCAPSULE = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/GongGanCapsule");
        if (!directory_GONGGANCAPSULE.exists())
            directory_GONGGANCAPSULE.mkdir();

        //Save a file
        mCurrentPhotoPath = storageDir.getAbsolutePath(); //현재 사용중인 사진의 경로(디바이스 내 파일 경로)
        Log.i("path", mCurrentPhotoPath);
        return storageDir;
    }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @OnClick(R.id.database)
//    public void onViewClicked() {
//        Intent intent = new Intent(this, DataBaseCheckActivity.class);
//        startActivity(intent);
//    }

    public void setTotalCount() {
        int totalDiary = dbHelper.getDiaryCount();
        totalTv.setText("총 " + totalDiary + "개");
    }
}