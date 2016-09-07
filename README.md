#Ti.AudioControl


This Titanium module is for control of Ti.Media.Audioplayer (or Kosso's' player) by headset, lockscreen and/or notification bar.

Thanks to [inFocusmedia in Kalmar](http://www.infocusmedia.se/app/)  <img src="http://www.infocusmedia.se/wp-content/themes/ifom/images/logo_ifom_01.png" height=20/>
 for sponsoring and Jonas Thoor for patience and support. 

<img src="https://raw.githubusercontent.com/AppWerft/Ti.AudioControls/master/assets/ac1.png" width=240>  <img src="https://raw.githubusercontent.com/AppWerft/Ti.AudioControls/master/assets/ac2.png" width=240>  <img src="https://raw.githubusercontent.com/AppWerft/Ti.AudioControls/master/assets/ac3.png" width=240>

View over lockscreen only works for devices ≤ Lollipop and player control as notification only works with API level ≥ Lollipop. Therefore the module uses a property "lollipop" to decide, which widget should work: "WIDGET_NOTIFICATION" or "WIDGET_LOCKSCREEN".

It is still in work.

The module has 3 functionalities:

1. Building the widgets
2. Updating the content
3. listening to button events
4. listening to hardware buttons on headset


##Interface

```javascript
var AudioControls = require("de.appwerft.audiocontrols");
var icons = [AudioControls.ICON_REWIND, AudioControls.ICON_PAUSE, AudioControls.ICON_FORWARD];
var updateControl = function() {
    AudioControls.updateRemoteAudioControl({
        image : "http://lorempixel.com/1500/1500/cats/" + "?" + new Date().getTime(),
        artist : Lorem(12),
        title : Lorem(4),
        icons : icons
    });
};
var playing = false;
AudioControls.createRemoteAudioControl({
    onClick : function(_event) {
        icons[1] = (icons[1] == AudioControls.ICON_PAUSE) ? AudioControls.ICON_PLAY : AudioControls.ICON_PAUSE;
        updateControl();
        playing = !playing;
        if (_event.cmd == "rew") {
            AudioControls.hideRemoteAudioControl();
            setTimeout(function() {
            }, 3000);
        }
    },
    vibrate : 26,
    title : Lorem(3),
    image : "http://lorempixel.com/250/250/cats/" + "?" + new Date().getTime(),
    artist : Lorem(5),
    icons : [AudioControls.ICON_REWIND, AudioControls.ICON_PAUSE, AudioControls.ICON_FORWARD],
    iconBackgroundColor : "#44aaaa"
});

```

Your manifest needs to entries:

```xml
<service android:name="de.appwerft.audiocontrols.LockScreenService" android:enabled="true" android:exported="true"/>
<service android:name="de.appwerft.audiocontrols.NotificationCompactService" android:enabled="true" android:exported="true"/>

```

<img src="https://raw.githubusercontent.com/AppWerft/Ti.AudioControls/master/assets/lsr.png" width="600">


In older devices it opens a view over lockscreen  – in new devices an interactive notifiaction.


[Details here (on bottom of page)](https://developer.android.com/about/versions/android-5.0-changes.html#Lockscreen+widget+support+removed)


