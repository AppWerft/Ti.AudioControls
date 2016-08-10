package de.appwerft.audiocontrols;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class NotificationCompactService extends Service {
	final static String ACTION = "NotifyServiceAction";
	final static String STOP_SERVICE_BROADCAST_KEY = "StopServiceBroadcastKey";
	final static int RQS_STOP_SERVICE = 1;
	NotificationCompactServiceReceiver notificationServiceReceiver;
	ResultReceiver resultReceiver;
	Resources res;
	String packageName;
	Context ctx;
	Boolean isPendingintentStarted = false;
	private NotificationManager notificationManager;
	private RemoteViews remoteViews;
	private NotificationCompat.Builder builder;
	final boolean PLAYING = true;
	final boolean PAUSING = false;
	boolean playintentdone = false;
	boolean previntentdone = false;
	boolean nextintentdone = false;

	boolean state = PLAYING;
	final int NOTIFICATION_ID = 1337;
	final int REQUEST_CODE = 1337;
	final String LCAT = "NotificationBigService 👽👽";
	int artistId, coverimageId, titleId, prevcontrolId, nextcontrolId,
			playcontrolId;
	int playiconId, pauseiconId;

	// http://stackoverflow.com/questions/22789588/how-to-update-notification-with-remoteviews
	public NotificationCompactService() {
		super();
		ctx = TiApplication.getInstance().getApplicationContext();
		res = ctx.getResources();
		packageName = ctx.getPackageName();
		artistId = R("artist", "id");
		titleId = R("title", "id");
		prevcontrolId = R("prevCtrl", "id");
		nextcontrolId = R("nextCtrl", "id");
		playcontrolId = R("playCtrl", "id");
		coverimageId = R("coverimage", "id");
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
		notificationServiceReceiver = new NotificationCompactServiceReceiver();
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent dummy) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		setUpNotification();
		if (intent != null && intent.hasExtra("title")) {
			updateNotification(intent.getExtras());
		}
		return START_STICKY;
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

	private void setUpNotification() {
		// http://stackoverflow.com/questions/22789588/how-to-update-notification-with-remoteviews
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		builder = new NotificationCompat.Builder(ctx);
		builder.setSmallIcon(R("notification_icon", "drawable"))
				.setAutoCancel(false).setOngoing(true).setContentTitle("")
				// .setContentIntent(pendIntent)
				.setContent(remoteViews);
		notificationManager.notify(NOTIFICATION_ID, builder.build());
		// startForeground(NOTIFICATION_ID, builder.build());

		if (!playintentdone) {
			setCompactButtonListener(playcontrolId, "play");
			playintentdone = true;
		}
		if (!previntentdone) {
			setCompactButtonListener(prevcontrolId, "prev");
			previntentdone = true;
		}
		if (!nextintentdone) {
			setCompactButtonListener(nextcontrolId, "next");
			nextintentdone = true;
		}
	}

	private void updateNotification(final Bundle bundle) {
		remoteViews.setTextViewText(artistId, bundle.getString("artist"));
		remoteViews.setTextViewText(titleId, bundle.getString("title"));
		notificationManager.notify(NOTIFICATION_ID, builder.build());
		final String image = bundle.getString("image");
		if (image != null) {
			final Target target = new Target() {
				@Override
				public void onBitmapLoaded(Bitmap bitmap,
						Picasso.LoadedFrom from) {
					remoteViews.setImageViewBitmap(coverimageId, bitmap);
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

	public class NotificationCompactServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			int rqs = intent.getIntExtra(STOP_SERVICE_BROADCAST_KEY, 0);
			if (rqs == RQS_STOP_SERVICE) {
				stopSelf();
				((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
						.cancelAll();
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

	private void setCompactButtonListener(int id, String msg) {
		/* same intent as in AudioControlWidget */
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		intent.setAction(ctx.getPackageName());
		intent.putExtra(AudiocontrolsModule.AUDIOCONTROL_COMMAND, msg);
		PendingIntent pendIntent = PendingIntent.getBroadcast(ctx, id, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(id, pendIntent);
		// ctx.sendBroadcast(intent);

	}

}
