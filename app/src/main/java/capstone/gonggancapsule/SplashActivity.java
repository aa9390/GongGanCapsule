package capstone.gonggancapsule;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler();

        handler.postDelayed( new Runnable() {
            @Override
            public void run() {
                    Intent intent = new Intent( getApplicationContext(), MainActivity.class );
                    startActivity( intent );
                    finish();
            }
        }, 2500 );

        JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
        ComponentName componentName = new ComponentName(getApplicationContext(), MyJobService.class);

        JobInfo jobInfo = new JobInfo.Builder(1, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(DateUtils.MINUTE_IN_MILLIS*15)
                .setPersisted(true)
                .build();
        jobScheduler.schedule(jobInfo);
    }
}
