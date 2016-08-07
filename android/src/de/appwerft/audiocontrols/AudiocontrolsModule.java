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
import de.appwerft.audiocontrols.services.*;

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
	@Kroll.constant
	final public static int STATE_STOP = 0;
	@Kroll.constant
	final public static int STATE_PLAYING = 2;

	Context ctx;
	public static String AUDIOCONTROL_COMMAND = "AUDIOCONTROL_COMMAND";
	public static String rootActivityClassName = "";
	AudioControlWidget audioControlWidget;
	final String LCAT = "RemAudio ♛♛♛";
	final int NOTIFICATION_ID = 1;
	private static int lollipop = 1;
	private static int state = 0;

	private Intent lockscreenService;
	static KrollFunction onKeypressedCallback = null;

	private RemoteAudioControlEventLister remoteAudioControlEventLister;

	private static String title, artist, image;

	public AudiocontrolsModule() {
		super();

		ctx = TiApplication.getInstance().getApplicationContext();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {

	}

	public void onPause(Activity activity) {
		Log.d(LCAT, "onPause");
		super.onPause(activity);
	}

	public void onStop(Activity activity) {
		Log.d(LCAT, "onStop");
		/*
		 * TiApplication.getInstance().stopService(lockscreenService);
		 * Log.d(LCAT, "TiApplication.getInst"); removeRemoteAudioControl();
		 * Log.d(LCAT, "removeRemoteAudioCon"); if
		 * (remoteAudioControlEventLister != null) { Log.d(LCAT,
		 * "terminating remoteAudioControlEventLister");
		 * ctx.unregisterReceiver(remoteAudioControlEventLister); }
		 */
		super.onStop(activity);
	}

	@Override
	public void onDestroy(Activity activity) {
		Log.d(LCAT, "onDestroy");
		removeRemoteAudioControl();
		stopNotificationService();
		// TiApplication.getInstance().stopService(lockscreenService);
		super.onDestroy(activity);
	}

	/* read all paramters from JS-side and save into vars in this class */
	private void getOptions(KrollDict opts) {
		if (opts != null && opts instanceof KrollDict) {
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
			if (opts.containsKeyAndNotNull("state")) {
				state = opts.getInt("state");
			}
			/* callback for buttons */
			if (opts.containsKeyAndNotNull("onKeypressed")) {
				Object cb = opts.get("onKeypressed");
				if (cb instanceof KrollFunction) {
					onKeypressedCallback = (KrollFunction) cb;
				}
			}
		}
	}

	@Kroll.method
	public void updateRemoteAudioControl(KrollDict opts) {
		this.createRemoteAudioControl(opts);
	}

	@Kroll.method
	public void removeRemoteAudioControl() {
		Log.d(LCAT, "removeRemoteAudioControl");
		Intent intent = new Intent(ctx.getPackageName());
		intent.setAction(NotificationBigService.ACTION);
		intent.putExtra(NotificationBigService.SERVICE_COMMAND_KEY,
				NotificationBigService.RQS_REMOVE_NOTIFICATION);

		Log.d(LCAT, "RQS_STOP_SERVICE will send");
		ctx.sendBroadcast(intent);
		Log.d(LCAT, "RQS_STOP_SERVICE sent");

	}

	private void stopNotificationService() {
		Intent intent = new Intent(ctx.getPackageName());
		intent.setAction(NotificationBigService.ACTION);
		intent.putExtra(NotificationBigService.SERVICE_COMMAND_KEY,
				NotificationBigService.RQS_STOP_SERVICE);
		ctx.sendBroadcast(intent);
		Log.d(LCAT, "RQS_STOP_SERVICE sent");
	}

	private boolean supportsBothWidgets() {
		return (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) ? true
				: false;
	}

	@Kroll.method
	public void createRemoteAudioControl(KrollDict opts) {
		/* all options will read from Javascript and save as class vars */
		getOptions(opts);
		/* registering of broadcastreceiver for results */
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
				|| supportsBothWidgets() == true
				&& lollipop == WIDGET_LOCKSCREEN) {
			Log.d(LCAT, "LockscreenView started");
			try {
				/* starting of service for it */
				Intent intent = new Intent(ctx, LockScreenService.class);
				if (title != null)
					intent.putExtra("title", title);
				if (artist != null)
					intent.putExtra("artist", artist);
				if (image != null)
					intent.putExtra("image", image);
				intent.putExtra("state", Integer.toString(state));
				ctx.startService(intent);
				/* and start of receiver for buttons */

			} catch (Exception ex) {
				Log.e(LCAT, "Exception caught:" + ex);
			}
		}
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
				|| supportsBothWidgets() == true
				&& lollipop == WIDGET_NOTIFICATION) {
			try {
				/* starting of service for it */
				Intent intent = new Intent(ctx, NotificationBigService.class);
				if (title != null)
					intent.putExtra("title", title);
				if (artist != null)
					intent.putExtra("artist", artist);
				if (image != null)
					intent.putExtra("image", image);
				intent.putExtra("state", Integer.toString(state));
				ctx.startService(intent);
				/* and start of receiver for buttons (first time) */
			} catch (Exception ex) {
				Log.e(LCAT, "Exception caught:" + ex);
			}
		}
		/* in all API levels: */
		if (remoteAudioControlEventLister == null) {
			remoteAudioControlEventLister = new RemoteAudioControlEventLister();
			IntentFilter filter = new IntentFilter(ctx.getPackageName());
			ctx.registerReceiver(remoteAudioControlEventLister, filter);
			Log.d(LCAT, "remoteAudioControlEventLister started");
		}

	}

	@Kroll.method
	public void removeEventListener(String eventname) {
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

	/* with this receiver we read the events from controlUI */
	public class RemoteAudioControlEventLister extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			final String audiocontrolercmd;
			if (intent.getStringExtra(AUDIOCONTROL_COMMAND) != null) {
				Vibrator v = (Vibrator) ctx
						.getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(20);
				KrollDict dict = new KrollDict();
				dict.put("cmd", intent.getStringExtra(AUDIOCONTROL_COMMAND));
				if (onKeypressedCallback != null
						&& onKeypressedCallback instanceof KrollFunction) {
					onKeypressedCallback.call(getKrollObject(), dict);
				}
			}
		}
	}

	/*
	 * For testing: adb shell am broadcast -a de.appwerft.audiocontrols.PLAY
	 */

}
