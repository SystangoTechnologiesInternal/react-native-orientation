package com.github.yamill.orientation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.ReactConstants;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class OrientationModule extends ReactContextBaseJavaModule {
    final private Activity mActivity;
    final ReactApplicationContext ctx ;

    public OrientationModule(ReactApplicationContext reactContext, final Activity activity) {
        super(reactContext);

        mActivity = activity;
        ctx = reactContext;

        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Configuration newConfig = intent.getParcelableExtra("newConfig");

                DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
                WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = (int)(size.x/metrics.density);
                int height = (int)(size.y/metrics.density);

                String orientationValue;
                if (newConfig != null) {
                    orientationValue = newConfig.orientation == 1 ? "PORTRAIT" : "LANDSCAPE";
                } else {
                    orientationValue = context.getResources().getConfiguration().orientation == 1 ? "PORTRAIT" : "LANDSCAPE" ;
                }

                WritableMap params = Arguments.createMap();
                params.putString("orientation", orientationValue);
                params.putString("width", String.valueOf(width));
                params.putString("height", String.valueOf(height));


                if (ctx.hasActiveCatalystInstance()) {
                    ctx
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("orientationDidChange", params);
                }
            }
        };

        activity.registerReceiver(receiver, new IntentFilter("onConfigurationChanged"));

        LifecycleEventListener listener = new LifecycleEventListener() {
            @Override
            public void onHostResume() {
                activity.registerReceiver(receiver, new IntentFilter("onConfigurationChanged"));
            }

            @Override
            public void onHostPause() {
                try
                {
                    activity.unregisterReceiver(receiver);
                }
                catch (java.lang.IllegalArgumentException e) {
                    FLog.e(ReactConstants.TAG, "receiver already unregistered", e);
                }
            }

            @Override
            public void onHostDestroy() {
                try
                {
                    activity.unregisterReceiver(receiver);
                }
                catch (java.lang.IllegalArgumentException e) {
                    FLog.e(ReactConstants.TAG, "receiver already unregistered", e);
                }
            }
        };

        reactContext.addLifecycleEventListener(listener);
    }

    @Override
    public String getName() {
        return "Orientation";
    }

    @ReactMethod
    public void getOrientation(Callback callback) {
        final int orientationInt = getReactApplicationContext().getResources().getConfiguration().orientation;

        String orientation = this.getOrientationString(orientationInt);

        DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = (int)(size.x/metrics.density);
        int height = (int)(size.y/metrics.density);

        if (orientation == "null") {
          callback.invoke(orientationInt, null, String.valueOf(width), String.valueOf(height));
        } else {
          callback.invoke(null, orientation, String.valueOf(width), String.valueOf(height));
        }

    }




    @ReactMethod
    public void lockToPortrait() {
      mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @ReactMethod
    public void lockToLandscape() {
      mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    @ReactMethod
    public void lockToLandscapeLeft() {
      mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @ReactMethod
    public void lockToLandscapeRight() {
      mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    }

    @ReactMethod
    public void enableIQKeyBoardManager() {

    }
    @ReactMethod
    public void getRectString(Callback callback) {
        DisplayMetrics metrics = mActivity.getResources().getDisplayMetrics();;
        WindowManager wm = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = (int)(size.x/metrics.density);
        int height = (int)(size.y/metrics.density);
        callback.invoke(null, String.valueOf(width), String.valueOf(height));
    }
    @ReactMethod
    public void disableIQKeyBoardManager() {

    }

    @ReactMethod
    public void unlockAllOrientations() {
      mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public @Nullable Map<String, Object> getConstants() {
      HashMap<String, Object> constants = new HashMap<String, Object>();
      int orientationInt = getReactApplicationContext().getResources().getConfiguration().orientation;

      String orientation = this.getOrientationString(orientationInt);
      if (orientation == "null") {
        constants.put("initialOrientation", null);
      } else {
        constants.put("initialOrientation", orientation);
      }


      return constants;
    }

    private String getOrientationString(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return "LANDSCAPE";
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return "PORTRAIT";
        } else if (orientation == Configuration.ORIENTATION_UNDEFINED) {
            DisplayMetrics metrics = mActivity.getResources().getDisplayMetrics();;
            WindowManager wm = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = (int)(size.x/metrics.density);
            int height = (int)(size.y/metrics.density);
            String orientationStr;
            if(width>height)
                orientationStr="LANDSCAPE";
            else
                orientationStr="PORTRAIT";

            return orientationStr;
        } else {
            return "null";
        }
    }
}
