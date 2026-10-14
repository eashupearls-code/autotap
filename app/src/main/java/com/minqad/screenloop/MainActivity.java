package com.minqad.screenloop;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.content.Context;
import android.widget.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;

public class MainActivity extends Activity {
    private android.content.SharedPreferences prefs;
    private EditText cycles, startY, endY, waitMs;
    private TextView pointsLabel, permissionLabel;
    private LinearLayout root;
    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }
    private TextView text(String s, int size, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(Color.rgb(28,35,48));
        if (bold) t.setTypeface(null, Typeface.BOLD); t.setPadding(0, dp(5), 0, dp(5)); return t;
    }
    private EditText field(String hint, String value) {
        EditText e = new EditText(this); e.setSingleLine(true); e.setText(value); e.setHint(hint); e.setTextSize(16);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        e.setPadding(dp(12), dp(8), dp(12), dp(8)); e.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.LTGRAY)); return e;
    }
    private Button button(String label, Runnable action) {
        Button b = new Button(this); b.setText(label); b.setAllCaps(false); b.setOnClickListener(v -> action.run()); return b;
    }
    private void add(View v) { LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.bottomMargin = dp(7); root.addView(v, lp); }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); prefs = getSharedPreferences("screenloop", MODE_PRIVATE);
        ScrollView scroll = new ScrollView(this); root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(20),dp(22),dp(20),dp(20)); root.setBackgroundColor(Color.rgb(245,247,251)); scroll.addView(root); setContentView(scroll);
        add(text("ScreenLoop", 28, true)); add(text("Custom tap + screen-swipe automation", 15, false));
        add(text("Cycle settings", 19, true));
        add(text("Target cycles (one cycle = tap all points, then swipe)", 14, false)); cycles = field("e.g. 100", ""+prefs.getInt("cycles",100)); add(cycles);
        add(text("Swipe start (% from top)",14,false)); startY = field("85", ""+prefs.getInt("startY",85)); add(startY);
        add(text("Swipe end (% from top)",14,false)); endY = field("10", ""+prefs.getInt("endY",10)); add(endY);
        add(text("Wait after swipe (milliseconds)",14,false)); waitMs = field("1200", ""+prefs.getInt("waitMs",1200)); add(waitMs);
        add(text("Default swipe from 85% to 10% covers 75% of screen height. Adjust the endpoints to calibrate your phone; a 90% swipe requires endpoints 90 percentage points apart.", 13, false));
        add(text("Permissions",19,true));
        add(button("1. Enable Accessibility Service", () -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))));
        add(button("2. Allow display over other apps", () -> { if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()))); else Toast.makeText(this,"Overlay permission is already allowed",Toast.LENGTH_SHORT).show(); }));
        add(button("Save settings", () -> save()));
        add(button("Open floating controls", () -> { save(); if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) { Toast.makeText(this,"Allow display over other apps first",Toast.LENGTH_LONG).show(); return; } startService(new Intent(this,AutoClickService.class).setAction("SHOW_CONTROLS")); Toast.makeText(this,"If the service is enabled, floating controls will appear",Toast.LENGTH_LONG).show(); }));
        pointsLabel = text("Tap points: " + (prefs.getString("points","").isEmpty()?"none saved":prefs.getString("points","").split(";").length+" saved"),15,true); add(pointsLabel);
        add(button("Clear saved tap points", () -> { prefs.edit().putString("points","").apply(); pointsLabel.setText("Tap points: none saved"); }));
        add(text("How to use",19,true));
        add(text("Enable the service and overlay permission. Open your target game/app, use the floating Mark point control to add coordinates, then press Start. Stop or pause using the floating panel. Test on a non-sensitive screen first.",14,false));
    }
    private void save() {
        try {
            int c = Math.max(1, Integer.parseInt(cycles.getText().toString().trim()));
            int sy = Integer.parseInt(startY.getText().toString().trim()); int ey = Integer.parseInt(endY.getText().toString().trim());
            int w = Integer.parseInt(waitMs.getText().toString().trim());
            if (sy < 1 || sy > 100 || ey < 0 || ey >= sy || w < 0) throw new IllegalArgumentException();
            prefs.edit().putInt("cycles",c).putInt("startY",sy).putInt("endY",ey).putInt("waitMs",w).apply();
            Toast.makeText(this,"Settings saved",Toast.LENGTH_SHORT).show();
        } catch (Exception ex) { Toast.makeText(this,"Check values: start must be below 100, end must be above 0 and above the bottom of the swipe",Toast.LENGTH_LONG).show(); }
    }
    @Override protected void onResume() { super.onResume(); if (pointsLabel != null) { String p=prefs.getString("points",""); pointsLabel.setText("Tap points: "+(p.isEmpty()?"none saved":p.split(";").length+" saved")); } }
}
