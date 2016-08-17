package de.appwerft.audiocontrols;

import java.net.MalformedURLException;
import java.net.URL;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.squareup.picasso.Picasso;

/* it is class for LockscreenView, triggered from LockScreenService*/
public class AudioControlCover extends RelativeLayout {
	private ImageView coverImage;
	private View container;
	float measuredWidth;
	float measuredHeight;
	Context ctx;// = TiApplication.getInstance().getApplicationContext();
	final String LCAT = "LockAudioScreen ♛♛♛";

	public AudioControlCover(Context ctx) {
		super(ctx);
		this.ctx = ctx;
		LayoutInflater inflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		container = inflater.inflate(R("remoteaudiocontrol_cover", "layout"),
				null);
		this.coverImage = (ImageView) container.findViewById(R("coverimage",
				"id"));
		this.addView(container);
	}

	public void updateContent(Bundle b) {
		final String imageUrl = b.getString("image");
		try {
			@SuppressWarnings("unused")
			URL dummy = new URL(imageUrl);
			Picasso.with(ctx).load(imageUrl).into(this.coverImage);
		} catch (MalformedURLException e) {
			e.printStackTrace();
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

	/**
	 * Measure with a specific aspect ratio<br />
	 * <br />
	 * 
	 * @param widthMeasureSpec
	 *            The width <tt>MeasureSpec</tt> passed in your
	 *            <tt>View.onMeasure()</tt> method
	 * @param heightMeasureSpec
	 *            The height <tt>MeasureSpec</tt> passed in your
	 *            <tt>View.onMeasure()</tt> method
	 * @param aspectRatio
	 *            The aspect ratio to calculate measurements in respect to
	 */
	public void measure(int widthMeasureSpec, int heightMeasureSpec,
			double aspectRatio) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = widthMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE
				: MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = heightMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE
				: MeasureSpec.getSize(heightMeasureSpec);

		if (heightMode == MeasureSpec.EXACTLY
				&& widthMode == MeasureSpec.EXACTLY) {
			/*
			 * Possibility 1: Both width and height fixed
			 */
			measuredWidth = widthSize;
			measuredHeight = heightSize;

		} else if (heightMode == MeasureSpec.EXACTLY) {
			/*
			 * Possibility 2: Width dynamic, height fixed
			 */
			measuredWidth = (int) Math.min(widthSize, heightSize * aspectRatio);
			measuredHeight = (int) (measuredWidth / aspectRatio);

		} else if (widthMode == MeasureSpec.EXACTLY) {
			/*
			 * Possibility 3: Width fixed, height dynamic
			 */
			measuredHeight = (int) Math
					.min(heightSize, widthSize / aspectRatio);
			measuredWidth = (int) (measuredHeight * aspectRatio);

		} else {
			/*
			 * Possibility 4: Both width and height dynamic
			 */
			if (widthSize > heightSize * aspectRatio) {
				measuredHeight = heightSize;
				measuredWidth = (int) (measuredHeight * aspectRatio);
			} else {
				measuredWidth = widthSize;
				measuredHeight = (int) (measuredWidth / aspectRatio);
			}

		}
	}
}