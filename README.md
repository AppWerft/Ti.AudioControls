#Ti.AudioControl


This Titanium module is for control of Ti.Media.Audioplayer (or Kosso's' player) by headset, lockscreen and/or notification bar.

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
![](https://raw.githubusercontent.com/AppWerft/Ti.AudioControls/master/assets/audiocontrol.png)


In older devices it opens a view over lockscreen  – in new devices an interactive notifiaction.

<img src="https://raw.githubusercontent.com/AppWerft/Ti.AudioControls/master/assets/lsr.png" width="600">

[Details here (on bottom of page)](https://developer.android.com/about/versions/android-5.0-changes.html#Lockscreen+widget+support+removed)


