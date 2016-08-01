package de.appwerft.audiocontrols;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RemoteViews;

class AudioControlRemoteViews extends RemoteViews {
	Context ctx;
	View container;
	ImageButton playCtrl;
	ImageButton prevCtrl;
	ImageButton nextCtrl;

	public AudioControlRemoteViews(String packageName, int layoutId) {
		super(packageName, layoutId);
		LayoutInflater inflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		container = inflater.inflate(getResId("remoteaudiocontrol", "layout"),
				null);

	}

	public void setCoverImage(String url) {

	}

	public void setTitle(String title) {
		setTextViewText(getResId("title", "id"), title);
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