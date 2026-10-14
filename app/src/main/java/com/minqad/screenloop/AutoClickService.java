package com.minqad.screenloop;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

public class AutoClickService extends AccessibilityService {
    private WindowManager wm; private View panel; private Handler handler;
    private SharedPreferences prefs; private boolean running=false, paused=false, marking=false;
    private int cycle=0, pointIndex=0, targetCycles=100, screenW, screenH;
    private final List<float[]> points = new ArrayList<>();
    private TextView status;
    private Runnable step;
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    @Override public void onServiceConnected(){ super.onServiceConnected(); prefs=getSharedPreferences("screenloop",MODE_PRIVATE); wm=(WindowManager)getSystemService(WINDOW_SERVICE); handler=new Handler(Looper.getMainLooper()); loadPoints(); showPanel(); }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt(){stopRun();}
    @Override public int onStartCommand(android.content.Intent intent, int flags, int startId){ if(handler!=null) handler.post(this::showPanel); return START_STICKY; }
    @Override public boolean onUnbind(android.content.Intent intent){stopRun(); removePanel(); return super.onUnbind(intent);}
    private void loadPoints(){ points.clear(); String raw=prefs.getString("points",""); if(raw.isEmpty())return; for(String item:raw.split(";")){try{String[] xy=item.split(",");points.add(new float[]{Float.parseFloat(xy[0]),Float.parseFloat(xy[1])});}catch(Exception ignored){}} }
    private WindowManager.LayoutParams params(int type){ int t=(Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE); return new WindowManager.LayoutParams(-2,-2,t,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT); }
    private void showPanel(){ if(wm==null||panel!=null)return; LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(10),dp(8),dp(10),dp(8)); box.setBackgroundColor(Color.argb(235,25,35,55));
        TextView title=new TextView(this);title.setText("ScreenLoop • drag to move");title.setTextColor(Color.WHITE);title.setTypeface(null,Typeface.BOLD);box.addView(title);
        status=new TextView(this);status.setTextColor(Color.WHITE);status.setText("Ready • points: "+points.size());box.addView(status);
        addBtn(box,"Mark point",()->startMark()); addBtn(box,"Test swipe",()->testSwipe()); addBtn(box,"Start",()->startRun()); addBtn(box,"Pause / Resume",()->{if(running){paused=!paused;if(!paused)scheduleStep(100);updateStatus();}}); addBtn(box,"Stop",()->stopRun()); addBtn(box,"Hide",()->removePanel());
        title.setOnTouchListener(new View.OnTouchListener(){float sx,sy;WindowManager.LayoutParams lp;public boolean onTouch(View v,MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){sx=e.getRawX();sy=e.getRawY();lp=(WindowManager.LayoutParams)panel.getLayoutParams();return true;}if(e.getAction()==MotionEvent.ACTION_MOVE){lp.x+=(int)(e.getRawX()-sx);lp.y+=(int)(e.getRawY()-sy);sx=e.getRawX();sy=e.getRawY();wm.updateViewLayout(panel,lp);return true;}return e.getAction()==MotionEvent.ACTION_UP;}});
        panel=box; WindowManager.LayoutParams lp=params(0);lp.gravity=Gravity.TOP|Gravity.LEFT;lp.x=dp(12);lp.y=dp(100);try{wm.addView(panel,lp);}catch(Exception ex){panel=null;}}
    private void addBtn(LinearLayout box,String label,Runnable r){Button b=new Button(this);b.setText(label);b.setTextSize(12);b.setAllCaps(false);b.setOnClickListener(v->r.run());box.addView(b,new LinearLayout.LayoutParams(dp(190),dp(42)));}
    private void updateStatus(){if(status!=null)status.setText((running?(paused?"Paused":"Running"):"Ready")+" • cycle "+cycle+"/"+targetCycles+" • points "+points.size());}
    private void removePanel(){if(panel!=null){try{wm.removeView(panel);}catch(Exception ignored){}panel=null;status=null;}}
    private void startMark(){ if(marking)return; marking=true; final FrameLayout layer=new FrameLayout(this); layer.setBackgroundColor(Color.argb(35,30,120,255)); TextView instruction=new TextView(this);instruction.setText("Tap the desired point to save it • tap here to cancel");instruction.setTextColor(Color.WHITE);instruction.setBackgroundColor(Color.argb(220,20,30,50));instruction.setPadding(dp(14),dp(14),dp(14),dp(14));FrameLayout.LayoutParams ilp=new FrameLayout.LayoutParams(-1,-2,Gravity.TOP);layer.addView(instruction,ilp);
        layer.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_UP){float x=e.getRawX(),y=e.getRawY();if(y<dp(70)){marking=false;try{wm.removeView(layer);}catch(Exception ignored){}return true;}points.add(new float[]{x,y});StringBuilder s=new StringBuilder();for(float[] p:points){if(s.length()>0)s.append(';');s.append(p[0]).append(',').append(p[1]);}prefs.edit().putString("points",s.toString()).apply();marking=false;try{wm.removeView(layer);}catch(Exception ignored){}updateStatus();return true;}return true;});
        WindowManager.LayoutParams lp=params(0);lp.width=-1;lp.height=-1;lp.gravity=Gravity.TOP|Gravity.LEFT;lp.flags=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;try{wm.addView(layer,lp);}catch(Exception ex){marking=false;}}
    private int[] screen(){android.util.DisplayMetrics dm=new android.util.DisplayMetrics();if(Build.VERSION.SDK_INT>=30){android.view.WindowMetrics metrics=wm.getCurrentWindowMetrics();Rect b=metrics.getBounds();return new int[]{b.width(),b.height()};}wm.getDefaultDisplay().getRealMetrics(dm);return new int[]{dm.widthPixels,dm.heightPixels};}
    private void testSwipe(){int[] wh=screen();int h=wh[1];float sy=prefs.getInt("startY",85)/100f*h,ey=prefs.getInt("endY",10)/100f*h;doSwipe(wh[0]/2f,sy,wh[0]/2f,ey,500,()->{if(status!=null)status.setText("Test swipe complete • points "+points.size());});}
    private void startRun(){loadPoints();if(points.isEmpty()){if(status!=null)status.setText("Add at least one tap point first");return;}if(running){paused=false;scheduleStep(50);return;}targetCycles=Math.max(1,prefs.getInt("cycles",100));cycle=0;pointIndex=0;running=true;paused=false;updateStatus();step=()->runStep();scheduleStep(150);}
    private void stopRun(){running=false;paused=false;if(handler!=null&&step!=null)handler.removeCallbacks(step);updateStatus();}
    private void scheduleStep(long delay){if(!running||paused)return;if(step!=null)handler.removeCallbacks(step);handler.postDelayed(step,Math.max(0,delay));}
    private void runStep(){if(!running||paused)return; if(cycle>=targetCycles){stopRun();if(status!=null)status.setText("Completed "+cycle+" cycles");return;}
        if(pointIndex<points.size()){float[] p=points.get(pointIndex++);doTap(p[0],p[1],()->scheduleStep(150));return;}
        int[] wh=screen();float sy=prefs.getInt("startY",85)/100f*wh[1],ey=prefs.getInt("endY",10)/100f*wh[1];cycle++;pointIndex=0;updateStatus();doSwipe(wh[0]/2f,sy,wh[0]/2f,ey,450,()->scheduleStep(prefs.getInt("waitMs",1200)));
    }
    private void doTap(float x,float y,Runnable done){Path path=new Path();path.moveTo(x,y);GestureDescription.StrokeDescription stroke=new GestureDescription.StrokeDescription(path,0,70);GestureDescription.Builder b=new GestureDescription.Builder().addStroke(stroke);dispatchGesture(b.build(),new GestureResultCallback(){@Override public void onCompleted(GestureDescription g){if(done!=null)done.run();}@Override public void onCancelled(GestureDescription g){if(done!=null)done.run();}},handler);}
    private void doSwipe(float x1,float y1,float x2,float y2,long duration,Runnable done){Path path=new Path();path.moveTo(x1,y1);path.lineTo(x2,y2);GestureDescription g=new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path,0,duration)).build();dispatchGesture(g,new GestureResultCallback(){@Override public void onCompleted(GestureDescription g){if(done!=null)done.run();}@Override public void onCancelled(GestureDescription g){if(done!=null)done.run();}},handler);}
}
