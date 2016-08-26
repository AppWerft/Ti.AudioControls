package de.appwerft.audiocontrols;

import java.util.ArrayList;
import java.util.Arrays;

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
import android.graphics.Color;
import android.os.Build;
import android.os.Vibrator;

@Kroll.module(name = "Audiocontrols", id = "de.appwerft.audiocontrols")
public class AudiocontrolsModule extends KrollModule {
	// constants for broadcast commnunication:
	final static String ACTION = "NotifyServiceAction";
	final static String SERVICE_COMMAND_KEY = "SERVICECOMMANDKEY";
	final static String NOTIFICATION_SETPROGRESS = "NOTIFICATION_SETPROGRESS";
	final static int RQS_STOP_SERVICE = 1;
	final static int RQS_REMOVE_NOTIFICATION = 2;
	// exported constants:
	@Kroll.constant
	final int WIDGET_LOCKSCREEN = 1;
	@Kroll.constant
	final int WIDGET_NOTIFICATION = 2;
	@Kroll.constant
	final int WIDGET_BOTH = 3;
	@Kroll.constant
	final int WIDGET_POSITION_TOP = 0;
	@Kroll.constant
	final int WIDGET_POSITION_BOTTOM = 1;
	@Kroll.constant
	final public static int STATE_STOP = 0;
	@Kroll.constant
	final public static int NOTIFICATION_TYPE_COMPACT = 1;
	@Kroll.constant
	final public static int NOTIFICATION_TYPE_BIG = 0;
	@Kroll.constant
	final public static int STATE_PLAYING = 2;
	// exposed icon strings (to avoid typos):
	@Kroll.constant
	final public static String ICON_PREVIOUS = "ic_media_previous";
	@Kroll.constant
	final public static String ICON_REWIND = "ic_media_rew";
	@Kroll.constant
	final public static String ICON_PLAY = "ic_media_play";
	@Kroll.constant
	final public static String ICON_PAUSE = "ic_media_pause";
	@Kroll.constant
	final public static String ICON_NEXT = "ic_media_next";
	@Kroll.constant
	final public static String ICON_FORWARD = "ic_media_ff";
	// default buttons:
	public String[] icons = { ICON_REWIND, ICON_PLAY, ICON_NEXT };
	Context ctx;
	public static String AUDIOCONTROL_COMMAND = "AUDIOCONTROL_COMMAND";
	public static String rootActivityClassName = "";
	AudioControlWidget audioControlWidget;
	final String LCAT = "RemAudio 📻📣🔊";
	final int NOTIFICATION_ID = 1;
	private static boolean hasProgress = false;
	private static boolean hasActions = true;
	private static int vibrate = 0;

	private int iconBackgroundColor = Color.DKGRAY;

	private static int state = 0;
	static KrollFunction onClickCallback = null;

	private RemoteAudioControlEventLister remoteAudioControlEventLister;

	private static String title, artist, coverimage;

	public AudiocontrolsModule() {
		super();
		ctx = TiApplication.getInstance().getApplicationContext();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {

	}

	/* read all paramters from JS-side and save into vars in this class */
	private void getOptions(KrollDict opts) {
		Log.d(LCAT, "getOptions");
		if (opts != null && opts instanceof KrollDict) {
			if (opts.containsKeyAndNotNull("hasActions")) {
				hasActions = opts.getBoolean("hasActions");
			}
			if (opts.containsKeyAndNotNull("title")) {
				title = opts.getString("title");
			}
			if (opts.containsKeyAndNotNull("artist")) {
				artist = opts.getString("artist");
			}
			if (opts.containsKeyAndNotNull("image")) {
				coverimage = opts.getString("image");
			}
			if (opts.containsKeyAndNotNull("icons")) {
				icons = opts.getStringArray("icons");
			}

			if (opts.containsKeyAndNotNull("vibrate")) {
				vibrate = opts.getInt("vibrate");
			}
			if (opts.containsKeyAndNotNull("hasProgress")) {
				hasProgress = opts.getBoolean("hasProgress");
			}
			try {
				if (opts.containsKeyAndNotNull("iconBackgroundColor")) {
					iconBackgroundColor = Color.parseColor(opts
							.getString("iconBackgroundColor"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (opts.containsKeyAndNotNull("state")) {
				state = opts.getInt("state");
			}
			/* callback for buttons */
			if (opts.containsKeyAndNotNull("onClick")) {
				Object cb = opts.get("onClick");
				if (cb instanceof KrollFunction) {
					onClickCallback = (KrollFunction) cb;
				}
			}
		}
		Log.d(LCAT, "options imported");
	}

	@Kroll.method
	public void setProgress(float progressValue) {
		if (progressValue < 0 || progressValue > 1)
			return;
		Intent intent = new Intent(ctx.getPackageName());
		intent.setAction(ACTION);
		intent.putExtra(NOTIFICATION_SETPROGRESS, progressValue);
		ctx.sendBroadcast(intent);
		Log.d(LCAT, "RQS_STOP_SERVICE sent");
	}

	@Kroll.method
	public void setMiddleButton(String name) {
		if (icons.length >= 2) {
			icons[1] = name;
		}
		this.updateRemoteAudioControl(null);
	}

	@Kroll.method
	public void updateRemoteAudioControl(KrollDict opts) {
		this.createRemoteAudioControl(opts);
	}

	@Kroll.method
	public void hideRemoteAudioControl() {
		Log.d(LCAT, "intent for hiding control will send. ");
		Intent intent = new Intent(ctx.getPackageName());
		intent.setAction(ACTION);
		intent.putExtra(SERVICE_COMMAND_KEY, RQS_REMOVE_NOTIFICATION);
		ctx.sendBroadcast(intent);
		Log.d(LCAT, "intent for hiding control sent. " + intent.toString());
	}

	private void stopNotificationService() {
		Intent intent = new Intent(ctx.getPackageName());
		intent.setAction(ACTION);
		intent.putExtra(SERVICE_COMMAND_KEY, RQS_STOP_SERVICE);
		ctx.sendBroadcast(intent);
		Log.d(LCAT, "RQS_STOP_SERVICE sent");
	}

	@Kroll.method
	public void createRemoteAudioControl(KrollDict opts) {
		if (opts != null)
			getOptions(opts);
		/* registering of broadcastreceiver for results */
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			try {
				/* starting of service for lock screen */
				Intent intent = new Intent(ctx, LockScreenService.class);
				if (title != null)
					intent.putExtra("title", title);
				if (artist != null)
					intent.putExtra("artist", artist);
				if (coverimage != null)
					intent.putExtra("image", coverimage);

				intent.putExtra("hasActions", hasActions);
				intent.putExtra("icons",
						new ArrayList<String>(Arrays.asList(icons)));
				intent.putExtra("hasProgress", hasProgress);
				intent.putExtra("iconBackgroundColor", iconBackgroundColor);
				// needed for null case:
				intent.putExtra("state", Integer.toString(state));
				ctx.startService(intent);
				/* and start of receiver for buttons */

			} catch (Exception ex) {
				Log.e(LCAT, "Exception caught:" + ex);
			}
		}

		try {
			Log.d(LCAT, "new device or forced notification:");
			/* starting of service for it */
			Intent intent = new Intent(ctx, NotificationCompactService.class);
			if (title != null)
				intent.putExtra("title", title);
			if (artist != null)
				intent.putExtra("artist", artist);
			if (coverimage != null)
				intent.putExtra("image", coverimage);
			if (icons != null)
				intent.putExtra("icons",
						new ArrayList<String>(Arrays.asList(icons)));
			intent.putExtra("hasActions", hasActions);
			intent.putExtra("hasProgress", hasProgress);
			intent.putExtra("iconBackgroundColor", iconBackgroundColor);
			// needed for null case:
			intent.putExtra("state", Integer.toString(state));
			ctx.startService(intent);
			/* and start of receiver for buttons (first time) */
		} catch (Exception ex) {
			Log.e(LCAT, "Exception caught:" + ex);
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
	 * with this receiver we read the events from controlUI and send back to JS,
	 * quasi a proxy
	 */
	public class RemoteAudioControlEventLister extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			Log.d(LCAT, "from remote control received: " + AUDIOCONTROL_COMMAND);
			if (intent.getStringExtra(AUDIOCONTROL_COMMAND) != null) {
				if (vibrate > 0) {
					Vibrator dildo = (Vibrator) ctx
							.getSystemService(Context.VIBRATOR_SERVICE);
					dildo.vibrate(vibrate);
				}
				KrollDict event = new KrollDict();
				event.put("cmd", intent.getStringExtra(AUDIOCONTROL_COMMAND));
				Log.d(LCAT, event.toString());
				if (onClickCallback != null) {
					Log.d(LCAT, "send " + event.toString() + " back to JS");
					onClickCallback.call(getKrollObject(), event);
				} else
					Log.e(LCAT, "no callback available");
			}
		}
	}

	@Override
	public void onPause(Activity activity) {
		Log.d(LCAT, "<<<<<<< onPause");
		super.onPause(activity);
	}

	@Override
	public void onStop(Activity activity) {
		Log.d(LCAT, "<<<<<<<< onStop");
		// hideRemoteAudioControl();
		// stopNotificationService();
		super.onStop(activity);
	}

	@Override
	public void onDestroy(Activity activity) {
		Log.d(LCAT, "onDestroy");
		hideRemoteAudioControl();
		stopNotificationService();
		// TiApplication.getInstance().stopService(lockscreenService);
		super.onDestroy(activity);
	}
	/*
	 * For testing: adb shell am broadcast -a de.appwerft.audiocontrols.PLAY
	 */

}
