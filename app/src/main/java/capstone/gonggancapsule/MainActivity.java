package capstone.gonggancapsule;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ModelRenderable;
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
import uk.co.appoly.arcorelocation.rendering.LocationNode;
import uk.co.appoly.arcorelocation.rendering.LocationNodeRender;
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper;

// ARCore 1.2Ver로 바뀜으로 인해 openGL코드 삭제
public class MainActivity extends AppCompatActivity {
    private boolean installRequested;

    //------Database 관련 코드------
    // Database 임시 확인을 위한 버튼
    @BindView(R.id.database)
    ImageButton database;
    // Database Helper 선언
    DatabaseHelper dbHelper = new DatabaseHelper( this, "capsule", null, 1 );

    // ARSceneform 관련 코드 (라이브러리 사용)
    private ArSceneView arSceneView;

    private ViewRenderable diaryLayoutRenderable;
    private ViewRenderable exampleLayoutRenderable;
    private ViewRenderable exampleLayoutRenderable2;
    private ViewRenderable exampleLayoutRenderable3;
    private ViewRenderable exampleLayoutRenderable4;
    private ModelRenderable andyRenderable;

    private LocationScene locationScene;

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
    private TextView totalTv;

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

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        ButterKnife.bind( this );

        arSceneView = findViewById(R.id.ar_scene_view);

        // 메인 화면 초기화
        initView();

        GPSTracker mGPS = new GPSTracker( this );

        //위치 받아오는지 확인하기 위한 임시코드
        TextView text = findViewById( R.id.longi );
        TextView text2 = findViewById( R.id.lati );

        if (mGPS.canGetLocation) {
            mGPS.getLocation();
            text.setText( "Lat" + mGPS.getLatitude() );
            text2.setText( "Lon" + mGPS.getLongitude() );
        } else {
            text.setText( "Unabletofind" );
            System.out.println( "Unable" );
        }

//        // 일기장 보여줄 레이아웃 임시 설정
//        // 추후 새롭게 만든 레이아웃으로 교체
//        CompletableFuture<ViewRenderable> diaryLayout =
//                ViewRenderable.builder()
//                        .setView(this, R.layout.activity_diary_test)
//                        .build();

//        CompletableFuture.allOf(
//                diaryLayout)
//                .handle(
//                        (notUsed, throwable) -> {
//
//                            if (throwable != null) {
//                                DemoUtils.displayError(
//                                        this, "Unable to load renderables", throwable);
//                                return null;
//                            }
//
//                            try {
//                                diaryLayoutRenderable = diaryLayout.get();
//                            } catch (InterruptedException | ExecutionException ex) {
//                                DemoUtils.displayError(
//                                        this, "Unable to load renderables", ex);
//                            }
//
//                            return null;
//                        });

        CompletableFuture<ViewRenderable> exampleLayout =
                ViewRenderable.builder()
                        .setView(this, R.layout.example_layout)
                        .build();

        CompletableFuture<ViewRenderable> exampleLayout2 =
                ViewRenderable.builder()
                        .setView(this, R.layout.example_layout)
                        .build();

        CompletableFuture<ViewRenderable> exampleLayout3 =
                ViewRenderable.builder()
                        .setView(this, R.layout.example_layout)
                        .build();

        CompletableFuture<ViewRenderable> exampleLayout4 =
                ViewRenderable.builder()
                        .setView(this, R.layout.example_layout)
                        .build();

        CompletableFuture<ModelRenderable> andy = ModelRenderable.builder()
                .setSource(this, R.raw.andy)
                .build();

        CompletableFuture.allOf(
                exampleLayout, exampleLayout2, andy)
                .handle(
                        (notUsed, throwable) -> {

                            if (throwable != null) {
                                DemoUtils.displayError(
                                        this, "Unable to load renderables", throwable);
                                return null;
                            }

                            try {
                                exampleLayoutRenderable = exampleLayout.get();
                                exampleLayoutRenderable2 = exampleLayout2.get();
                                exampleLayoutRenderable3 = exampleLayout3.get();
                                exampleLayoutRenderable4 = exampleLayout4.get();
//                                andyRenderable = andy.get();
                            } catch (InterruptedException | ExecutionException ex) {
                                DemoUtils.displayError(
                                        this, "Unable to load renderables", ex);
                            }

                            return null;
                        });


        // Set an update listener on the Scene that will hide the loading message once a Plane is
        // detected.
//        arSceneView
//                .getScene()
//                .setOnUpdateListener(
//                        frameTime -> {
//                            if (locationScene == null) {
//                                locationScene = new LocationScene(this, this, arSceneView);
//
//                                LocationMarker Caffebene = new LocationMarker(
//                                        2.33, 86.0111,
//                                        getDiaryView()
//                                );
//
//                                // 건국대학교
//                                LocationMarker konkuk = new LocationMarker(
//                                        37.5407667,127.0771488,
//                                        getDiaryView()
//                                );
//
//                                // 건대 호야
//                                LocationMarker hoya = new LocationMarker(
//                                        37.5421674,127.0726771,
//                                        getDiaryView()
//                                );
//
//                                // 세종대학교
//                                LocationMarker sejong = new LocationMarker(
//                                        37.5490116,127.0713384,
//                                        getDiaryView()
//                                );
//
//                                Caffebene.setRenderEvent(new LocationNodeRender() {
//                                    @Override
//                                    public void render(LocationNode node) {
//                                        View eView = diaryLayoutRenderable.getView();
//                                        TextView content = eView.findViewById( R.id.showContentTv );
//                                        ImageView pic = eView.findViewById( R.id.showPictureIv );
//                                        pic.setImageResource( R.drawable.icon_capsule );
//                                        content.setText( "오늘의 일기 \n 카페베네에에에에렝ㄹㅇ" );
////                                        TextView distanceTextView = eView.findViewById(R.id.textView2);
////                                        TextView titleView = eView.findViewById(R.id.textView);
////                                        TextView contentView = eView.findViewById( R.id.content );
////                                        LinearLayout back = eView.findViewById( R.id.back );
////                                        ImageView pic = eView.findViewById( R.id.pic );
////                                        distanceTextView.setText(node.getDistance() + "M");
////                                        titleView.setText( "카페베네의 일기" );
////                                        pic.setImageResource( R.drawable.pic_test2 );
////                                        contentView.setText( "카페베네 망고스무디를 먹어땅" );
////                                        pic.setOnClickListener( new View.OnClickListener() {
////                                            @Override
////                                            public void onClick(View v) {
////                                                pic.setImageResource( R.drawable.pic_test1 );
////                                            }
////                                        } );
////                                        back.setBackgroundColor( Color.parseColor("#C14D38") );
//
//                                    }
//                                });
//
//                                konkuk.setRenderEvent(new LocationNodeRender() {
//                                    @Override
//                                    public void render(LocationNode node) {
//                                        View eView = diaryLayoutRenderable.getView();
//                                        TextView content = eView.findViewById( R.id.showContentTv );
//                                        ImageView pic = eView.findViewById( R.id.showPictureIv );
//                                        pic.setImageResource( R.drawable.icon_capsule );
//                                        content.setText( "오늘의 일기 \n 오늘은 건대 탐탐에서 팀플을 했당" );
////                                        TextView distanceTextView = eView.findViewById(R.id.textView2);
////                                        TextView titleView = eView.findViewById(R.id.textView);
////                                        TextView contentView = eView.findViewById( R.id.content );
////                                        LinearLayout back = eView.findViewById( R.id.back );
////                                        ImageView pic = eView.findViewById( R.id.pic );
////                                        distanceTextView.setText(node.getDistance() + "M");
////                                        titleView.setText( "카페베네의 일기" );
////                                        pic.setImageResource( R.drawable.pic_test2 );
////                                        contentView.setText( "카페베네 망고스무디를 먹어땅" );
////                                        pic.setOnClickListener( new View.OnClickListener() {
////                                            @Override
////                                            public void onClick(View v) {
////                                                pic.setImageResource( R.drawable.pic_test1 );
////                                            }
////                                        } );
////                                        back.setBackgroundColor( Color.parseColor("#C14D38") );
//
//                                    }
//                                });
//
//                                hoya.setRenderEvent(new LocationNodeRender() {
//                                    @Override
//                                    public void render(LocationNode node) {
//                                        View eView = diaryLayoutRenderable.getView();
//                                        TextView content = eView.findViewById( R.id.showContentTv );
//                                        ImageView pic = eView.findViewById( R.id.showPictureIv );
//                                        pic.setImageResource( R.drawable.icon_capsule );
//                                        content.setText( "오늘의 일기 \n 자외선 너무 세ㅠㅠ" );
////                                        TextView distanceTextView = eView.findViewById(R.id.textView2);
////                                        TextView titleView = eView.findViewById(R.id.textView);
////                                        TextView contentView = eView.findViewById( R.id.content );
////                                        LinearLayout back = eView.findViewById( R.id.back );
////                                        distanceTextView.setText(node.getDistance() + "M");
////                                        titleView.setText( "할리스의 일기" );
////                                        contentView.setText( "나는 할리스에 자주간당" );
////                                        back.setBackgroundColor( Color.parseColor("#B96CA7") );
//                                    }
//                                });
//
//                                sejong.setRenderEvent(new LocationNodeRender() {
//                                    @Override
//                                    public void render(LocationNode node) {
//                                        View eView = diaryLayoutRenderable.getView();
//                                        TextView content = eView.findViewById( R.id.showContentTv );
//                                        ImageView pic = eView.findViewById( R.id.showPictureIv );
//                                        pic.setImageResource( R.drawable.icon_capsule );
//                                        content.setText( "오늘의 일기 \n 후후후후훟ㅎㅎ" );
////                                        TextView distanceTextView = eView.findViewById(R.id.textView2);
////                                        TextView titleView = eView.findViewById(R.id.textView);
////                                        TextView contentView = eView.findViewById( R.id.content );
////                                        LinearLayout back = eView.findViewById( R.id.back );
////                                        distanceTextView.setText(node.getDistance() + "M");
////                                        titleView.setText( "스타벅스의 일기" );
////                                        contentView.setText( "스타벅스 아메리카노 맛이따" );
////                                        back.setBackgroundColor( Color.parseColor("#6CB972") );
//                                    }
//                                });
//
//                                // Adding the marker
//                                // 마커를 ADD
//                                locationScene.mLocationMarkers.add(Caffebene);
//                                locationScene.mLocationMarkers.add(konkuk);
//                                locationScene.mLocationMarkers.add(hoya);
//                                locationScene.mLocationMarkers.add(sejong);
//
//                                Toast.makeText( this, "레이아웃", Toast.LENGTH_SHORT ).show();
//                            }
//
//                            Frame frame = arSceneView.getArFrame();
//
//                            if (frame == null) {
//                                return;
//                            }
//
//                            if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
//                                return;
//                            }
//
//                            if (locationScene != null) {
//                                locationScene.processFrame(frame);
//                            }
//
//                        });

        arSceneView
                .getScene()
                .setOnUpdateListener(
                        frameTime -> {
                            if (locationScene == null) {
                                locationScene = new LocationScene(this, this, arSceneView);

                                // 충무로 카페베네 앞.
                                LocationMarker Caffebene = new LocationMarker(
                                        2.33, 86.0111,
                                        getExampleView()
                                );

                                // 충무로 할리스
                                LocationMarker Hollys = new LocationMarker(
                                        -0.119677, 51.478494,
                                        getExampleView2()
                                );

//                                37.561382, 126.993892
                                // 충무로 스타벅스
                                LocationMarker StarBucks = new LocationMarker(
                                        37.561382, 126.993892,
                                        getExampleView3()
                                );

                                // 서울여자대학교
                                LocationMarker swu = new LocationMarker(
                                        37.99999, 111.123124,
                                        getExampleView4()
                                );

                                // textView에 일기 제목, textView2에 거리를 보여줌.
                                // 거리가 이상하게 나오니 새로 만드는게 나을듯.
                                Caffebene.setRenderEvent(new LocationNodeRender() {
                                    @Override
                                    public void render(LocationNode node) {
                                        View eView = exampleLayoutRenderable.getView();
                                        TextView distanceTextView = eView.findViewById(R.id.textView2);
                                        TextView titleView = eView.findViewById(R.id.textView);
                                        TextView contentView = eView.findViewById( R.id.content );
                                        LinearLayout back = eView.findViewById( R.id.back );
                                        ImageView pic = eView.findViewById( R.id.pic );
                                        distanceTextView.setText(node.getDistance() + "M");
                                        titleView.setText( "카페베네의 일기" );
//                                        pic.setImageResource( R.drawable.pic_test1 );
                                        contentView.setText( "카페베네 망고스무디를 먹어땅" );
                                        pic.setOnClickListener( new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                pic.setImageResource( R.drawable.pic_test1 );
                                            }
                                        } );
                                        back.setBackgroundColor( Color.parseColor("#C14D38") );

                                    }
                                });

                                Hollys.setRenderEvent(new LocationNodeRender() {
                                    @Override
                                    public void render(LocationNode node) {
                                        View eView = exampleLayoutRenderable2.getView();
                                        TextView distanceTextView = eView.findViewById(R.id.textView2);
                                        TextView titleView = eView.findViewById(R.id.textView);
                                        TextView contentView = eView.findViewById( R.id.content );
                                        LinearLayout back = eView.findViewById( R.id.back );
                                        distanceTextView.setText(node.getDistance() + "M");
                                        titleView.setText( "할리스의 일기" );
                                        contentView.setText( "나는 할리스에 자주간당" );
                                        back.setBackgroundColor( Color.parseColor("#B96CA7") );
                                    }
                                });

                                StarBucks.setRenderEvent(new LocationNodeRender() {
                                    @Override
                                    public void render(LocationNode node) {
                                        View eView = exampleLayoutRenderable3.getView();
                                        TextView distanceTextView = eView.findViewById(R.id.textView2);
                                        TextView titleView = eView.findViewById(R.id.textView);
                                        TextView contentView = eView.findViewById( R.id.content );
                                        LinearLayout back = eView.findViewById( R.id.back );
                                        distanceTextView.setText(node.getDistance() + "M");
                                        titleView.setText( "스타벅스의 일기" );
                                        contentView.setText( "스타벅스 아메리카노 맛이따" );
                                        back.setBackgroundColor( Color.parseColor("#6CB972") );
                                    }
                                });

                                swu.setRenderEvent(new LocationNodeRender() {
                                    @Override
                                    public void render(LocationNode node) {
                                        View eView = exampleLayoutRenderable4.getView();
                                        TextView distanceTextView = eView.findViewById(R.id.textView2);
                                        TextView titleView = eView.findViewById(R.id.textView);
                                        TextView contentView = eView.findViewById( R.id.content );
                                        LinearLayout back = eView.findViewById( R.id.back );
                                        distanceTextView.setText(node.getDistance() + "M");
                                        titleView.setText( "서울여대의 일기" );
                                        contentView.setText( "내일 휴강했으면..." );
                                        back.setBackgroundColor( Color.parseColor("#C4D9FF") );
                                    }
                                });

                                // Adding the marker
                                // 마커를 ADD
                                locationScene.mLocationMarkers.add(Caffebene);
                                locationScene.mLocationMarkers.add(Hollys);
                                locationScene.mLocationMarkers.add(StarBucks);
                                locationScene.mLocationMarkers.add(swu);

                                // 나중에 캡슐 모양으로 대체
                                // 지금은 안보임.
                                // Adding a simple location marker of a 3D model
                                locationScene.mLocationMarkers.add(
                                        new LocationMarker(
                                                -0.119677,
                                                51.478494,
                                                getAndy()));
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

// Lastly request CAMERA & fine location permission which is required by ARCore-Location.
        ARLocationPermissionHelper.requestPermission(this);
    }

    private Node getExampleView() {
        Node base = new Node();
        base.setRenderable(exampleLayoutRenderable);

        return base;
    }

    private Node getExampleView2() {
        Node base = new Node();
        base.setRenderable(exampleLayoutRenderable2);

        return base;
    }

    private Node getExampleView3() {
        Node base = new Node();
        base.setRenderable(exampleLayoutRenderable3);

        return base;
    }

    private Node getExampleView4() {
        Node base = new Node();
        base.setRenderable(exampleLayoutRenderable4);

        return base;
    }

    private Node getAndy() {
        Node base = new Node();
        base.setRenderable(andyRenderable);
        Context c = this;
        base.setOnTapListener((v, event) -> {
            Toast.makeText(
                    c, "Andy touched.", Toast.LENGTH_LONG)
                    .show();
        });
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

    private Node getDiaryView() {
        Node base = new Node();
        base.setRenderable(diaryLayoutRenderable);

        return base;
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @OnClick(R.id.database)
    public void onViewClicked() {
        Intent intent = new Intent( this, DataBaseCheckActivity.class );
        startActivity( intent );
    }
}