package tech.swiper.v1;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = true;
	public static final boolean includeTitle = true;
    public static WeakReference<Activity> previousOne;
    public static boolean dontPause;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mostCurrent = this;
		if (processBA == null) {
			processBA = new BA(this.getApplicationContext(), null, null, "tech.swiper.v1", "tech.swiper.v1.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.setActivityPaused(true);
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(this, processBA, wl, false))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "tech.swiper.v1", "tech.swiper.v1.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "tech.swiper.v1.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create " + (isFirst ? "(first time)" : "") + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEventFromUI(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeydown", this, new Object[] {keyCode, event}))
            return true;
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeyup", this, new Object[] {keyCode, event}))
            return true;
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null)
            return;
        if (this != mostCurrent)
			return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        if (!dontPause)
            BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        else
            BA.LogInfo("** Activity (main) Pause event (activity is not paused). **");
        if (mostCurrent != null)
            processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        if (!dontPause) {
            processBA.setActivityPaused(true);
            mostCurrent = null;
        }

        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
            main mc = mostCurrent;
			if (mc == null || mc != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
            if (mc != mostCurrent)
                return;
		    processBA.raiseEvent(mc._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        for (int i = 0;i < permissions.length;i++) {
            Object[] o = new Object[] {permissions[i], grantResults[i] == 0};
            processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, o);
        }
            
    }

public anywheresoftware.b4a.keywords.Common __c = null;
public static anywheresoftware.b4a.objects.SocketWrapper.UDPSocket _udp = null;
public static String _rpiip = "";
public static int _rpiport = 0;
public static anywheresoftware.b4a.objects.Timer _tmrlift = null;
public static anywheresoftware.b4a.objects.Timer _tmrrotate = null;
public static String _currentliftcommand = "";
public static boolean _islifting = false;
public static boolean _isrotating = false;
public joystickviewwrapper.joystickViewWrapper _jsv1 = null;
public anywheresoftware.b4a.objects.LabelWrapper _l4 = null;
public anywheresoftware.b4a.objects.LabelWrapper _l5 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtrpiip = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btnsetip = null;
public anywheresoftware.b4a.objects.EditTextWrapper _udpport = null;
public anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper _togglegripper = null;
public static int _rot_value = 0;
public anywheresoftware.b4a.objects.WebViewWrapper _camerastreamview = null;
public anywheresoftware.b4a.objects.ButtonWrapper _reload_web = null;
public tech.swiper.v1.starter _starter = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 41;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 42;BA.debugLine="Activity.LoadLayout(\"main\")";
mostCurrent._activity.LoadLayout("main",mostCurrent.activityBA);
 //BA.debugLineNum = 45;BA.debugLine="If toggleGripper.Checked Then";
if (mostCurrent._togglegripper.getChecked()) { 
 //BA.debugLineNum = 46;BA.debugLine="toggleGripper.Background = CreateCustomDrawable(";
mostCurrent._togglegripper.setBackground((android.graphics.drawable.Drawable)(_createcustomdrawable(((int)0xffffea8b)).getObject()));
 }else {
 //BA.debugLineNum = 48;BA.debugLine="toggleGripper.Background = CreateCustomDrawable(";
mostCurrent._togglegripper.setBackground((android.graphics.drawable.Drawable)(_createcustomdrawable(((int)0xff0644a5)).getObject()));
 };
 //BA.debugLineNum = 52;BA.debugLine="jsv1.ButtonColor = 0xFFFFEA8B";
mostCurrent._jsv1.setButtonColor(((int)0xffffea8b));
 //BA.debugLineNum = 53;BA.debugLine="jsv1.MainCircleColor = Colors.Black";
mostCurrent._jsv1.setMainCircleColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 //BA.debugLineNum = 54;BA.debugLine="jsv1.SecondaryCircleColor = 0xFFB35513";
mostCurrent._jsv1.setSecondaryCircleColor(((int)0xffb35513));
 //BA.debugLineNum = 55;BA.debugLine="jsv1.SecondaryCircleStrokeWidth = 3";
mostCurrent._jsv1.setSecondaryCircleStrokeWidth((int) (3));
 //BA.debugLineNum = 56;BA.debugLine="jsv1.HorizontalLineColor = 0xFFB35513";
mostCurrent._jsv1.setHorizontalLineColor(((int)0xffb35513));
 //BA.debugLineNum = 57;BA.debugLine="jsv1.HorizontalLineStrokeWidth = 5";
mostCurrent._jsv1.setHorizontalLineStrokeWidth((int) (5));
 //BA.debugLineNum = 58;BA.debugLine="jsv1.VerticalLineColor = 0xFFB35513";
mostCurrent._jsv1.setVerticalLineColor(((int)0xffb35513));
 //BA.debugLineNum = 59;BA.debugLine="jsv1.VerticalLineStrokeWidth = 5";
mostCurrent._jsv1.setVerticalLineStrokeWidth((int) (5));
 //BA.debugLineNum = 61;BA.debugLine="If FirstTime Then";
if (_firsttime) { 
 //BA.debugLineNum = 62;BA.debugLine="UDP.Initialize(\"UDP\", 0, 8000)";
_udp.Initialize(processBA,"UDP",(int) (0),(int) (8000));
 //BA.debugLineNum = 63;BA.debugLine="tmrLift.Initialize(\"tmrLift\", 200) ' 100ms inter";
_tmrlift.Initialize(processBA,"tmrLift",(long) (200));
 //BA.debugLineNum = 64;BA.debugLine="tmrRotate.Initialize(\"tmrRotate\", 100) ' 100ms i";
_tmrrotate.Initialize(processBA,"tmrRotate",(long) (100));
 //BA.debugLineNum = 65;BA.debugLine="tmrLift.Enabled = False";
_tmrlift.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 66;BA.debugLine="tmrRotate.Enabled = False";
_tmrrotate.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 };
 //BA.debugLineNum = 68;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 162;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 163;BA.debugLine="tmrLift.Enabled = False";
_tmrlift.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 164;BA.debugLine="tmrRotate.Enabled = False";
_tmrrotate.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 165;BA.debugLine="currentLiftCommand = \"\"";
_currentliftcommand = "";
 //BA.debugLineNum = 166;BA.debugLine="isLifting = False";
_islifting = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 167;BA.debugLine="isRotating = False";
_isrotating = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 168;BA.debugLine="End Sub";
return "";
}
public static String  _btnliftdown_down() throws Exception{
 //BA.debugLineNum = 138;BA.debugLine="Sub btnLiftDown_Down";
 //BA.debugLineNum = 139;BA.debugLine="If isLifting Then Return ' If rotating is in prog";
if (_islifting) { 
if (true) return "";};
 //BA.debugLineNum = 140;BA.debugLine="currentLiftCommand = \"Lift Down\"";
_currentliftcommand = "Lift Down";
 //BA.debugLineNum = 141;BA.debugLine="SendUDP(currentLiftCommand) ' Send immediately on";
_sendudp(_currentliftcommand);
 //BA.debugLineNum = 142;BA.debugLine="tmrLift.Enabled = True";
_tmrlift.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 143;BA.debugLine="isLifting = True ' Set the lifting flag to true";
_islifting = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 144;BA.debugLine="End Sub";
return "";
}
public static String  _btnliftdown_up() throws Exception{
 //BA.debugLineNum = 146;BA.debugLine="Sub btnLiftDown_Up";
 //BA.debugLineNum = 147;BA.debugLine="currentLiftCommand = \"\"";
_currentliftcommand = "";
 //BA.debugLineNum = 148;BA.debugLine="tmrLift.Enabled = False";
_tmrlift.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 149;BA.debugLine="isLifting = False ' Set the lifting flag to false";
_islifting = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 150;BA.debugLine="End Sub";
return "";
}
public static String  _btnliftup_down() throws Exception{
 //BA.debugLineNum = 124;BA.debugLine="Sub btnLiftUp_Down";
 //BA.debugLineNum = 125;BA.debugLine="If isLifting Then Return ' If rotating is in prog";
if (_islifting) { 
if (true) return "";};
 //BA.debugLineNum = 126;BA.debugLine="currentLiftCommand = \"Lift Up\"";
_currentliftcommand = "Lift Up";
 //BA.debugLineNum = 127;BA.debugLine="SendUDP(currentLiftCommand) ' Send immediately on";
_sendudp(_currentliftcommand);
 //BA.debugLineNum = 128;BA.debugLine="tmrLift.Enabled = True";
_tmrlift.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 129;BA.debugLine="isLifting = True ' Set the lifting flag to true";
_islifting = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 130;BA.debugLine="End Sub";
return "";
}
public static String  _btnliftup_up() throws Exception{
 //BA.debugLineNum = 132;BA.debugLine="Sub btnLiftUp_Up";
 //BA.debugLineNum = 133;BA.debugLine="currentLiftCommand = \"\"";
_currentliftcommand = "";
 //BA.debugLineNum = 134;BA.debugLine="tmrLift.Enabled = False";
_tmrlift.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 135;BA.debugLine="isLifting = False ' Set the lifting flag to false";
_islifting = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 136;BA.debugLine="End Sub";
return "";
}
public static String  _btnsetip_click() throws Exception{
 //BA.debugLineNum = 178;BA.debugLine="Sub btnSetIP_Click";
 //BA.debugLineNum = 179;BA.debugLine="RPiIP = txtRPiIP.Text";
_rpiip = mostCurrent._txtrpiip.getText();
 //BA.debugLineNum = 180;BA.debugLine="Try";
try { //BA.debugLineNum = 181;BA.debugLine="RPiPort = udpPort.Text";
_rpiport = (int)(Double.parseDouble(mostCurrent._udpport.getText()));
 } 
       catch (Exception e5) {
			processBA.setLastException(e5); //BA.debugLineNum = 183;BA.debugLine="RPiPort = 5000";
_rpiport = (int) (5000);
 };
 //BA.debugLineNum = 185;BA.debugLine="cameraStreamView.JavaScriptEnabled = True";
mostCurrent._camerastreamview.setJavaScriptEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 186;BA.debugLine="cameraStreamView.LoadUrl(\"http://\" & RPiIP & \":80";
mostCurrent._camerastreamview.LoadUrl("http://"+_rpiip+":8080/?action=stream");
 //BA.debugLineNum = 187;BA.debugLine="End Sub";
return "";
}
public static anywheresoftware.b4a.objects.drawable.ColorDrawable  _createcustomdrawable(int _color) throws Exception{
anywheresoftware.b4a.objects.drawable.ColorDrawable _cd = null;
 //BA.debugLineNum = 71;BA.debugLine="Sub CreateCustomDrawable(color As Int) As ColorDra";
 //BA.debugLineNum = 72;BA.debugLine="Dim cd As ColorDrawable";
_cd = new anywheresoftware.b4a.objects.drawable.ColorDrawable();
 //BA.debugLineNum = 73;BA.debugLine="cd.Initialize(color, 20dip) ' Color and corner ra";
_cd.Initialize(_color,anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (20)));
 //BA.debugLineNum = 74;BA.debugLine="Return cd";
if (true) return _cd;
 //BA.debugLineNum = 75;BA.debugLine="End Sub";
return null;
}
public static String  _getcurrentjoystickudp() throws Exception{
int _angle = 0;
int _cont_power = 0;
 //BA.debugLineNum = 197;BA.debugLine="Sub GetCurrentJoystickUDP As String";
 //BA.debugLineNum = 198;BA.debugLine="Dim angle As Int";
_angle = 0;
 //BA.debugLineNum = 199;BA.debugLine="Dim cont_Power As Int";
_cont_power = 0;
 //BA.debugLineNum = 201;BA.debugLine="Try";
try { //BA.debugLineNum = 202;BA.debugLine="angle = l4.Text.Trim";
_angle = (int)(Double.parseDouble(mostCurrent._l4.getText().trim()));
 //BA.debugLineNum = 203;BA.debugLine="cont_Power = l5.Text.Trim";
_cont_power = (int)(Double.parseDouble(mostCurrent._l5.getText().trim()));
 //BA.debugLineNum = 204;BA.debugLine="angle = angle.As(Int)";
_angle = (_angle);
 //BA.debugLineNum = 205;BA.debugLine="cont_Power = cont_Power.As(Int)";
_cont_power = (_cont_power);
 } 
       catch (Exception e9) {
			processBA.setLastException(e9); //BA.debugLineNum = 207;BA.debugLine="angle = 0";
_angle = (int) (0);
 //BA.debugLineNum = 208;BA.debugLine="cont_Power = 0";
_cont_power = (int) (0);
 };
 //BA.debugLineNum = 210;BA.debugLine="Return angle & \",\" & cont_Power & \",\" & rot_value";
if (true) return BA.NumberToString(_angle)+","+BA.NumberToString(_cont_power)+","+BA.NumberToString(_rot_value);
 //BA.debugLineNum = 211;BA.debugLine="End Sub";
return "";
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 28;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 29;BA.debugLine="Private jsv1 As JoystickView";
mostCurrent._jsv1 = new joystickviewwrapper.joystickViewWrapper();
 //BA.debugLineNum = 30;BA.debugLine="Private l4 As Label";
mostCurrent._l4 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 31;BA.debugLine="Private l5 As Label";
mostCurrent._l5 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 32;BA.debugLine="Private txtRPiIP As EditText";
mostCurrent._txtrpiip = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 33;BA.debugLine="Private btnSetIP As Button";
mostCurrent._btnsetip = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 34;BA.debugLine="Private udpPort As EditText";
mostCurrent._udpport = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 35;BA.debugLine="Private toggleGripper As ToggleButton";
mostCurrent._togglegripper = new anywheresoftware.b4a.objects.CompoundButtonWrapper.ToggleButtonWrapper();
 //BA.debugLineNum = 36;BA.debugLine="Private rot_value As Int = 0";
_rot_value = (int) (0);
 //BA.debugLineNum = 37;BA.debugLine="Private cameraStreamView As WebView";
mostCurrent._camerastreamview = new anywheresoftware.b4a.objects.WebViewWrapper();
 //BA.debugLineNum = 38;BA.debugLine="Private reload_web As Button";
mostCurrent._reload_web = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 39;BA.debugLine="End Sub";
return "";
}
public static String  _jsv1_value_changed(int _angle,int _powr,int _direc) throws Exception{
int _mapped_angle = 0;
 //BA.debugLineNum = 78;BA.debugLine="Sub jsv1_value_changed(angle As Int, powr As Int,";
 //BA.debugLineNum = 79;BA.debugLine="Dim mapped_angle As Int";
_mapped_angle = 0;
 //BA.debugLineNum = 80;BA.debugLine="mapped_angle = MapAngle(angle)";
_mapped_angle = _mapangle(_angle);
 //BA.debugLineNum = 81;BA.debugLine="l4.Text = mapped_angle";
mostCurrent._l4.setText(BA.ObjectToCharSequence(_mapped_angle));
 //BA.debugLineNum = 82;BA.debugLine="l5.Text = powr";
mostCurrent._l5.setText(BA.ObjectToCharSequence(_powr));
 //BA.debugLineNum = 83;BA.debugLine="SendUDP(mapped_angle & \",\" & powr & \",\" & rot_val";
_sendudp(BA.NumberToString(_mapped_angle)+","+BA.NumberToString(_powr)+","+BA.NumberToString(_rot_value));
 //BA.debugLineNum = 84;BA.debugLine="End Sub";
return "";
}
public static String  _leftrot_down() throws Exception{
 //BA.debugLineNum = 94;BA.debugLine="Sub LeftRot_Down";
 //BA.debugLineNum = 95;BA.debugLine="If isRotating Then Return ' If lifting is in prog";
if (_isrotating) { 
if (true) return "";};
 //BA.debugLineNum = 96;BA.debugLine="rot_value = -50";
_rot_value = (int) (-50);
 //BA.debugLineNum = 97;BA.debugLine="SendUDP(GetCurrentJoystickUDP) ' Send immediately";
_sendudp(_getcurrentjoystickudp());
 //BA.debugLineNum = 98;BA.debugLine="tmrRotate.Enabled = True ' Start the timer when b";
_tmrrotate.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 99;BA.debugLine="isRotating = True ' Set the rotating flag to true";
_isrotating = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 100;BA.debugLine="End Sub";
return "";
}
public static String  _leftrot_up() throws Exception{
 //BA.debugLineNum = 102;BA.debugLine="Sub LeftRot_Up";
 //BA.debugLineNum = 103;BA.debugLine="rot_value = 0";
_rot_value = (int) (0);
 //BA.debugLineNum = 104;BA.debugLine="SendUDP(GetCurrentJoystickUDP)";
_sendudp(_getcurrentjoystickudp());
 //BA.debugLineNum = 105;BA.debugLine="tmrRotate.Enabled = False ' Stop the timer when b";
_tmrrotate.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 106;BA.debugLine="isRotating = False ' Set the rotating flag to fal";
_isrotating = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 107;BA.debugLine="End Sub";
return "";
}
public static int  _mapangle(int _angle) throws Exception{
 //BA.debugLineNum = 86;BA.debugLine="Sub MapAngle(angle As Int) As Int";
 //BA.debugLineNum = 87;BA.debugLine="If angle < 0 Then";
if (_angle<0) { 
 //BA.debugLineNum = 88;BA.debugLine="Return 360 + angle";
if (true) return (int) (360+_angle);
 }else {
 //BA.debugLineNum = 90;BA.debugLine="Return angle";
if (true) return _angle;
 };
 //BA.debugLineNum = 92;BA.debugLine="End Sub";
return 0;
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
starter._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 17;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 18;BA.debugLine="Dim UDP As UDPSocket";
_udp = new anywheresoftware.b4a.objects.SocketWrapper.UDPSocket();
 //BA.debugLineNum = 19;BA.debugLine="Dim RPiIP As String = \"192.168.0.116\"";
_rpiip = "192.168.0.116";
 //BA.debugLineNum = 20;BA.debugLine="Dim RPiPort As Int = 5000";
_rpiport = (int) (5000);
 //BA.debugLineNum = 21;BA.debugLine="Dim tmrLift As Timer";
_tmrlift = new anywheresoftware.b4a.objects.Timer();
 //BA.debugLineNum = 22;BA.debugLine="Dim tmrRotate As Timer";
_tmrrotate = new anywheresoftware.b4a.objects.Timer();
 //BA.debugLineNum = 23;BA.debugLine="Dim currentLiftCommand As String = \"\"";
_currentliftcommand = "";
 //BA.debugLineNum = 24;BA.debugLine="Dim isLifting As Boolean = False ' Flag to track";
_islifting = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 25;BA.debugLine="Dim isRotating As Boolean = False ' Flag to track";
_isrotating = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 26;BA.debugLine="End Sub";
return "";
}
public static String  _reload_web_click() throws Exception{
 //BA.debugLineNum = 214;BA.debugLine="Sub reload_web_Click";
 //BA.debugLineNum = 215;BA.debugLine="cameraStreamView.LoadUrl(\"http://\" & RPiIP & \":80";
mostCurrent._camerastreamview.LoadUrl("http://"+_rpiip+":8080/?action=stream");
 //BA.debugLineNum = 216;BA.debugLine="End Sub";
return "";
}
public static String  _rightrot_down() throws Exception{
 //BA.debugLineNum = 109;BA.debugLine="Sub Rightrot_Down";
 //BA.debugLineNum = 110;BA.debugLine="If isRotating Then Return ' If lifting is in prog";
if (_isrotating) { 
if (true) return "";};
 //BA.debugLineNum = 111;BA.debugLine="rot_value = 50";
_rot_value = (int) (50);
 //BA.debugLineNum = 112;BA.debugLine="SendUDP(GetCurrentJoystickUDP) ' Send immediately";
_sendudp(_getcurrentjoystickudp());
 //BA.debugLineNum = 113;BA.debugLine="tmrRotate.Enabled = True ' Start the timer when b";
_tmrrotate.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 114;BA.debugLine="isRotating = True ' Set the rotating flag to true";
_isrotating = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 115;BA.debugLine="End Sub";
return "";
}
public static String  _rightrot_up() throws Exception{
 //BA.debugLineNum = 117;BA.debugLine="Sub Rightrot_Up";
 //BA.debugLineNum = 118;BA.debugLine="rot_value = 0";
_rot_value = (int) (0);
 //BA.debugLineNum = 119;BA.debugLine="SendUDP(GetCurrentJoystickUDP)";
_sendudp(_getcurrentjoystickudp());
 //BA.debugLineNum = 120;BA.debugLine="tmrRotate.Enabled = False ' Stop the timer when b";
_tmrrotate.setEnabled(anywheresoftware.b4a.keywords.Common.False);
 //BA.debugLineNum = 121;BA.debugLine="isRotating = False ' Set the rotating flag to fal";
_isrotating = anywheresoftware.b4a.keywords.Common.False;
 //BA.debugLineNum = 122;BA.debugLine="End Sub";
return "";
}
public static String  _sendudp(String _msg) throws Exception{
anywheresoftware.b4a.objects.SocketWrapper.UDPSocket.UDPPacket _packet = null;
 //BA.debugLineNum = 189;BA.debugLine="Sub SendUDP(msg As String)";
 //BA.debugLineNum = 190;BA.debugLine="If UDP.IsInitialized = False Then Return";
if (_udp.IsInitialized()==anywheresoftware.b4a.keywords.Common.False) { 
if (true) return "";};
 //BA.debugLineNum = 191;BA.debugLine="Dim packet As UDPPacket";
_packet = new anywheresoftware.b4a.objects.SocketWrapper.UDPSocket.UDPPacket();
 //BA.debugLineNum = 192;BA.debugLine="packet.Initialize(msg.GetBytes(\"UTF8\"), RPiIP, RP";
_packet.Initialize(_msg.getBytes("UTF8"),_rpiip,_rpiport);
 //BA.debugLineNum = 193;BA.debugLine="UDP.Send(packet)";
_udp.Send(_packet);
 //BA.debugLineNum = 194;BA.debugLine="Log(\"UDP Sent: \" & msg)";
anywheresoftware.b4a.keywords.Common.LogImpl("21245189","UDP Sent: "+_msg,0);
 //BA.debugLineNum = 195;BA.debugLine="End Sub";
return "";
}
public static String  _tmrlift_tick() throws Exception{
 //BA.debugLineNum = 152;BA.debugLine="Sub tmrLift_Tick";
 //BA.debugLineNum = 153;BA.debugLine="If currentLiftCommand <> \"\" Then";
if ((_currentliftcommand).equals("") == false) { 
 //BA.debugLineNum = 154;BA.debugLine="SendUDP(currentLiftCommand) ' Send command durin";
_sendudp(_currentliftcommand);
 };
 //BA.debugLineNum = 156;BA.debugLine="End Sub";
return "";
}
public static String  _tmrrotate_tick() throws Exception{
 //BA.debugLineNum = 158;BA.debugLine="Sub tmrRotate_Tick";
 //BA.debugLineNum = 159;BA.debugLine="SendUDP(GetCurrentJoystickUDP) ' Continuously sen";
_sendudp(_getcurrentjoystickudp());
 //BA.debugLineNum = 160;BA.debugLine="End Sub";
return "";
}
public static String  _togglegripper_checkedchange(boolean _checked) throws Exception{
 //BA.debugLineNum = 170;BA.debugLine="Sub toggleGripper_CheckedChange(Checked As Boolean";
 //BA.debugLineNum = 171;BA.debugLine="If Checked Then";
if (_checked) { 
 //BA.debugLineNum = 172;BA.debugLine="SendUDP(\"Open Gripper\")";
_sendudp("Open Gripper");
 }else {
 //BA.debugLineNum = 174;BA.debugLine="SendUDP(\"Close Gripper\")";
_sendudp("Close Gripper");
 };
 //BA.debugLineNum = 176;BA.debugLine="End Sub";
return "";
}
}
