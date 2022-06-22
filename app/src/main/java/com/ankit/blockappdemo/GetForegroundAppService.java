package com.ankit.blockappdemo;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class GetForegroundAppService extends Service {

    private String tag = "GetForegroundAppService";
    CountDownTimer checkForegroundApps;
    SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        checkForegroundApps = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
//                Log.e("checkForegroundApps", "checkForegroundApps");
                UsageStatsManager usm = null;
                usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
                long time = System.currentTimeMillis();
                List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
//                Log.e("appList", appList.size() + "");
                if (appList.size() > 0) {
                    SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                    for (UsageStats usageStats : appList) {
                        mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                    }
                    if (!mySortedMap.isEmpty()) {
                        String currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                        Log.e("currentApp", currentApp);
                        if (sharedPreferences.getBoolean(Utility.Instagram, false) && currentApp.equals(Utility.Instagram)) {
                            closeApp(Utility.Instagram);
                        } else if (sharedPreferences.getBoolean(Utility.Facebook, false) && currentApp.equals(Utility.Facebook)) {
                            closeApp(Utility.Facebook);
                        } else if (sharedPreferences.getBoolean(Utility.WhatsApp, false) && currentApp.equals(Utility.WhatsApp)) {
                            closeApp(Utility.WhatsApp);
                        }
                    }
                }

                checkForegroundApps.start();
            }
        }.start();
    }

    public void closeApp(String packageName) {
        Toast.makeText(GetForegroundAppService.this, "App Close", Toast.LENGTH_SHORT).show();
//        Intent startMain = new Intent(Intent.ACTION_MAIN);
//        startMain.addCategory(Intent.CATEGORY_HOME);
//        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(startMain);

        Intent i = new Intent(GetForegroundAppService.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(tag, "onStartCommand"); // 2
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    public class LocalBinder extends Binder {
        public GetForegroundAppService getService() {
            // Return this instance of LocalService so clients can call public methods
            return GetForegroundAppService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(tag, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.e("onDestroy", "onDestroy");
        super.onDestroy();
    }
}
