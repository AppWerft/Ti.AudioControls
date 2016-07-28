package de.appwerft.audiocontrols;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

public class LockScreenService extends Service {
	final String LCAT = "LockAudioScreen 😇😇😇";
	WindowManager.LayoutParams layoutParams;
	ResultReceiver resultReceiver;
	private BroadcastReceiver lockScreenStateReceiver;
	private boolean isShowing = false;
	AudioControlWidget audiocontrolWidget;
	WindowManager windowManager;
	Context ctx;
	Resources res;
	String packageName;

	public LockScreenService() {
		super();
		ctx = TiApplication.getInstance().getApplicationContext();
		res = ctx.getResources();
		packageName = ctx.getPackageName();
		Log.d(LCAT, "CONSTRUCTOR	");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Log.d(LCAT, "inside service on start of onCreate");
		audiocontrolWidget = new AudioControlWidget(ctx);
		audiocontrolWidget.setCover("http://lorempixel.com/200/200/");
		// getting references to buttons:
		/*
		 * OnClickListener buttonListener = new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View clicksource) { int buttonId =
		 * clicksource.getId(); String msg = ""; if (buttonId ==
		 * rewindcontrolId) msg = "rewind"; if (buttonId == forwardcontrolId)
		 * msg = "forward"; if (buttonId == playcontrolId) msg = "play"; Bundle
		 * bundle = new Bundle(); bundle.putString("lockscreen", msg);
		 * resultReceiver.send(100, bundle); } };
		 * rewindButton.setOnClickListener(buttonListener);
		 * forwardButton.setOnClickListener(buttonListener);
		 * playButton.setOnClickListener(buttonListener);
		 */
		// http://stackoverflow.com/questions/19846541/what-is-windowmanager-in-android
		// adding to window stack:
		layoutParams = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);
		layoutParams.gravity = Gravity.BOTTOM;
		lockScreenStateReceiver = new LockScreenStateReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		registerReceiver(lockScreenStateReceiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		resultReceiver = intent.getParcelableExtra("receiver");
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
					windowManager.addView(audiocontrolWidget, layoutParams);
					isShowing = true;
				}
			}

			else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
				// Handle resuming events if user is present/screen is unlocked
				// remove the audiocontrolWidget immediately
				if (isShowing) {
					windowManager.removeViewImmediate(audiocontrolWidget);
					isShowing = false;
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		windowManager.removeView(audiocontrolWidget);
	}

}

//	