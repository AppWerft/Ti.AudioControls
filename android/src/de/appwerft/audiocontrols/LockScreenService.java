package de.appwerft.audiocontrols;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiProperties;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.view.Gravity;
import android.view.WindowManager;

public class LockScreenService extends Service {
	final String LCAT = "LockAudioScreen 😇😇😇";
	WindowManager.LayoutParams layoutParams;
	ResultReceiver resultReceiver;
	private TiProperties appProperties;
	private BroadcastReceiver lockScreenStateReceiver;
	private boolean isShowing = false;
	AudioControlWidget audioControlWidget;

	WindowManager windowManager;
	Context ctx;
	Resources res;
	String packageName;

	public LockScreenService() {
		super();
		ctx = TiApplication.getInstance().getApplicationContext();
		res = ctx.getResources();
		packageName = ctx.getPackageName();
		Log.d(LCAT, "LockscreenService constructed");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(LCAT, "LockscreenService created");
		audioControlWidget = new AudioControlWidget(ctx);
		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		final int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_FULLSCREEN
				| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
				| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		final int type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		final int HEIGHT = 155;
		layoutParams = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.FILL_PARENT, HEIGHT, type, flags,
				PixelFormat.TRANSLUCENT);
		layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

		appProperties = TiApplication.getInstance().getAppProperties();
		String verticalAlign = appProperties.getString(
				"PLAYER_VERTICAL_POSITION", "BOTTOM");
		layoutParams.gravity = (verticalAlign == "TOP") ? Gravity.TOP
				: Gravity.BOTTOM;
		layoutParams.alpha = 0.95f;
		lockScreenStateReceiver = new LockScreenStateReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		ctx.registerReceiver(lockScreenStateReceiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			Log.d(LCAT, "onStartCommand ");
			audioControlWidget.updateContent(intent.getStringExtra("image"),
					intent.getStringExtra("title"),
					intent.getStringExtra("artist"));
		}
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public class LockScreenStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				// if screen is turn off show the controlview
				if (!isShowing) {
					windowManager.addView(audioControlWidget, layoutParams);
					isShowing = true;
				}
			} else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
				if (isShowing) {
					windowManager.removeViewImmediate(audioControlWidget);
					isShowing = false;
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		windowManager.removeView(audioControlWidget);
		ctx.unregisterReceiver(lockScreenStateReceiver);
	}
}

//	