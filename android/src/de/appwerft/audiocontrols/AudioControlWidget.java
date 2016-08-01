package de.appwerft.audiocontrols;

import java.net.MalformedURLException;
import java.net.URL;

import org.appcelerator.titanium.TiApplication;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class AudioControlWidget extends RelativeLayout {
	ImageView coverView;
	View container;
	TextView titleView;
	TextView artistView;
	ImageButton playCtrl;
	ImageButton prevCtrl;
	ImageButton nextCtrl;
	Boolean isPlaying;
	int playCtrlId, nextCtrlId, prevCtrlId, placeholderId;
	Context ctx;// = TiApplication.getInstance().getApplicationContext();
	final String LCAT = "LockAudioScreen ♛♛♛";

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

	public AudioControlWidget(Context ctx) {
		super(ctx);
		isPlaying = true;
		this.ctx = ctx;
		LayoutInflater inflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		container = inflater.inflate(getResId("remoteaudiocontrol", "layout"),
				null);
		this.coverView = (ImageView) container.findViewById(getResId(
				"coverimage", "id"));
		this.artistView = (TextView) container.findViewById(getResId("artist",
				"id"));
		this.titleView = (TextView) container.findViewById(getResId("title",
				"id"));
		this.prevCtrl = (ImageButton) container
				.findViewById(prevCtrlId = getResId("prevcontrol", "id"));
		this.playCtrl = (ImageButton) container
				.findViewById(playCtrlId = getResId("playcontrol", "id"));
		this.nextCtrl = (ImageButton) container
				.findViewById(nextCtrlId = getResId("nextcontrol", "id"));
		this.playCtrl.setOnClickListener(buttonListener);
		this.prevCtrl.setOnClickListener(buttonListener);
		this.nextCtrl.setOnClickListener(buttonListener);
		this.placeholderId = getResId("placeholder", "drawable");
		this.addView(container);
	}

	private OnClickListener buttonListener = new OnClickListener() {
		@SuppressWarnings("deprecation")
		@Override
		public void onClick(View clicksource) {
			int id = clicksource.getId();
			String msg = "";
			if (id == prevCtrlId)
				msg = "rewind";
			if (id == nextCtrlId)
				msg = "forward";
			if (id == playCtrlId) {
				msg = "play";
				int resId = ctx.getResources().getIdentifier(
						(isPlaying == false) ? "android:ic_media_pause"
								: "android:ic_media_play", "drawable", null);
				if (resId != 0)
					playCtrl.setImageDrawable(ctx.getResources().getDrawable(
							resId));
				isPlaying = !isPlaying;

			}
			Vibrator v = (Vibrator) ctx
					.getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(50);
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			intent.setAction(ctx.getPackageName());
			intent.putExtra("audiocontrolercmd", msg);
			Log.d(LCAT, "try to send a message to KrollModule " + msg);
			ctx.sendBroadcast(intent);
			/*
			 * Bundle bundle = new Bundle(); bundle.putString("lockscreen",
			 * msg); // resultReceiver.send(100, bundle);
			 */
		}
	};

	public void updateContent(String imageUrl, String title, String artist) {
		if (this.placeholderId > 0) {
			try {
				@SuppressWarnings("unused")
				URL url = new URL(imageUrl);
				Picasso.with(ctx).load(imageUrl).placeholder(placeholderId)
						.into(this.coverView);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			this.titleView.setText(title);
			this.artistView.setText(artist);
		} else
			Log.e(LCAT, "cannot resolve placeholder.png");
	}

	public void setTitle(String title) {
		titleView.setText(title);
	}

	public void setArtist(String artist) {
		artistView.setText(artist);
	}

}