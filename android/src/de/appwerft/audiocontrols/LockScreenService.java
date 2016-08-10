package de.appwerft.audiocontrols;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiProperties;

import de.appwerft.audiocontrols.NotificationBigService.NotificationBigServiceReceiver;
import android.app.NotificationManager;
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
	final static String ACTION = "NotifyServiceAction";
	final static String SERVICE_COMMAND_KEY = "ServiceCommandKey";
	final static int RQS_STOP_SERVICE = 1;
	final static int RQS_REMOVE_NOTIFICATION = 2;
	LockscreenServiceReceiver lockscreenServiceReceiver;
	boolean widgetVisible = false;
	final String LCAT = "LockAudioScreen 📌📌";
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
		widgetVisible = true;
		lockscreenServiceReceiver = new LockscreenServiceReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION);
		ctx.registerReceiver(lockscreenServiceReceiver, filter);

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
		IntentFilter mfilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		mfilter.addAction(Intent.ACTION_USER_PRESENT);
		ctx.registerReceiver(lockScreenStateReceiver, mfilter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			Log.d(LCAT, "onStartCommand ");
			audioControlWidget.updateContent(intent.getExtras());
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
			Log.d(LCAT, "LockScreenStateReceiver received event");
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Log.d(LCAT, "LockScreenStateReceiver ACTION_SCREEN_OFF");
				// if screen is turn off show the controlview
				if (!isShowing) {
					Log.d(LCAT, "LockScreenStateReceiver  !isShowing");
					windowManager.addView(audioControlWidget, layoutParams);
					isShowing = true;
				}
			} else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
				Log.d(LCAT, "LockScreenStateReceiver ACTION_USER_PRESENT");
				if (isShowing) {
					windowManager.removeViewImmediate(audioControlWidget);
					isShowing = false;
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopSelf();
		ctx.unregisterReceiver(lockScreenStateReceiver);
	}

	public class LockscreenServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			int rqs = intent.getIntExtra(SERVICE_COMMAND_KEY, 0);
			Log.d(LCAT, ctx.getClass().getCanonicalName());
			Log.d(LCAT, SERVICE_COMMAND_KEY + "===============>" + rqs);
			if (rqs == RQS_STOP_SERVICE) {
				Log.d(LCAT, "STOP_SERVICE_BROADCAST_KEY received");
				stopSelf();
			}
			if (rqs == RQS_REMOVE_NOTIFICATION) {
				Log.d(LCAT, "RQS_REMOVE_NOTIFICATION received");
				if (isShowing) {
					windowManager.removeView(audioControlWidget);
					isShowing = false;
				}
				// windowManager.removeView(audioControlWidget);
				// ctx.unregisterReceiver(lockScreenStateReceiver);
			}
		}
	}
}