package de.appwerft.audiocontrols;

import java.util.Timer;

import org.appcelerator.kroll.common.Log;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class LockScreenService extends Service {
	Timer timer;
	WindowManager.LayoutParams layoutParams;

	View audiocontrolView;
	WindowManager winMgr;
	Handler mHandler;

	int scale = -1;
	int level = -1;
	int charging = 0;

	private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			sendBroadcast();
		}
	};

	@Override
	public void onCreate() {
		Log.v("Nav Service: ",
				"{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{CREATED");
		// Start up the thread running the service. Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block. We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		/*
		 * allowedApps.add("com.sapientnitro.lcinstore2");//LC
		 * allowedApps.add("com.adobe.reader");//acrobat
		 * allowedApps.add("com.dynamixsoftware.printershare");//printer share
		 * allowedApps.add("my.handrite.prem");//HandRite Pro
		 */
		mHandler = new Handler();
		// batMan = new BatteryManager();
		winMgr = (WindowManager) getSystemService(WINDOW_SERVICE);
		// cTime = new Time(Time.getCurrentTimezone());

	}

	@SuppressWarnings("deprecation")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (audiocontrolView == null) {
			// create View
			audiocontrolView = new RelativeLayout(getApplicationContext());
			layoutParams = new WindowManager.LayoutParams(
					WindowManager.LayoutParams.FILL_PARENT, 50,
					// This allows the view to be displayed over the status bar
					// WindowManager.LayoutParams.TYPE_SYSTEM_ALERT |
					WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
					// this is to keep button presses going to the background
					// window
					WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
							| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
							|
							// this is to enable the notification to recieve
							// touch
							// events
							WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
							// Draws over status bar
							WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,

					PixelFormat.TRANSPARENT);
			layoutParams.gravity = Gravity.TOP;
			layoutParams.y = 50;
			audiocontrolView.setBackgroundColor(0xffff0000);
			winMgr.addView(audiocontrolView, layoutParams);

			// If we get killed, after returning from here, restart
		} else {
			// Update view

		}
		return START_REDELIVER_INTENT;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	@Override
	public void onDestroy() {
		winMgr.removeView(audiocontrolView);
	}
}
