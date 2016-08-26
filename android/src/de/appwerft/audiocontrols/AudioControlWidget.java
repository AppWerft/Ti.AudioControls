package de.appwerft.audiocontrols;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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
	public static final int DIRECTION_UP = 1, DIRECTION_DOWN = -1;

	private GestureDetectorCompat gestureDetector;
	private ImageView coverView;
	private View container;
	private TextView titleView, artistView;
	private ImageButton playCtrl, prevCtrl, nextCtrl;
	Boolean isPlaying;
	int playIcon, stopIcon, playCtrlId, nextCtrlId, prevCtrlId, placeholderId;
	float xScale, yScale;
	Context ctx;// = TiApplication.getInstance().getApplicationContext();
	final String LCAT = "LockAudioScreen ♛♛♛";
	public onFlingListener flingListener;

	public interface onFlingListener {
		public void onFlinged(int direction);
	}

	public AudioControlWidget(Context ctx, onFlingListener flingListener) {
		super(ctx);
		this.flingListener = flingListener;
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
		final ArrayList<String> icons = b.getStringArrayList("icons");

		if (this.placeholderId > 0 && imageUrl != null) {
			try {
				@SuppressWarnings("unused")
				URL dummy = new URL(imageUrl);
				Picasso.with(ctx).load(imageUrl).placeholder(placeholderId)
						.resize(150, 150).into(this.coverView);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

		}
		if (title != null)
			this.titleView.setText(title);
		if (artist != null)
			this.artistView.setText(artist);
		if (icons != null) {
			this.prevCtrl.setImageResource(R(icons.get(0), "drawable"));
			this.playCtrl.setImageResource(R(icons.get(1), "drawable"));
			this.nextCtrl.setImageResource(R(icons.get(2), "drawable"));

			// this.playCtrl.setScaleX(xScale);
			// this.playCtrl.setScaleY(yScale);
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
			int direction = (velocityY < 0) ? DIRECTION_UP : DIRECTION_DOWN;
			if (flingListener != null)
				flingListener.onFlinged(direction);
			return true;
		}
	}

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

}