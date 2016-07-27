#Ti.AudioControl


This Titanium module is for control of Ti.Media.Audioplayer (or Kosso's' player) by headset, lockscreen and/or notification bar.

It is still in work.

##Interface

```javascript
var AudioControlModule = require("de.appwerft.audiocontrol");

AudioControlModule.enableHeadsetKeyboard();
// audioControl is singleton, therefore we use module for it:
AudioControlModule.createRemoteControl({
    artist : "Michael Jackson",
    title : "Awesome track",
    image : "http://",
    keypressed : function(_e) {
        switch (_e.keycode) {
            case AudioControlModule.BUTTON_PLAYPAUSE:
            break;
            case AudioControlModule.BUTTON_SKIPBACK:
            break;
            case AudioControlModule.BUTTON_SKIPFORWARD:
            break;
        }
    
    }
});
AudioControlModule.updateRemoteControl({
    artist : "Michael Jackson",
    title : "Awesome track",
    image : "http://",
});

```
![](https://raw.githubusercontent.com/AppWerft/Ti.AudioControls/master/assets/audiocontrol.png)


In older devices it opens a view over lockscreen  – in new devices an interactive notifiaction.

<img src="https://raw.githubusercontent.com/AppWerft/Ti.AudioControls/master/assets/lsr.png" width="600">

[Details here (on bottom of page)](https://developer.android.com/about/versions/android-5.0-changes.html#Lockscreen+widget+support+removed)


