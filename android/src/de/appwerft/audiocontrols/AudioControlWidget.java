package de.appwerft.audiocontrols;

import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/* it is class for LockscreenView, triggered from LockScreenService*/
public class AudioControlWidget extends RelativeLayout {
	private GestureDetectorCompat gestureDetector;
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

	public AudioControlWidget(Context ctx) {
		super(ctx);
		isPlaying = true;
		this.ctx = ctx;
		LayoutInflater inflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		container = inflater.inflate(
				R("remoteaudiocontrol_lockscreen", "layout"), null);
		this.coverView = (ImageView) container.findViewById(R("coverimage",
				"id"));
		this.artistView = (TextView) container.findViewById(R("artist", "id"));
		this.titleView = (TextView) container.findViewById(R("title", "id"));
		this.prevCtrl = (ImageButton) container.findViewById(prevCtrlId = R(
				"prevcontrol", "id"));
		this.playCtrl = (ImageButton) container.findViewById(playCtrlId = R(
				"playcontrol", "id"));
		this.nextCtrl = (ImageButton) container.findViewById(nextCtrlId = R(
				"nextcontrol", "id"));
		this.playIcon = R("android:drawable/ic_media_play", "drawable");
		this.stopIcon = R("android:drawable/ic_media_pause", "drawable");
		this.xScale = this.playCtrl.getScaleX();
		this.yScale = this.playCtrl.getScaleY();
		/* activating of control buttons: */
		this.playCtrl.setOnClickListener(buttonListener);
		this.prevCtrl.setOnClickListener(buttonListener);
		this.nextCtrl.setOnClickListener(buttonListener);
		this.placeholderId = R("placeholder", "drawable");
		this.addView(container);
		gestureDetector = new GestureDetectorCompat(ctx,
				new LockScreenWidgetGestureListener());

	}

	private OnClickListener buttonListener = new OnClickListener() {
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
			Log.d(LCAT, "parsed " + state);
			if (state == AudiocontrolsModule.STATE_PLAYING) {
				this.playCtrl.setImageResource(stopIcon);
			} else if (state == AudiocontrolsModule.STATE_STOP) {
				this.playCtrl.setImageResource(playIcon);
			} else {
				Log.e(LCAT, "missing symbol");
			}
			this.playCtrl.setScaleX(xScale);
			this.playCtrl.setScaleY(yScale);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	class LockScreenWidgetGestureListener extends
			GestureDetector.SimpleOnGestureListener {
		private static final String DEBUG_TAG = "🔫";

		@Override
		public boolean onDown(MotionEvent event) {
			return true;
		}

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2,
				float velocityX, float velocityY) {

			Log.d(DEBUG_TAG, "onFling: " + event1.toString());
			return true;
		}
	}

}