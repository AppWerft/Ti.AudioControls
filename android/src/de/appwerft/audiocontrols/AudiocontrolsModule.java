package de.appwerft.audiocontrols;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RemoteViews;

@Kroll.module(name = "Audiocontrols", id = "de.appwerft.audiocontrols")
public class AudiocontrolsModule extends KrollModule {
	Context context;
	public static String rootActivityClassName = "";
	final String LCAT = "LockAudioScreen ♛♛♛";
	final int NOTIFICATION_ID = 1;
	static ResultReceiver resultReceiver;
	Intent lockscreenService;

	public AudiocontrolsModule() {
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {
		resultReceiver = new ResultReceiver(null);
	}

	@Override
	public void onDestroy(Activity activity) {
		TiApplication.getInstance().stopService(lockscreenService);
		super.onDestroy(activity);

	}

	private RemoteViews getAudioControlView() {
		// Using RemoteViews to bind custom layouts into Notification
		int layoutId = context.getResources().getIdentifier(
				"remoteaudiocontrol", "layout", context.getPackageName());
		Log.d(LCAT, "layoutId = " + layoutId);
		if (layoutId == 0) {
			return null;
		}
		RemoteViews customenotificationView = new RemoteViews(
				context.getPackageName(), layoutId);

		// Locate and set the Image into customnotificationtext.xml ImageViews
		// notificationView.setImageViewResource(R.id.imagenotileft,
		// R.drawable.ic_launcher);

		// Locate and set the Text into customnotificationtext.xml TextViews
		// notificationView.setTextViewText(R.id.title, getTitle());
		// notificationView.setTextViewText(R.id.text, getText());

		return customenotificationView;
	}

	@Kroll.method
	public void createRemoteAudioControl(KrollDict opts) {
		context = TiApplication.getInstance().getApplicationContext();
		String title = "", artist = "", image = "";
		if (opts != null && opts.containsKeyAndNotNull("title")) {
			title = opts.getString("title");
		}
		if (opts != null && opts.containsKeyAndNotNull("artist")) {
			artist = opts.getString("artist");
		}
		if (opts != null && opts.containsKeyAndNotNull("image")) {
			image = opts.getString("image");
		}
		Resources res = context.getResources();
		String pn = context.getPackageName();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			// Vorbild:
			// http://stackoverflow.com/questions/23222063/android-custom-notification-layout-with-remoteviews
			// http://www.laurivan.com/android-notifications-with-custom-layout/
			Log.d(LCAT, " >= Build.VERSION_CODES.LOLLIPOP");
			Intent intent = new Intent(context, LockScreenService.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);
			int iconId = res.getIdentifier("notification_icon", "drawable", pn);
			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					context).setSmallIcon(iconId)
					.setContentIntent(contentIntent).setContentText("")
					.setAutoCancel(false);

			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			Notification notification;

			notification = builder.build();

			// important: call this after building
			// (http://stackoverflow.com/questions/21237495/create-custom-big-notifications)
			notification.bigContentView = getAudioControlView();

			// for making sticky
			notification.flags |= Notification.FLAG_NO_CLEAR;

			notificationManager.notify(NOTIFICATION_ID, notification);

			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			Button playButton = (Button) inflater.inflate(
					res.getIdentifier("playcontrol", "layout", pn), null);

			playButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(LCAT, ">>>>>>>>>><<<<<<<");
				}
			});

		} else {
			// for preL we open a view over lockscreen:
			Log.d(LCAT, " < Build.VERSION_CODES.LOLLIPOP");
			try {
				Intent intent = new Intent(context, LockScreenService.class);
				intent.putExtra("title", title);
				intent.putExtra("artist", artist);
				intent.putExtra("image", image);
				/* for back communication */
				// lockscreenService.putExtra("receiver", resultReceiver);
				context.startService(intent);
				Log.d(LCAT,
						pn + ".LockScreenService try to start with "
								+ opts.toString());
			} catch (Exception ex) {
				Log.d(LCAT, "Exception caught:" + ex);
			}
		}
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

}
