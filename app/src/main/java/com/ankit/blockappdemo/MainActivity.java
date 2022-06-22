package com.ankit.blockappdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btnAskPermission, btnGetInstalledApps;
    LinearLayout llViews;
    SharedPreferences sharedPreferences;
    SwitchCompat switchInstagram, switchFacebook, switchWhatsApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        btnAskPermission = findViewById(R.id.btnAskPermission);
        btnGetInstalledApps = findViewById(R.id.btnGetInstalledApps);
        llViews = findViewById(R.id.llViews);
        switchInstagram = findViewById(R.id.switchInstagram);
        switchFacebook = findViewById(R.id.switchFacebook);
        switchWhatsApp = findViewById(R.id.switchWhatsApp);

        btnAskPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            }
        });

        btnGetInstalledApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PackageManager packageManager = getPackageManager();
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

                List<ResolveInfo> appList = packageManager.queryIntentActivities(mainIntent, 0);
                Collections.sort(appList, new ResolveInfo.DisplayNameComparator(packageManager));
                List<PackageInfo> packs = packageManager.getInstalledPackages(0);
                for (int i = 0; i < packs.size(); i++) {
                    PackageInfo p = packs.get(i);
                    ApplicationInfo a = p.applicationInfo;
                    // skip system apps if they shall not be included
                    if ((a.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                        continue;
                    }
                    Log.e("package name", p.packageName);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Log.e("category", p.applicationInfo.category + "");
                    }
                }
            }
        });

        switchInstagram.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setSharedPreference(Utility.Instagram, b);
                initSwitch();
            }
        });

        switchFacebook.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setSharedPreference(Utility.Facebook, b);
                initSwitch();
            }
        });

        switchWhatsApp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setSharedPreference(Utility.WhatsApp, b);
                initSwitch();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
            Utility.askOverlayPermission(MainActivity.this);
        }
    }

    public void setSharedPreference(String key, boolean value) {
        if (Utility.isPackageInstalled(this, key)) {
            SharedPreferences.Editor myEdit = sharedPreferences.edit();
            myEdit.putBoolean(key, value);
            myEdit.apply();
        } else {
            Toast.makeText(this, "App not installed", Toast.LENGTH_SHORT).show();
        }
    }

    public void initSwitch() {
        switchInstagram.setChecked(sharedPreferences.getBoolean(Utility.Instagram, false));
        switchFacebook.setChecked(sharedPreferences.getBoolean(Utility.Facebook, false));
        switchWhatsApp.setChecked(sharedPreferences.getBoolean(Utility.WhatsApp, false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utility.isUsageStatsPermissionGranted(this)) {
            btnAskPermission.setVisibility(View.GONE);
            llViews.setVisibility(View.VISIBLE);

            startForegroundAppService();
        } else {
            btnAskPermission.setVisibility(View.VISIBLE);
            llViews.setVisibility(View.GONE);
        }
        initSwitch();
    }

    public void startForegroundAppService() {
        Intent intent = new Intent(MainActivity.this, GetForegroundAppService.class);
        Log.e("isMyServiceRunning", Utility.isMyServiceRunning(MainActivity.this, GetForegroundAppService.class) + "");
        if (!Utility.isMyServiceRunning(MainActivity.this, GetForegroundAppService.class)) { // IMPORTANT
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
            startService(intent);
            bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    // We've bound to LocalService, cast the IBinder and get LocalService instance
                    GetForegroundAppService.LocalBinder binder = (GetForegroundAppService.LocalBinder) service;
                    GetForegroundAppService getForegroundAppService = binder.getService();

                    Log.e("GetForegroundAppService", "connected");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            }, Context.BIND_AUTO_CREATE);
        }
    }
}