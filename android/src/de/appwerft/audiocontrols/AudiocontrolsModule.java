package de.appwerft.audiocontrols;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.widget.Toast;

@Kroll.module(name = "Audiocontrols", id = "de.appwerft.audiocontrols")
public class AudiocontrolsModule extends KrollModule {
	@Kroll.constant
	final int WIDGET_LOCKSCREEN = 1;
	@Kroll.constant
	final int WIDGET_NOTIFICATION = 2;
	@Kroll.constant
	final int WIDGET_POSITION_TOP = 0;
	@Kroll.constant
	final int WIDGET_POSITION_BOTTOM = 1;
	Context ctx;
	private IntentFilter intentFilterForMediaButton;
	public static String rootActivityClassName = "";
	AudioControlWidget audioControlWidget;
	final String LCAT = "RemAudioScreen ♛♛♛";
	final int NOTIFICATION_ID = 1;
	private int lollipop = 1;
	private Intent lockscreenService;
	KrollFunction onKeypressedCallback = null;
	private HeadsetEventListener headsetEventListener;
	private NotificationEventListener notificationEventListener;
	private LockscreenwidgetEventListener lockscreenwidgetEventListener;
	Boolean lockscreenEnabled = false;
	Boolean notificationEnabled = true;
	Boolean headsetEnabled = true;
	private String title, artist, image;
	AudioControlNotification audioControlNotification;

	public AudiocontrolsModule() {
		super();
		headsetEventListener = new HeadsetEventListener();
		intentFilterForMediaButton = new IntentFilter(
				Intent.ACTION_MEDIA_BUTTON);
		intentFilterForMediaButton
				.addAction("android.intent.action.ACTION_MEDIA_BUTTON");
		intentFilterForMediaButton.setPriority(10000);
		ctx = TiApplication.getInstance().getApplicationContext();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {

	}

	@Override
	public void onDestroy(Activity activity) {
		TiApplication.getInstance().stopService(lockscreenService);
		if (headsetEventListener != null)
			ctx.unregisterReceiver(headsetEventListener);
		if (lockscreenwidgetEventListener != null)
			ctx.unregisterReceiver(lockscreenwidgetEventListener);
		if (notificationEventListener != null)
			ctx.unregisterReceiver(notificationEventListener);
		if (audioControlNotification != null)
			audioControlNotification.cancelNotification();
		super.onDestroy(activity);
	}

	/* read all paramters from JS-side and save into vars in this class */
	private void getOptions(KrollDict opts) {
		if (opts == null)
			return;
		if (opts.containsKeyAndNotNull("title")) {
			title = opts.getString("title");
		}
		if (opts.containsKeyAndNotNull("artist")) {
			artist = opts.getString("artist");
		}
		if (opts.containsKeyAndNotNull("image")) {
			image = opts.getString("image");
		}
		if (opts.containsKeyAndNotNull("lollipop")) {
			lollipop = opts.getInt("lollipop");
		}
		/* callback for buttons */
		if (opts.containsKeyAndNotNull("onKeypressed")) {
			Object cb = opts.get("onKeypressed");
			if (cb instanceof KrollFunction) {
				onKeypressedCallback = (KrollFunction) cb;
			}
		}
		/* both kinds of UI */
		if (opts.containsKeyAndNotNull("lockscreenEnabled")) {
			lockscreenEnabled = opts.getBoolean("lockscreenEnabled");
		}
		if (opts.containsKeyAndNotNull("notificationEnabled")) {
			notificationEnabled = opts.getBoolean("notificationEnabled");
		}
	}

	@Kroll.method
	public void updateRemoteAudioControl(KrollDict opts) {
		Log.d(LCAT, "inside updateRemoteAudioControl");
		this.createRemoteAudioControl(opts);
	}

	@Kroll.method
	public void createRemoteAudioControl(KrollDict opts) {
		/* all options will read from Javascript and save as class vars */
		this.getOptions(opts);

		/* registering of broadcastreceiver for results */
		final int VERSION = Build.VERSION.SDK_INT;
		Log.d(LCAT, ">>>>>>>>> AP Version=" + VERSION + "   lollipop="
				+ WIDGET_LOCKSCREEN);
		if (VERSION < 21
				|| ((VERSION == 21 || VERSION == 22) && lollipop == WIDGET_LOCKSCREEN)) {
			Log.d(LCAT, "LockscreenView started");
			try {
				/* starting of service for it */
				Intent intent = new Intent(ctx, LockScreenService.class);
				intent.putExtra("title", title);
				intent.putExtra("artist", artist);
				intent.putExtra("image", image);
				ctx.startService(intent);
				/* and start of receiver for buttons */
				if (lockscreenwidgetEventListener == null) {
					lockscreenwidgetEventListener = new LockscreenwidgetEventListener();
					IntentFilter filter = new IntentFilter(ctx.getPackageName());
					ctx.registerReceiver(lockscreenwidgetEventListener, filter);
				}
			} catch (Exception ex) {
				Log.e(LCAT, "Exception caught:" + ex);
			}
		}
		if (VERSION > 22
				|| ((VERSION == 21 || VERSION == 22) && lollipop == WIDGET_NOTIFICATION)) {
			try {
				/* starting of service for it */
				Intent intent = new Intent(ctx, NotificationService.class);
				intent.putExtra("title", title);
				intent.putExtra("artist", artist);
				intent.putExtra("image", image);
				ctx.startService(intent);
				/* and start of receiver for buttons (first time) */
				if (notificationEventListener == null) {
					NotificationEventListener notificationEventListener = new NotificationEventListener();
					IntentFilter filter = new IntentFilter(
							"de.appwerft.audiocontrols.PLAY");
					filter.addAction("PLAYCONTROL");
					ctx.registerReceiver(notificationEventListener, filter);
				}
			} catch (Exception ex) {
				Log.e(LCAT, "Exception caught:" + ex);
			}
		}
	}

	@Kroll.method
	public void addEventListener(String eventname, KrollFunction callback) {
		if (eventname != null && callback != null) {
			onKeypressedCallback = callback;
			ctx.registerReceiver(headsetEventListener,
					intentFilterForMediaButton);
		}
	}

	@Kroll.method
	public void removeEventListener(String eventname) {
		ctx.unregisterReceiver(headsetEventListener);
	}

	@Kroll.method
	public void setContent(KrollDict opts) {
		// http://stackoverflow.com/questions/15346647/android-passing-variables-to-an-already-running-service
		// Alias to create…
		this.createRemoteAudioControl(opts);
	}

	@Override
	public void onStart(Activity activity) {
		rootActivityClassName = TiApplication.getInstance()
				.getApplicationContext().getPackageName()
				+ "."
				+ TiApplication.getAppRootOrCurrentActivity().getClass()
						.getSimpleName();
		Log.d(LCAT, "Module started");
		super.onStart(activity);
	}

	// http://stackoverflow.com/questions/9056814/how-do-i-intercept-button-presses-on-the-headset-in-android
	/*
	 * This Receiver is for events from hardware button on Headset
	 */
	private class HeadsetEventListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
				KeyEvent event = (KeyEvent) intent
						.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
				if (event == null) {
					return;
				}
				// if (event.getAction() == KeyEvent.ACTION_DOWN) {
				KrollDict dict = new KrollDict();
				dict.put("keycode", event.getKeyCode());
				onKeypressedCallback.call(getKrollObject(), dict);
				// }
			}
		}
	}

	/* with this receiver we read the events from controlUI */
	private class LockscreenwidgetEventListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			final String audiocontrolercmd;
			if (intent.getStringExtra("audiocontrolercmd") != null) {
				audiocontrolercmd = "audio_"
						+ intent.getStringExtra("audiocontrolercmd");
				Log.d(LCAT, audiocontrolercmd);
				KrollDict dict = new KrollDict();
				dict.put("keypressed", audiocontrolercmd);
				if (onKeypressedCallback != null
						&& onKeypressedCallback instanceof KrollFunction) {
					onKeypressedCallback.call(getKrollObject(), dict);
				} else {
					Log.e(LCAT,
							"onKeypressedCallback is null or not Krollfunction "
									+ onKeypressedCallback.toString());
				}
			}
		}
	}

	/*
	 * For testing: adb shell am broadcast -a de.appwerft.audiocontrols.PLAY
	 */
	public class NotificationEventListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			Log.d(LCAT, NotificationEventListener.class.getSimpleName(),
					"received broadcast");
			String pn = "de.appwerft.audiocontrols";
			String action = intent.getAction();
			Vibrator v = (Vibrator) ctx
					.getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(50);
			if (action.equalsIgnoreCase(pn + ".PLAY")) {
				Log.d(LCAT, "►◼ pressed︎");
				audioControlNotification.togglePlayButton();
			} else if (action.equalsIgnoreCase(pn + ".NEXT")) {
				Log.d(LCAT, "⇤ pressed︎");
			} else if (action.equalsIgnoreCase(pn + ".PREV")) {
				Log.d(LCAT, "⇥ pressed︎");
			}
		}
	}

}
