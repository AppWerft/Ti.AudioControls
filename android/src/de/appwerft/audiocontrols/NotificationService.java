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
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class NotificationService extends Service {
	final static String ACTION = "NotifyServiceAction";
	final static String STOP_SERVICE_BROADCAST_KEY = "StopServiceBroadcastKey";
	final static int RQS_STOP_SERVICE = 1;
	NotificationServiceReceiver notificationServiceReceiver;
	ResultReceiver resultReceiver;
	Resources res;
	String packageName;
	Context ctx;

	private NotificationTarget notificationTarget;
	private NotificationManager notificationManager;

	private RemoteViews remoteViews;

	private NotificationCompat.Builder builder;

	final boolean PLAYING = true;
	final boolean PAUSING = false;
	boolean state = PLAYING;
	final int NOTIFICATION_ID = 1337;
	final int REQUEST_CODE = 1337;
	final String LCAT = "NotificationService 👽👽";
	int artistId, coverimageId, titleId, prevcontrolId, nextcontrolId,
			playcontrolId;
	int playiconId, pauseiconId;

	// http://stackoverflow.com/questions/22789588/how-to-update-notification-with-remoteviews
	public NotificationService() {
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
		playiconId = R("android:ic_media_play", "drawable");
		pauseiconId = R("android:ic_media_pause", "drawable");
		/* for dynamic update: */
		playiconId = R("android:ic_media_play", "drawable");
		pauseiconId = R("android:ic_media_pause", "drawable");
		remoteViews = new RemoteViews(ctx.getPackageName(), R(
				"remoteaudiocontrol_notification", "layout"));
		// setButtonListeners(remoteViews, ctx);
	}

	@Override
	public void onCreate() {
		Log.d(LCAT,
				"LockscreenService created => new notificationServiceReceiver");
		notificationServiceReceiver = new NotificationServiceReceiver();
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent dummy) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d(LCAT, "onStartCommand");
		/*
		 * if (intent != null) { Log.d(LCAT, intent.toString()); notification =
		 * new NotificationCompat.Builder(ctx).setOngoing(true)
		 * .setSmallIcon(R("notification_icon", "drawable"))
		 * .setOnlyAlertOnce(true).setContentTitle("").build(); //
		 * notification.flags |= Notification.FLAG_AUTO_CANCEL;
		 * notification.contentView = view;
		 * 
		 * notificationManager = (NotificationManager) ctx
		 * .getSystemService(Context.NOTIFICATION_SERVICE);
		 * notificationManager.notify(NOTIFICATION_ID, notification); if
		 * (intent.hasExtra("title")) updateNotification(intent.getExtras()); }
		 */
		setUpNotification();
		if (intent != null && intent.hasExtra("title")) {
			updateNotification(intent.getExtras());
		}
		return START_STICKY;
	}

	private void setUpNotification() {
		// http://stackoverflow.com/questions/22789588/how-to-update-notification-with-remoteviews
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// we need to build a basic notification first, then update it
		// Intent intentNotif = new Intent(this, MainActivity.class);
		// intentNotif.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
		// Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// PendingIntent pendIntent = PendingIntent.getActivity(this, 0,
		// intentNotif, PendingIntent.FLAG_UPDATE_CURRENT);

		// notification's layout

		builder = new NotificationCompat.Builder(ctx);
		builder.setSmallIcon(R("notification_icon", "drawable"))
				.setAutoCancel(false).setOngoing(true).setContentTitle("")
				// .setContentIntent(pendIntent)
				.setContent(remoteViews);
		notificationManager.notify(NOTIFICATION_ID, builder.build());

		// startForeground(NOTIFICATION_ID, builder.build());
	}

	private void updateNotification(final Bundle bundle) {
		final String image = bundle.getString("image");
		if (image != null) {
			final Target target = new Target() {
				@Override
				public void onBitmapLoaded(Bitmap bitmap,
						Picasso.LoadedFrom from) {
					remoteViews.setTextViewText(artistId,
							bundle.getString("artist"));
					remoteViews.setTextViewText(titleId,
							bundle.getString("title"));
					remoteViews.setImageViewBitmap(coverimageId, bitmap);
					notificationManager
							.notify(NOTIFICATION_ID, builder.build());

					// loading of the bitmap was a success
					// TODO do some action with the bitmap
				}

				@Override
				public void onBitmapFailed(Drawable errorDrawable) {
					Log.e(LCAT, "onBitmapFaile");

				}

				@Override
				public void onPrepareLoad(Drawable placeHolderDrawable) {
				}
			};
			Picasso.with(ctx).load(image).into(target);

		} else {
			Log.e(LCAT, "image is null in updateNotification ");
		}
	}

	public class NotificationServiceReceiver extends BroadcastReceiver {
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

	private void setButtonListeners(RemoteViews view, Context ctx) {
		Log.d(LCAT, "inside setButtonListeners");
		String pn = "de.appwerft.audiocontrols";
		Intent intent = new Intent();
		intent.setClass(
				ctx,
				de.appwerft.audiocontrols.AudiocontrolsModule.NotificationEventListener.class);
		intent.putExtra("cmd", "play");

		Intent prevIntent = new Intent(
				ctx,
				de.appwerft.audiocontrols.AudiocontrolsModule.NotificationEventListener.class);
		prevIntent.setAction("goPREV");
		Intent nextIntent = new Intent(
				ctx,
				de.appwerft.audiocontrols.AudiocontrolsModule.NotificationEventListener.class);
		prevIntent.setAction("goNEXT");

		// https://developer.android.com/reference/android/widget/RemoteViews.html#setOnClickPendingIntent(int,%20android.app.PendingIntent)
		/* for every event we define a pendingIntent with embedded intent */

		PendingIntent pPrev = PendingIntent.getBroadcast(ctx, REQUEST_CODE,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(prevcontrolId, pPrev);
		PendingIntent pNext = PendingIntent.getBroadcast(ctx, REQUEST_CODE,
				nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(nextcontrolId, pNext);
		view.setOnClickPendingIntent(
				playcontrolId,
				PendingIntent.getBroadcast(ctx, REQUEST_CODE, new Intent(pn
						+ ".PLAY"), PendingIntent.FLAG_UPDATE_CURRENT));
	}
}
