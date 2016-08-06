package de.appwerft.audiocontrols;

import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/* it is class for LockscreenView, triggered from LockScreenService*/
public class AudioControlWidget extends RelativeLayout {
	ImageView coverView;
	View container;
	TextView titleView;
	TextView artistView;
	ImageButton playCtrl;
	ImageButton prevCtrl;
	ImageButton nextCtrl;
	Boolean isPlaying;
	int playIcon, stopIcon;
	float xScale, yScale;
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
		container = inflater.inflate(
				getResId("remoteaudiocontrol_lockscreen", "layout"), null);
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

		this.playIcon = getResId("android:drawable/ic_media_play", "drawable");
		this.stopIcon = getResId("android:drawable/ic_media_paue", "drawable");
		float xScale = this.playCtrl.getScaleX();
		float yScale = this.playCtrl.getScaleY();
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

			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			intent.setAction(ctx.getPackageName());
			intent.putExtra(AudiocontrolsModule.AUDIOCONTROL_COMMAND, msg);
			ctx.sendBroadcast(intent);
			/*
			 * Bundle bundle = new Bundle(); bundle.putString("lockscreen",
			 * msg); // resultReceiver.send(100, bundle);
			 */
		}
	};

	public void updateContent(Bundle b) {
		final String imageUrl = b.getString("image");
		final String artist = b.getString("artist");
		final String title = b.getString("title");

		if (this.placeholderId > 0 && imageUrl != null) {
			try {
				@SuppressWarnings("unused")
				URL dummy = new URL(imageUrl);
				Picasso.with(ctx).load(imageUrl).placeholder(placeholderId)
						.into(this.coverView);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

		} else
			Log.e(LCAT, "cannot resolve placeholder.png");
		if (title != null)
			this.titleView.setText(title);
		if (artist != null)
			this.artistView.setText(artist);
		if (b.getString("state") != null) {
			final int state = Integer.parseInt(b.getString("state"));

			if (state == AudiocontrolsModule.STATE_PLAYING) {
				this.playCtrl.setImageResource(stopIcon);
			}
			if (state == AudiocontrolsModule.STATE_STOP) {
				this.playCtrl.setImageResource(playIcon);
			}
			this.playCtrl.setScaleX(xScale);
			this.playCtrl.setScaleY(yScale);

		}
	}

}