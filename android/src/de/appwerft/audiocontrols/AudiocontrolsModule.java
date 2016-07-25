package de.appwerft.audiocontrols;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import android.app.Activity;
import android.content.Intent;
import android.os.ResultReceiver;

@Kroll.module(name = "Audiocontrols", id = "de.appwerft.audiocontrols")
public class AudiocontrolsModule extends KrollModule {
	public static String rootActivityClassName = "";
	static ResultReceiver resultReceiver;
	Intent intent;

	public AudiocontrolsModule() {
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {
		resultReceiver = new ResultReceiver(null);
	}

	@Override
	public void onDestroy(Activity activity) {
		TiApplication.getInstance().stopService(intent);
		super.onDestroy(activity);

	}

	@Kroll.method
	public void createLockScreenControl(KrollDict opts) {
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
		try {
			intent = new Intent(TiApplication.getAppCurrentActivity(),
					LockScreenService.class);
			intent.putExtra("title", title);
			intent.putExtra("artist", artist);
			intent.putExtra("image", image);
			/* for back communication */
			intent.putExtra("receiver", resultReceiver);
			TiApplication.getInstance().startService(intent);
		} catch (Exception ex) {
			Log.d("AudioControls", "Exception caught:" + ex);
		}
	}

	@Kroll.method
	public void setContent(KrollDict opts) {
		// http://stackoverflow.com/questions/15346647/android-passing-variables-to-an-already-running-service
		this.createLockScreenControl(opts);
	}

	@Override
	public void onStart(Activity activity) {
		rootActivityClassName = TiApplication.getInstance()
				.getApplicationContext().getPackageName()
				+ "."
				+ TiApplication.getAppRootOrCurrentActivity().getClass()
						.getSimpleName();
		super.onStart(activity);
	}

}
