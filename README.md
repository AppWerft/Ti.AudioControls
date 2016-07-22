#Ti.AudioControl


This Titanium module is for control of Ti.Media.Audioplayer (or Kosso's' player) by headset, lockscreen and/or notification bar.

##Interface

```javascript
var AudioControlModule = require("de.appwerft.audiocontrol");

AudioControlModule.enableHeadsetKeyboard();

var LockScreenControl = AudioControlModule.createLockscreenControl({
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
LockScreenControl.setTitle();
LockScreenControl.setArtist();
LockScreenControl.setImage();
LockScreenControl.setProgress();

```