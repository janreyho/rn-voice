# rn-voice

baidu voice for react-native

automatically link the NativeModule</p>

```sh
react-native link rn-voice
```

### Manually Link Android

- In `android/setting.gradle`

```gradle
...
include ':rn-voice'
project(':rn-voice').projectDir = new File(rootProject.projectDir, '../node_modules/rn-voice/android')
```

- In `android/app/build.gradle`

```gradle
...
dependencies {
    ...
    compile project(':rn-voice')
}
```

- In `MainApplication.java`

```java
import com.facebook.react.ReactApplication
import com.facebook.react.ReactPackage;
...
import com.janreyho.voice.VoicePackage; // <------ Add this!
...

public class MainActivity extends ReactActivity {
...
    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
        new MainReactPackage(),
        new VoicePackage() // <------ Add this!
        );
    }
}
```

### Manually Link iOS

- Drag the Voice.xcodeproj from the rn-voice/ios folder to the Libraries group on Xcode in your poject. [Manual linking](https://facebook.github.io/react-native/docs/linking-libraries-ios.html)
- Click on your main project file (the one that represents the .xcodeproj) select Build Phases and drag the static library, lib.Voice.a, from the Libraries/Voice.xcodeproj/Products folder to Link Binary With Libraries

### Example

```javascript
import Voice from 'rn-voice';
import React, {Component} from 'react';

class VoiceTest extends Component {
  constructor(props) {
    Voice.onSpeechStart = this.onSpeechStartHandler.bind(this);
    Voice.onSpeechEnd = this.onSpeechEndHandler.bind(this);
    Voice.onSpeechResults = this.onSpeechResultsHandler.bind(this);
  }
  onStartButtonPress(e){
    Voice.start('en');
  }
  ...
}
```

**All methods _now_ return a `new Promise` for `async/await` compatibility.**

| Method Name                | Description                              | Platform     |
| -------------------------- | ---------------------------------------- | ------------ |
| Voice.isAvailable()        | Checks whether a speech recognition service is available on the system. | Android, iOS |
| Voice.start(locale)        | Starts listening for speech for a specific locale. Returns null if no error occurs. | Android, iOS |
| Voice.stop()               | Stops listening for speech. Returns null if no error occurs. | Android, iOS |
| Voice.cancel()             | Cancels the speech recognition. Returns null if no error occurs. | Android, iOS |
| Voice.destroy()            | Destroys the current SpeechRecognizer instance. Returns null if no error occurs. | Android, iOS |
| Voice.removeAllListeners() | Cleans/nullifies overridden `Voice` static methods. | Android, iOS |
| Voice.isRecognizing()      | Return if the SpeechRecognizer is recognizing. | Android, iOS |

<h2 align="center">Events</h2>

<p align="center">Callbacks that are invoked when a native event emitted.</p>

| Event Name                          | Description                              | Event                                    | Platform     |
| ----------------------------------- | ---------------------------------------- | ---------------------------------------- | ------------ |
| Voice.onSpeechStart(event)          | Invoked when `.start()` is called without error. | `{ error: false }`                       | Android, iOS |
| Voice.onSpeechRecognized(event)     | Invoked when speech is recognized.       | `{ error: false }`                       | Android, iOS |
| Voice.onSpeechEnd(event)            | Invoked when SpeechRecognizer stops recognition. | `{ error: false }`                       | Android, iOS |
| Voice.onSpeechError(event)          | Invoked when an error occurs.            | `{ error: Description of error as string }` | Android, iOS |
| Voice.onSpeechResults(event)        | Invoked when SpeechRecognizer is finished recognizing. | `{ value: [..., 'Speech recognized'] }`  | Android, iOS |
| Voice.onSpeechPartialResults(event) | Invoked when any results are computed.   | `{ value: [..., 'Partial speech recognized'] }` | Android, iOS |
| Voice.onSpeechVolumeChanged(event)  | Invoked when pitch that is recognized changed. | `{ value: pitch in dB }`                 | Android      |



### Android

While the included `VoiceTest` app works without explicit permissions checks and requests, it may be necessary to add a permission request for `RECORD_AUDIO` for some configurations.
Since Android M (6.0), [user need to grant permission at runtime (and not during app installation)](https://developer.android.com/training/permissions/requesting.html).
By default, calling the `startSpeech` method will invoke `RECORD AUDIO` permission popup to the user. This can be disabled by passing `REQUEST_PERMISSIONS_AUTO: true` in the options argument.

### iOS

Need to include permissions for `NSMicrophoneUsageDescription` and `NSSpeechRecognitionUsageDescription` inside Info.plist for iOS. See the included `VoiceTest` for how to handle these cases.

```xml
<dict>
  ...
  <key>NSMicrophoneUsageDescription</key>
  <string>Description of why you require the use of the microphone</string>
  <key>NSSpeechRecognitionUsageDescription</key>
  <string>Description of why you require the use of the speech recognition</string>
  ...
</dict>
```