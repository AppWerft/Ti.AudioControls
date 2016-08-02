package de.appwerft.audiocontrols;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

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
	private Intent lockscreenService;
	KrollFunction onKeypressedCallback = null;
	private RemoteControlReceiver mediakeyListener;
	private NotificationReceiver notificationListener;
	private AudioControlWidgetReceiver audioControlWidgetReceiver;
	Boolean LOCKSCREENVIEWENABLED = true;
	Boolean NOTIFICATIONVIEWENABLED = true;
	private String title, artist, image;
	Boolean notificationbuilt = false;
	AudioControlNotification audioControlNotification;

	public AudiocontrolsModule() {
		super();
		mediakeyListener = new RemoteControlReceiver();
		intentFilterForMediaButton = new IntentFilter(
				Intent.ACTION_MEDIA_BUTTON);
		intentFilterForMediaButton
				.addAction("android.intent.action.ACTION_MEDIA_BUTTON");
		intentFilterForMediaButton.setPriority(10000);
		audioControlWidgetReceiver = new AudioControlWidgetReceiver();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {

	}

	@Override
	public void onDestroy(Activity activity) {
		TiApplication.getInstance().stopService(lockscreenService);
		ctx.unregisterReceiver(mediakeyListener);
		ctx.unregisterReceiver(audioControlWidgetReceiver);
		ctx.unregisterReceiver(notificationListener);

		super.onDestroy(activity);

	}

	/* read all paramters from JS-side and save into vars in this class */
	private void getOptions(KrollDict opts) {
		if (opts != null && opts.containsKeyAndNotNull("title")) {
			title = opts.getString("title");
		}
		if (opts != null && opts.containsKeyAndNotNull("artist")) {
			artist = opts.getString("artist");
		}
		if (opts != null && opts.containsKeyAndNotNull("image")) {
			image = opts.getString("image");
		}
		/* callback for buttons */
		if (opts != null && opts.containsKeyAndNotNull("onKeypressed")) {
			Object cb = opts.get("onKeypressed");
			if (cb instanceof KrollFunction) {
				onKeypressedCallback = (KrollFunction) cb;
			}
		}
		/* both kinds of UI */
		if (opts != null && opts.containsKeyAndNotNull("lockscreen")) {
			LOCKSCREENVIEWENABLED = opts.getBoolean("lockscreen");
		}
		if (opts != null && opts.containsKeyAndNotNull("notification")) {
			NOTIFICATIONVIEWENABLED = opts.getBoolean("notification");
		}
	}

	@Kroll.method
	public void updateRemoteAudioControl(KrollDict opts) {
		this.createRemoteAudioControl(opts);
	}

	@Kroll.method
	public void createRemoteAudioControl(KrollDict opts) {

		this.getOptions(opts);
		if (NOTIFICATIONVIEWENABLED == true) {
			if (notificationbuilt == false) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
					return;
				ctx = TiApplication.getInstance().getApplicationContext();
				audioControlNotification = new AudioControlNotification(ctx);
				notificationbuilt = true;
			} else {
				audioControlNotification.updateContent("", "", "");
			}
			notificationListener = new NotificationReceiver();
			IntentFilter filter = new IntentFilter(ctx.getPackageName());
			ctx.registerReceiver(notificationListener, filter);
			// ctx.registerReceiver(NotificationReceiver);

		}
		if (LOCKSCREENVIEWENABLED == true) {
			try {
				/* starting of service for it */
				ctx = TiApplication.getInstance().getApplicationContext();
				Intent intent = new Intent(ctx, LockScreenService.class);
				intent.putExtra("title", title);
				intent.putExtra("artist", artist);
				intent.putExtra("image", image);
				ctx.startService(intent);
				/* registering of broadcastreceiver for results */
				IntentFilter filter = new IntentFilter(ctx.getPackageName());
				ctx.registerReceiver(audioControlWidgetReceiver, filter);

			} catch (Exception ex) {
				Log.d(LCAT, "Exception caught:" + ex);
			}
		}
	}

	@Kroll.method
	public void addEventListener(String eventname, KrollFunction callback) {
		if (eventname != null && callback != null) {
			onKeypressedCallback = callback;
			ctx.registerReceiver(mediakeyListener, intentFilterForMediaButton);
		}

	}

	@Kroll.method
	public void removeEventListener(String eventname) {
		ctx.unregisterReceiver(mediakeyListener);
	}

	public void onStartStop(View view) {
		Log.d(LCAT, "▶︎◼︎ ▶︎◼ ︎▶︎◼︎");

	}

	public void onForward(View view) {
		Log.d(LCAT, ">>>>>>>>>>>>>>>>>>>");

	}

	public void onRewind(View view) {
		Log.d(LCAT, "<<<<<<<<<<<<<<<<<<<<");
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
	private class RemoteControlReceiver extends BroadcastReceiver {
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
	private class AudioControlWidgetReceiver extends BroadcastReceiver {
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

	private class NotificationReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("de.appwerft.audiocontrols.play")) {
			} else if (intent.getAction().equals(
					"de.appwerft.audiocontrols.next")) {
			} else if (intent.getAction().equals(
					"de.appwerft.audiocontrols.prev")) {
			}
		}
	}

}
