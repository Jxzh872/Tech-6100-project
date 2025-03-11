package JHS.JoystickView;


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
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = true;
    public static WeakReference<Activity> previousOne;
    public static boolean dontPause;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mostCurrent = this;
		if (processBA == null) {
			processBA = new BA(this.getApplicationContext(), null, null, "JHS.JoystickView", "JHS.JoystickView.main");
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
		activityBA = new BA(this, layout, processBA, "JHS.JoystickView", "JHS.JoystickView.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "JHS.JoystickView.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
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
public joystickviewwrapper.joystickViewWrapper _jsv1 = null;
public anywheresoftware.b4a.objects.LabelWrapper _l4 = null;
public anywheresoftware.b4a.objects.LabelWrapper _l5 = null;
public anywheresoftware.b4a.objects.LabelWrapper _l6 = null;
public anywheresoftware.b4a.objects.EditTextWrapper _txtrpiip = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btnsetip = null;
public anywheresoftware.b4a.objects.EditTextWrapper _udpport = null;
public JHS.JoystickView.starter _starter = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 34;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 35;BA.debugLine="Activity.LoadLayout(\"main\")";
mostCurrent._activity.LoadLayout("main",mostCurrent.activityBA);
 //BA.debugLineNum = 37;BA.debugLine="jsv1.ButtonColor = Colors.White";
mostCurrent._jsv1.setButtonColor(anywheresoftware.b4a.keywords.Common.Colors.White);
 //BA.debugLineNum = 38;BA.debugLine="jsv1.MainCircleColor = Colors.Black";
mostCurrent._jsv1.setMainCircleColor(anywheresoftware.b4a.keywords.Common.Colors.Black);
 //BA.debugLineNum = 39;BA.debugLine="jsv1.SecondaryCircleColor = Colors.Gray";
mostCurrent._jsv1.setSecondaryCircleColor(anywheresoftware.b4a.keywords.Common.Colors.Gray);
 //BA.debugLineNum = 40;BA.debugLine="jsv1.SecondaryCircleStrokeWidth = 3";
mostCurrent._jsv1.setSecondaryCircleStrokeWidth((int) (3));
 //BA.debugLineNum = 41;BA.debugLine="jsv1.HorizontalLineColor = Colors.Green";
mostCurrent._jsv1.setHorizontalLineColor(anywheresoftware.b4a.keywords.Common.Colors.Green);
 //BA.debugLineNum = 42;BA.debugLine="jsv1.HorizontalLineStrokeWidth = 4";
mostCurrent._jsv1.setHorizontalLineStrokeWidth((int) (4));
 //BA.debugLineNum = 43;BA.debugLine="jsv1.VerticalLineColor = Colors.Green";
mostCurrent._jsv1.setVerticalLineColor(anywheresoftware.b4a.keywords.Common.Colors.Green);
 //BA.debugLineNum = 44;BA.debugLine="jsv1.VerticalLineStrokeWidth = 8";
mostCurrent._jsv1.setVerticalLineStrokeWidth((int) (8));
 //BA.debugLineNum = 46;BA.debugLine="If FirstTime Then";
if (_firsttime) { 
 //BA.debugLineNum = 47;BA.debugLine="UDP.Initialize(\"UDP\", 0, 8000)";
_udp.Initialize(processBA,"UDP",(int) (0),(int) (8000));
 };
 //BA.debugLineNum = 50;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 56;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 58;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 52;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 54;BA.debugLine="End Sub";
return "";
}
public static String  _btnsetip_click() throws Exception{
 //BA.debugLineNum = 81;BA.debugLine="Sub btnSetIP_Click";
 //BA.debugLineNum = 82;BA.debugLine="RPiIP = txtRPiIP.Text";
_rpiip = mostCurrent._txtrpiip.getText();
 //BA.debugLineNum = 83;BA.debugLine="RPiPort = udpPort.Text";
_rpiport = (int)(Double.parseDouble(mostCurrent._udpport.getText()));
 //BA.debugLineNum = 84;BA.debugLine="End Sub";
return "";
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 23;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 25;BA.debugLine="Private jsv1 As JoystickView";
mostCurrent._jsv1 = new joystickviewwrapper.joystickViewWrapper();
 //BA.debugLineNum = 26;BA.debugLine="Private l4 As Label";
mostCurrent._l4 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 27;BA.debugLine="Private l5 As Label";
mostCurrent._l5 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 28;BA.debugLine="Private l6 As Label";
mostCurrent._l6 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 29;BA.debugLine="Private txtRPiIP As EditText";
mostCurrent._txtrpiip = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 30;BA.debugLine="Private btnSetIP As Button";
mostCurrent._btnsetip = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 31;BA.debugLine="Private udpPort As EditText";
mostCurrent._udpport = new anywheresoftware.b4a.objects.EditTextWrapper();
 //BA.debugLineNum = 32;BA.debugLine="End Sub";
return "";
}
public static String  _jsv1_value_changed(int _angle,int _powr,int _direc) throws Exception{
int _mapped_angle = 0;
String _msg = "";
anywheresoftware.b4a.objects.SocketWrapper.UDPSocket.UDPPacket _packet = null;
 //BA.debugLineNum = 68;BA.debugLine="Sub jsv1_value_changed(angle As Int, powr As Int,";
 //BA.debugLineNum = 69;BA.debugLine="Dim mapped_angle As Int = MapAngle(angle) ' Corre";
_mapped_angle = _mapangle(_angle);
 //BA.debugLineNum = 71;BA.debugLine="l4.Text = mapped_angle";
mostCurrent._l4.setText(BA.ObjectToCharSequence(_mapped_angle));
 //BA.debugLineNum = 72;BA.debugLine="l5.Text = powr";
mostCurrent._l5.setText(BA.ObjectToCharSequence(_powr));
 //BA.debugLineNum = 73;BA.debugLine="l6.Text = direc";
mostCurrent._l6.setText(BA.ObjectToCharSequence(_direc));
 //BA.debugLineNum = 75;BA.debugLine="Dim msg As String = mapped_angle & \",\" & powr";
_msg = BA.NumberToString(_mapped_angle)+","+BA.NumberToString(_powr);
 //BA.debugLineNum = 76;BA.debugLine="Dim packet As UDPPacket";
_packet = new anywheresoftware.b4a.objects.SocketWrapper.UDPSocket.UDPPacket();
 //BA.debugLineNum = 77;BA.debugLine="packet.Initialize(msg.GetBytes(\"UTF8\"), RPiIP, RP";
_packet.Initialize(_msg.getBytes("UTF8"),_rpiip,_rpiport);
 //BA.debugLineNum = 78;BA.debugLine="UDP.Send(packet)";
_udp.Send(_packet);
 //BA.debugLineNum = 79;BA.debugLine="End Sub";
return "";
}
public static int  _mapangle(int _angle) throws Exception{
 //BA.debugLineNum = 60;BA.debugLine="Sub MapAngle(angle As Int) As Int";
 //BA.debugLineNum = 61;BA.debugLine="If angle < 0 Then";
if (_angle<0) { 
 //BA.debugLineNum = 62;BA.debugLine="Return 360 + angle";
if (true) return (int) (360+_angle);
 }else {
 //BA.debugLineNum = 64;BA.debugLine="Return angle";
if (true) return _angle;
 };
 //BA.debugLineNum = 66;BA.debugLine="End Sub";
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
 //BA.debugLineNum = 16;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 18;BA.debugLine="Dim UDP As UDPSocket";
_udp = new anywheresoftware.b4a.objects.SocketWrapper.UDPSocket();
 //BA.debugLineNum = 19;BA.debugLine="Dim RPiIP As String = \"192.168.1.100\" ' Default R";
_rpiip = "192.168.1.100";
 //BA.debugLineNum = 20;BA.debugLine="Dim RPiPort As Int = 5000 ' Set your target port";
_rpiport = (int) (5000);
 //BA.debugLineNum = 21;BA.debugLine="End Sub";
return "";
}
}
