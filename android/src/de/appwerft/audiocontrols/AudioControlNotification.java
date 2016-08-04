package de.appwerft.audiocontrols;

import org.appcelerator.kroll.common.Log;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

class AudioControlNotification {
	final int NOTIFICATION_ID = 1337;
	final String LCAT = "LockAudioScreen 😇😇😇";
	Context ctx;
	Notification notification;
	NotificationManager notificationManager;
	RemoteViews view;
	final boolean PLAYING = true;
	final boolean PAUSING = false;
	boolean state = PLAYING;
	final int REQUEST_CODE = 0;

	int artistId, coverimageId, titleId, prevcontrolId, nextcontrolId,
			playcontrolId;
	int playiconId, pauseiconId;

	// https://futurestud.io/blog/picasso-callbacks-remoteviews-and-notifications
	public AudioControlNotification(Context ctx) {
		this.ctx = ctx;
		view = new RemoteViews(ctx.getPackageName(), getResId(
				"remoteaudiocontrol_notification", "layout"));
		artistId = getResId("artist", "id");
		titleId = getResId("title", "id");
		prevcontrolId = getResId("prevCtrl", "id");
		nextcontrolId = getResId("nextCtrl", "id");
		playcontrolId = getResId("playCtrl", "id");

		/* for dynamic update: */
		playiconId = getResId("android:ic_media_play", "drawable");
		pauseiconId = getResId("android:ic_media_pause", "drawable");

		notification = new NotificationCompat.Builder(ctx).setOngoing(true)
				.setSmallIcon(getResId("notification_icon", "drawable"))
				.setOnlyAlertOnce(true).setContentTitle("").build();
		// notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.contentView = view;
		setButtonListeners(view, ctx);
		notificationManager = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, notification);
	}

	public void cancelNotification() {
		Log.d(LCAT, "kill all Notification from this app ☗☗");
		notificationManager.cancelAll();
	}

	public void togglePlayButton() {
		Log.d(LCAT, "togglePlayButton=" + state);
		int id = (state == PLAYING) ? pauseiconId : playiconId;
		Log.d(LCAT, "id=" + id);
		notification.contentView.setImageViewResource(playcontrolId, id);
		state = !state;
		notificationManager.notify(NOTIFICATION_ID, notification);
	};

	public void updateContent(String imageUrl, String title, String artist) {
		Log.d(LCAT, "inside audioControlNotification.updateContent");
		notification.contentView.setTextViewText(artistId, artist);
		notification.contentView.setTextViewText(titleId, title);
		ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton
																// instance
		imageLoader.loadImage(imageUrl, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingComplete(String imageUri, View view,
					Bitmap loadedImage) {
				notification.contentView.setImageViewBitmap(coverimageId,
						loadedImage);
			}
		});
		notificationManager.notify(NOTIFICATION_ID, notification);
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

	/* helper function for safety getting resources */
	private int getResId(String name, String type) {
		int id = 0;
		try {
			id = ctx.getResources().getIdentifier(name, type,
					ctx.getPackageName());
		} catch (Exception e) {
			return id;
		}
		return id;
	}

}