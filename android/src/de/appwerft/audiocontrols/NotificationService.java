package de.appwerft.audiocontrols;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiProperties;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.view.WindowManager;

public class NotificationService extends Service {
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
	final String LCAT = "NotificationService 😇😇😇";

	public NotificationService() {
		super();
		ctx = TiApplication.getInstance().getApplicationContext();
		res = ctx.getResources();
		packageName = ctx.getPackageName();
		Log.d(LCAT, "LockscreenService constructed");
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(LCAT, "onStartCommand");
		updateNotification(intent.getStringExtra("image"),
				intent.getStringExtra("title"), intent.getStringExtra("artist"));
		return START_STICKY;
	}

	private void updateNotification(String image, String title, String artist) {
	}
}
