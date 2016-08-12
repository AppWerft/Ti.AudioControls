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

	LockscreenServiceReceiver lockscreenServiceReceiver;
	boolean widgetVisible = false;
	final String LCAT = "LockAudioScreen 📌📌";
	WindowManager.LayoutParams layoutParams;
	ResultReceiver resultReceiver;
	private TiProperties appProperties;
	private BroadcastReceiver lockScreenStateReceiver;
	private boolean isShowing = false, shouldVisible = true;
	AudioControlWidget audioControlWidget;
	WindowManager windowManager;
	Context ctx;

	public LockScreenService() {
		super();
		ctx = TiApplication.getInstance().getApplicationContext();
		appProperties = TiApplication.getInstance().getAppProperties();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		super.onCreate();
		widgetVisible = true;
		lockscreenServiceReceiver = new LockscreenServiceReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AudiocontrolsModule.ACTION);
		ctx.registerReceiver(lockscreenServiceReceiver, filter);

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		final int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_FULLSCREEN
				| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
				| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		final int type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		final int HEIGHT = 165;
		/* Building of layoutParams: */
		layoutParams = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.FILL_PARENT, HEIGHT, type, flags,
				PixelFormat.TRANSLUCENT);
		layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

		String verticalAlign = appProperties.getString(
				"PLAYER_VERTICAL_POSITION", "BOTTOM");
		layoutParams.gravity = (verticalAlign == "TOP") ? Gravity.TOP
				: Gravity.BOTTOM;
		layoutParams.alpha = 1.00f;

		audioControlWidget = new AudioControlWidget(ctx,
				new AudioControlWidget.onFlingListener() {
					@Override
					public void onFlinged(int direction) {
						if (direction == AudioControlWidget.DIRECTION_DOWN) {
							layoutParams.gravity = Gravity.BOTTOM;
							appProperties.setString("PLAYER_VERTICAL_POSITION",
									"BOTTOM");
						} else {
							layoutParams.gravity = Gravity.TOP;
							appProperties.setString("PLAYER_VERTICAL_POSITION",
									"TOP");
						}
						windowManager.updateViewLayout(audioControlWidget,
								layoutParams);
					}
				});
		/* Receiver for handling hide/view */
		lockScreenStateReceiver = new LockScreenStateReceiver();
		IntentFilter mfilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		mfilter.addAction(Intent.ACTION_USER_PRESENT);
		ctx.registerReceiver(lockScreenStateReceiver, mfilter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
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
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				if (!isShowing && shouldVisible) {
					Log.d(LCAT, "LockScreenStateReceiver  !isShowing");
					windowManager.addView(audioControlWidget, layoutParams);
					isShowing = true;
				}
			} else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
				if (isShowing && shouldVisible) {
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

	/* controling from AudiocontrolsModul: */
	public class LockscreenServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			int rqs = intent.getIntExtra(
					AudiocontrolsModule.SERVICE_COMMAND_KEY, 0);
			if (rqs == AudiocontrolsModule.RQS_STOP_SERVICE) {
				stopSelf();
			}
			if (rqs == AudiocontrolsModule.RQS_REMOVE_NOTIFICATION) {
				shouldVisible = false;
				if (isShowing) {
					windowManager.removeView(audioControlWidget);
					isShowing = false;
				}
			}
		}
	}
}