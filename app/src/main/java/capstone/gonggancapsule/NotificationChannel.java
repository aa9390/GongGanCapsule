package capstone.gonggancapsule;

import android.app.Application;
import android.app.NotificationManager;
import android.os.Build;

public class NotificationChannel extends Application {
    public static final String CHANNEL_1_ID = "capsule";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.NotificationChannel capsule = new android.app.NotificationChannel(
                    CHANNEL_1_ID,
                    "capsule",
                    NotificationManager.IMPORTANCE_HIGH
            );
            capsule.setDescription("공간캡슐");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(capsule);
        }
    }
}
