package de.appwerft.audiocontrols;

import org.appcelerator.kroll.common.Log;
import de.appwerft.helpers.*;
import org.appcelerator.titanium.TiApplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class NotificationCompactService extends Service {
	/*
	 * final static String ACTION = "NotifyServiceAction"; final static String
	 * SERVICE_COMMAND_KEY = "SERVICE_COMMAND_KEY"; final static String
	 * NOTIFICATION_SETPROGRESS = "NOTIFICATION_SETPROGRESS"; final static int
	 * RQS_STOP_SERVICE = 1; final static int RQS_REMOVE_NOTIFICATION = 2;
	 */
	NotificationServiceReceiver notificationServiceReceiver;
	ResultReceiver resultReceiver;
	Resources res;
	String packageName;
	Context ctx;
	Boolean isPendingintentStarted = false;
	private NotificationManager notificationManager;
	private NotificationCompat.BigTextStyle bigTextNotification;
	private RemoteViews remoteViews;
	private NotificationCompat.Builder builder;
	final boolean PLAYING = true;
	final boolean PAUSING = false;
	boolean state = PLAYING;

	final int NOTIFICATION_ID = 1337;
	final int REQUEST_CODE = 1337;
	private boolean hasProgress;
	final String LCAT = "NotificationCompactService";
	int artistId, coverimageId, titleId, prevcontrolId, nextcontrolId,
			playcontrolId;
	int playiconId, pauseiconId, playIcon, stopIcon, prevIcon, nextIcon;

	// http://stackoverflow.com/questions/22789588/how-to-update-notification-with-remoteviews
	public NotificationCompactService() {
		super();
		ctx = TiApplication.getInstance().getApplicationContext();
		res = ctx.getResources();
		packageName = ctx.getPackageName();
		/*
		 * artistId = R("artist", "id"); titleId = R("title", "id");
		 * prevcontrolId = R("prevCtrl", "id"); nextcontrolId = R("nextCtrl",
		 * "id"); playcontrolId = R("playCtrl", "id"); coverimageId =
		 * R("coverimage", "id");
		 */
		playiconId = R("ic_play", "drawable");
		pauseiconId = R("ic_pause", "drawable");
		remoteViews = new RemoteViews(ctx.getPackageName(), R(
				"remoteaudiocontrol_notification", "layout"));
		// setCompactButtonListeners(remoteViews, ctx);
	}

	@Override
	public void onCreate() {
		Log.d(LCAT,
				"LockscreenService created => new notificationServiceReceiver");
		notificationServiceReceiver = new NotificationServiceReceiver();
		// for back communication:*/
		IntentFilter filter = new IntentFilter();
		filter.addAction(AudiocontrolsModule.ACTION);
		ctx.registerReceiver(notificationServiceReceiver, filter);
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent dummy) {
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		stopSelf();
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(LCAT, "onStartCommand " + intent.toString());
		if (builder == null)
			createNotification(intent.getExtras());
		if (intent != null && intent.hasExtra("title")) {
			updateNotification(intent.getExtras());
		}
		return START_NOT_STICKY;
	}

	private void createNotification(Bundle bundle) {
		hasProgress = bundle.getBoolean("hasProgress");
		final boolean hasActions = bundle.getBoolean("hasActions");
		final int iconBackgroundColor = bundle.getInt("iconBackgroundColor");
		final String title = bundle.getString("title");
		final String artist = bundle.getString("artist");
		// http://stackoverflow.com/questions/22789588/how-to-update-notification-with-remoteviews
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		builder = new NotificationCompat.Builder(ctx);
		builder.setSmallIcon(R("notification_icon", "drawable"))
				.setAutoCancel(false).setOngoing(true)
				.setColor(iconBackgroundColor)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setContentTitle("").setContentText("");
		if (hasProgress) {
			builder.setProgress(100, 0, false);
		}
		if (hasActions) {
			bigTextNotification = new NotificationCompat.BigTextStyle();
			bigTextNotification.setBigContentTitle("Title");
			bigTextNotification.bigText("Title");
			builder.setStyle(bigTextNotification);
			setAudioControlActions("ic_media_play");
		}
		if (title != null)
			notificationManager.notify(NOTIFICATION_ID, builder.build());

	}

	private PendingIntent createPendingIntent(String msg) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		intent.setAction(getPackageName());
		intent.putExtra(AudiocontrolsModule.AUDIOCONTROL_COMMAND, msg);
		PendingIntent pendIntent = PendingIntent.getBroadcast(ctx,
				(int) System.currentTimeMillis(), intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		return pendIntent;
	}

	private void setAudioControlActions(String id) {
		builder.mActions.clear();
		Log.d(LCAT, "id=" + id);
		builder.addAction(RHelper.getAndroidDrawable("ic_media_rew"),
				RHelper.getAppText("ic_media_rew"),
				createPendingIntent("rewind"));

		builder.addAction(RHelper.getAndroidDrawable(id),
				RHelper.getAppText(id), createPendingIntent("play"));

		builder.addAction(RHelper.getAndroidDrawable("ic_media_ff"),
				RHelper.getAppText("ic_media_ff"),
				createPendingIntent("forward"));
	}

	private void updateNotification(final Bundle bundle) {
		final String title = bundle.getString("title");
		final String artist = bundle.getString("artist");
		final String image = bundle.getString("image");

		if (title != null) {
			bigTextNotification.setBigContentTitle(title);
			bigTextNotification.bigText(artist);
		}
		if (artist != null) {
			builder.setContentTitle(title);
			builder.setContentText(artist);
		}

		if (bundle.getString("state") != null) {
			final int state = Integer.parseInt(bundle.getString("state"));
			if (state == AudiocontrolsModule.STATE_PLAYING) {
				setAudioControlActions("ic_media_pause");
			}
			if (state == AudiocontrolsModule.STATE_STOP) {
				setAudioControlActions("ic_media_play");
			}
		}
		notificationManager.notify(NOTIFICATION_ID, builder.build());
		if (image != null) {
			final Target target = new Target() {
				@Override
				public void onBitmapLoaded(Bitmap bitmap,
						Picasso.LoadedFrom from) {
					builder.setLargeIcon(bitmap);
					notificationManager
							.notify(NOTIFICATION_ID, builder.build());
				}

				@Override
				public void onBitmapFailed(Drawable errorDrawable) {
				}

				@Override
				public void onPrepareLoad(Drawable placeHolderDrawable) {
				}
			};
			Picasso.with(ctx).load(image).into(target);

		} else {
			Log.i(LCAT, "image is null in updateNotification ");
		}
	}

	public class NotificationServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			if (intent.hasExtra(AudiocontrolsModule.SERVICE_COMMAND_KEY)) {
				int rqs = intent.getIntExtra(
						AudiocontrolsModule.SERVICE_COMMAND_KEY, 0);
				if (rqs == AudiocontrolsModule.RQS_STOP_SERVICE) {
					Log.d(LCAT, "STOP_SERVICE_BROADCAST_KEY received");
					stopSelf();
					Log.d(LCAT, "stopSelf");
					((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
							.cancelAll();
				}
				if (rqs == AudiocontrolsModule.RQS_REMOVE_NOTIFICATION) {
					Log.d(LCAT, "RQS_REMOVE_NOTIFICATION received");
					notificationManager.cancel(NOTIFICATION_ID);
					Log.d(LCAT, "notificationManager.cancelAll()");
				}
			}
			if (intent.hasExtra(AudiocontrolsModule.NOTIFICATION_SETPROGRESS)) {
				float progressValue = intent.getFloatExtra(
						AudiocontrolsModule.NOTIFICATION_SETPROGRESS, 0.0f);
				if (builder != null && hasProgress == true) {
					builder.setProgress(1000, Math.round(progressValue * 1000),
							false);
				} else {
					Log.w(LCAT, "hasProgress was false, cannot setProgress");
				}
			}
		}
	}

	/* helper function for safety getting resources */
	private int R(String name, String type) {
		int id = 0;
		try {
			id = ctx.getResources().getIdentifier(name, type,
					ctx.getPackageName());
		} catch (Exception e) {
			return id;
		}
		return id;
	}

	private int R(String name, String type, String pn) {
		int id = 0;
		try {
			id = ctx.getResources().getIdentifier(name, type, pn);
		} catch (Exception e) {
			return id;
		}
		return id;
	}

	private CharSequence RgetText(String name, String type) {
		CharSequence bar = "";
		Log.d(LCAT, "name type " + name + "  " + type);
		try {
			int id = ctx.getResources().getIdentifier(name, type, null);
			bar = ctx.getResources().getText(id);
		} catch (Exception e) {
			Log.e(LCAT, "Resource not found: " + type + "@" + name);
			return bar;
		}
		Log.d(LCAT, "bar=" + bar);
		return bar;
	}
}
