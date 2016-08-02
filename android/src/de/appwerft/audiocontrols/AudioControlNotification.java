package de.appwerft.audiocontrols;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

class AudioControlNotification {
	Context ctx;
	Notification notification;
	int artistId, coverimageId, titleId, prevcontrolId, nextcontrolId,
			playcontrolId;

	public AudioControlNotification(Context ctx) {
		this.ctx = ctx;
		RemoteViews view = new RemoteViews(ctx.getPackageName(), getResId(
				"remoteaudiocontrol_notification", "layout"));
		artistId = getResId("artist", "id");
		titleId = getResId("title", "id");
		prevcontrolId = getResId("prevCtrl", "id");
		nextcontrolId = getResId("nextCtrl", "id");
		playcontrolId = getResId("playCtrl", "id");
		notification = new NotificationCompat.Builder(ctx)
				.setSmallIcon(getResId("notification_icon", "drawable"))
				.setContentTitle("").build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.contentView = view;

		setListeners(view, ctx);

		NotificationManager nm = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(7, notification);
	}

	public void updateContent(String imageUrl, String title, String artist) {
		notification.contentView.setTextViewText(artistId, artist);
		notification.contentView.setTextViewText(titleId, title);

	}

	private void setListeners(RemoteViews view, Context ctx) {
		Intent prev = new Intent("de.appwerft.audiocontrols.prev");
		Intent next = new Intent("de.appwerft.audiocontrols.next");
		Intent play = new Intent("de.appwerft.audiocontrols.play");
		PendingIntent pPrev = PendingIntent.getBroadcast(ctx, 0, prev,
				PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(prevcontrolId, pPrev);
		PendingIntent pNext = PendingIntent.getBroadcast(ctx, 0, next,
				PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(nextcontrolId, pNext);
		PendingIntent pPlay = PendingIntent.getBroadcast(ctx, 0, play,
				PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(playcontrolId, pPlay);
	}

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