package de.appwerft.audiocontrols;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

public class LockScreenService extends IntentService {
	final String LCAT = "LockAudioScreen 😇😇😇";
	WindowManager.LayoutParams layoutParams;
	ResultReceiver resultReceiver;
	View audiocontrolView;
	WindowManager winMgr;
	Context context;

	public LockScreenService(String name) {
		super(name);
		Log.d(LCAT, "CONSTRUCTOR	");
	}

	@Override
	public void onCreate() {
		Log.d(LCAT, "onCreate");
		winMgr = (WindowManager) getSystemService(WINDOW_SERVICE);
	}

	@SuppressWarnings("deprecation")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(LCAT, "inside service on start of onStartCommand");
		/* for back communication */
		resultReceiver = intent.getParcelableExtra("receiver");
		context = TiApplication.getInstance().getApplicationContext();
		Resources res = context.getResources();
		String pn = context.getPackageName();
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		if (audiocontrolView == null) {
			// we get a context from app for getting the view from XML:
			int layoutId = context.getResources().getIdentifier("content_main",
					"layout", pn);
			audiocontrolView = (View) inflater.inflate(layoutId, null);

			// getting references to buttons:
			final int rewindcontrolId, forwardcontrolId, playcontrolId;
			Button rewindButton = (Button) inflater.inflate(
					rewindcontrolId = res.getIdentifier("rewindcontrol",
							"layout", pn), null);
			Button forwardButton = (Button) inflater.inflate(
					forwardcontrolId = res.getIdentifier("rewindcontrol",
							"layout", pn), null);
			Button playButton = (Button) inflater.inflate(
					playcontrolId = res.getIdentifier("rewindcontrol",
							"layout", pn), null);
			OnClickListener buttonListener = new View.OnClickListener() {
				@Override
				public void onClick(View clicksource) {
					int buttonId = clicksource.getId();
					String msg = "";
					if (buttonId == rewindcontrolId)
						msg = "rewind";
					if (buttonId == forwardcontrolId)
						msg = "forward";
					if (buttonId == playcontrolId)
						msg = "play";
					Bundle bundle = new Bundle();
					bundle.putString("lockscreen", msg);
					resultReceiver.send(100, bundle);
				}
			};
			rewindButton.setOnClickListener(buttonListener);
			forwardButton.setOnClickListener(buttonListener);
			playButton.setOnClickListener(buttonListener);

			// http://stackoverflow.com/questions/19846541/what-is-windowmanager-in-android
			// adding to window stack:
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
		return null;
	}

	@Override
	public void onDestroy() {
		winMgr.removeView(audiocontrolView);
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		// TODO Auto-generated method stub

	}
}
