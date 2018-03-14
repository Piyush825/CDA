package com.example.Piyush.CDA;

/**
 * Created by akshat on 13/3/18.
 */


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;


import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

public class ActivityRecognizedService extends Service {
    public ActivityRecognizedService() {}
    public static String LOCAL_BROADCAST_NAME = "LOCAL_ACT_RECOGNITION";
    public static String LOCAL_BROADCAST_EXTRA = "RESULT";

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities( result.getProbableActivities() );
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        StringBuilder str = new StringBuilder();

        for(DetectedActivity activity : probableActivities) {
            switch( activity.getType() ) {
                case DetectedActivity.IN_VEHICLE: {
                    str.append("In Vehicle: " + activity.getConfidence() + "\n");
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    str.append("On Bicycle: " + activity.getConfidence() + "\n");
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    str.append("On Foot: " + activity.getConfidence() + "\n");
                    break;
                }
                case DetectedActivity.RUNNING: {
                    str.append("Running: " + activity.getConfidence() + "\n");
                    break;
                }
                case DetectedActivity.STILL: {
                    str.append("Still: " + activity.getConfidence() + "\n");
                    break;
                }
                case DetectedActivity.TILTING: {
                    str.append("Tilting: " + activity.getConfidence() + "\n");
                    break;
                }
                case DetectedActivity.WALKING: {
                    str.append("Walking: " + activity.getConfidence() + "\n");
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    str.append("Unknown: " + activity.getConfidence() + "\n");
                    break;
                }
            }
        }

        Intent intent = new Intent(LOCAL_BROADCAST_NAME);
        intent.putExtra(LOCAL_BROADCAST_EXTRA, str.toString());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}