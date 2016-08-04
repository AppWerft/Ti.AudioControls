#Ti.AudioControl


This Titanium module is for control of Ti.Media.Audioplayer (or Kosso's' player) by headset, lockscreen and/or notification bar.

Thanks to [inFocusmedia  in Kalmar](http://www.infocusmedia.se/app/)    for sponsoring and Jonas Thoor for patience and support. 

 <img src="http://www.infocusmedia.se/wp-content/themes/ifom/images/logo_ifom_01.png" height=40/>


<img src="https://raw.githubusercontent.com/AppWerft/Ti.AudioControls/master/assets/lsr.png" width="600">


View over lockscreen only works for devices ≤ Lollipop and player control as notification only works with API level ≥ Lollipop. Therefore the module uses a property "lollipop" to decide, which widget should work: "WIDGET_NOTIFICATION" or "WIDGET_LOCKSCREEN".

It is still in work.

##Interface

```javascript
var AudioControlModule = require("de.appwerft.audiocontrol");
var LoremIpsum = require("libs/loremipsum");

// audioControl is singleton, therefore we use module for it:
var AudioControls = require("de.appwerft.audiocontrols");
    AudioControls.createRemoteAudioControl({
        onKeypressed : function(_event) {
            console.log(_event);
            AudioControls.updateRemoteAudioControl({
                image : "http://lorempixel.com/120/120/cats" + "?_=" + Math.random(),
                artist : LoremIpsum(10),
                title : LoremIpsum(2)
            });
        },
        lollipop : AudioControls.WIDGET_LOCKSCREEN,
});
```

Your manifest needs to entries:

```xml
<service android:name="de.appwerft.audiocontrols.LockScreenService" android:enabled="true" android:exported="true"/>
<service android:name="de.appwerft.audiocontrols.NotificationService" android:enabled="true" android:exported="true"/>
```


![](https://raw.githubusercontent.com/AppWerft/Ti.AudioControls/master/assets/audiocontrol.png)


In older devices it opens a view over lockscreen  – in new devices an interactive notifiaction.


[Details here (on bottom of page)](https://developer.android.com/about/versions/android-5.0-changes.html#Lockscreen+widget+support+removed)


