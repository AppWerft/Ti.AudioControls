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
	final String LCAT = "LockAudioScreen ♛♛♛";
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

	@Kroll.method
	public void createLockScreenControl(KrollDict opts) {
		Log.d(LCAT, opts.toString());
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
			lockscreenService = new Intent();
			lockscreenService.setClassName(
					TiApplication.getAppCurrentActivity(),
					"LockScreenService.class");
			lockscreenService.putExtra("title", title);
			lockscreenService.putExtra("artist", artist);
			lockscreenService.putExtra("image", image);
			lockscreenService.setAction(Intent.ACTION_VIEW);
			/* for back communication */
			lockscreenService.putExtra("receiver", resultReceiver);
			TiApplication.getInstance().startService(lockscreenService);
			Log.d(LCAT, "Service for Lockcreen will started");
		} catch (Exception ex) {
			Log.d(LCAT, "Exception caught:" + ex);
		}
	}

	@Kroll.method
	public void setContent(KrollDict opts) {
		// http://stackoverflow.com/questions/15346647/android-passing-variables-to-an-already-running-service
		// Alias to create…
		this.createLockScreenControl(opts);
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
