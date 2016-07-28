package de.appwerft.audiocontrols;

import java.net.MalformedURLException;
import java.net.URL;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class AudioControlWidget extends RelativeLayout {
	ImageView coverView;
	TextView titleView;
	TextView artistView;
	ImageButton playCtrl;
	Context ctx = TiApplication.getInstance().getApplicationContext();
	final String LCAT = "LockAudioScreen ♛♛♛";

	private int getResId(String name, String type) {
		int id = 0;
		try {
			id = ctx.getResources().getIdentifier(name, type,
					ctx.getPackageName());
			Log.d(LCAT, type + "." + name + "=" + id);
		} catch (Exception e) {
			Log.e(LCAT, "getResId: name=" + name + " type=" + type + " "
					+ " pn=" + ctx.getPackageName() + e.getMessage());
		}
		return id;
	}

	public AudioControlWidget(Context ctx) {
		super(ctx);
		LayoutInflater infl = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View container = infl.inflate(getResId("remoteaudiocontrol", "layout"),
				null);

		coverView = (ImageView) findViewWithTag("cover");
		artistView = (TextView) findViewWithTag("artist");

		// titleView = (TextView) findViewWithTag("title");
		// playCtrl = (ImageButton) findViewWithTag("playcontrol");
		this.addView(container);
	}

	public void setCover(String imageUrl) {
		if (coverView == null) {
			Log.e(LCAT, "CoverView is null");
			return;

		}
		Log.d(LCAT, "imageUrl=" + imageUrl);
		// only for testing for right URL syntax:
		try {
			@SuppressWarnings("unused")
			URL url = new URL(imageUrl);
			Picasso.with(ctx).load(imageUrl).into(coverView);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}

	public void setTitle(String title) {
		titleView.setText(title);
	}

	public void setArtist(String artist) {
		artistView.setText(artist);
	}
}