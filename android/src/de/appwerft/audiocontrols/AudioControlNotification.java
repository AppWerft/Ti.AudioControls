package de.appwerft.audiocontrols;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.appcelerator.kroll.common.Log;

import com.squareup.picasso.Picasso;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

class AudioControlNotification {
	final int NOTIFICATION_ID = 1337;
	Context ctx;
	Notification notification;
	RemoteViews view;
	final boolean PLAYING = true;
	final boolean PAUSING = false;
	boolean state = PLAYING;
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
		playiconId = getResId("android:ic_media_play", "drawable");
		pauseiconId = getResId("android:ic_media_pause", "drawable");
		notification = new NotificationCompat.Builder(ctx)
				.setSmallIcon(getResId("notification_icon", "drawable"))
				.setContentTitle("").build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.contentView = view;

		setButtonListeners(view, ctx);

		NotificationManager nm = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(NOTIFICATION_ID, notification);
	}

	public void updateContent(String imageUrl, String title, String artist) {
		notification.contentView.setTextViewText(artistId, artist);
		notification.contentView.setTextViewText(titleId, title);
		// notification.contentView.setImageViewBitmap(coverimageId,
		// getImageBitmap(imageUrl));
		Picasso.with(ctx).load(imageUrl)
				.into(view, coverimageId, NOTIFICATION_ID, notification);

	}

	@SuppressWarnings("unused")
	private Bitmap getImageBitmap(String url) {
		Bitmap bitmap = null;
		try {
			URLConnection conn = new URL(url).openConnection();
			conn.connect();
			InputStream inputStream = conn.getInputStream();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(
					inputStream);
			bitmap = BitmapFactory.decodeStream(bufferedInputStream);
			bufferedInputStream.close();
			inputStream.close();
		} catch (IOException e) {
			Log.e("AudioCtrlNotification", "Error getting bitmap", e);
		}
		return bitmap;
	}

	public void togglePlayButton() {
		notification.contentView.setImageViewResource(playcontrolId,
				(state == PLAYING) ? pauseiconId : playiconId);
		state = !state;

	};

	private void setButtonListeners(RemoteViews view, Context ctx) {
		Intent prevIntent = new Intent("de.appwerft.audiocontrols.prev"/* Action */);
		Intent nextIntent = new Intent("de.appwerft.audiocontrols.next");
		Intent playIntent = new Intent("de.appwerft.audiocontrols.play");
		// https://developer.android.com/reference/android/widget/RemoteViews.html#setOnClickPendingIntent(int,%20android.app.PendingIntent)
		PendingIntent pPrev = PendingIntent.getBroadcast(ctx, 0, prevIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(prevcontrolId, pPrev);

		PendingIntent pNext = PendingIntent.getBroadcast(ctx, 0, nextIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(nextcontrolId, pNext);

		PendingIntent pPlay = PendingIntent.getBroadcast(ctx, 0, playIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(playcontrolId, pPlay);
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