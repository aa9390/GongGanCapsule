package capstone.gonggancapsule;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import capstone.gonggancapsule.database.DatabaseHelper;

import static capstone.gonggancapsule.NotificationChannel.CHANNEL_1_ID;

public class MyJobService extends JobService {
    JobParameters params;
    DoItTask doIt;
    private NotificationManagerCompat notificationManager;
    ArrayList<Capsule> capsuleList;
    final ArrayList<Capsule> capsuleRangeList = new ArrayList<>();
    Capsule capsule;

    public boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;

    Location location; // location
    double latitude = 0; // latitude
    double longitude = 0; // longitude

    @Override
    public boolean onStartJob(JobParameters params) {
        this.params = params;
        doIt = new DoItTask();
        doIt.execute();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if(doIt != null)
            doIt.cancel(true);
        return false;
    }

    private class DoItTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            jobFinished(params,false);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext(), "capsule", null, 1);
            //GPSTracker mGPS = new GPSTracker( MyJobService.this );
            capsuleList = dbHelper.getAllDiary();

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(isGPSEnabled) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }else if(isNetworkEnabled){
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            latitude = location.getLatitude();
            longitude = location.getLongitude();

            double distance;
            Location locationA = new Location("point A");
            Location locationB = new Location("point B");

            locationA.setLatitude(latitude);
            locationA.setLongitude(longitude);

            if (capsuleList != null) {
                if (capsuleRangeList != null) {
                    capsuleRangeList.clear();
                }
                // 선택한 반경 안의 캡슐만 list에 add
                for (int i = 0; i < capsuleList.size(); i++) {
                    capsule = capsuleList.get(i);

                    locationB.setLatitude(capsule.getLatitude());
                    locationB.setLongitude(capsule.getLongitude());

                    distance = locationA.distanceTo(locationB);

                    if ( distance < 300 ) {
                        capsuleRangeList.add(capsule);
                        if (capsuleRangeList.size() != 0) {

                            // push notification
                            notificationManager = NotificationManagerCompat.from(getApplicationContext());
                            Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                            PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                            Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_1_ID)
                                    .setSmallIcon(R.drawable.icon_capsule_5)
                                    .setContentTitle("공간캡슐")
                                    .setContentText("300m 이내에 공간캡슐이 " + capsuleRangeList.size() + "개 있습니다")
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                    .setContentIntent(resultPendingIntent)
                                    .setAutoCancel(true)
                                    .build();

                            notificationManager.notify(1, notification);
                        }
                    }
                }
            }
            return null;
        }
    }
}
