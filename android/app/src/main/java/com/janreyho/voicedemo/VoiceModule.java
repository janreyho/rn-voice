package com.janreyho.voicedemo; //包名

import com.janreyho.voicedemo.recognization.RecogResult;
import com.janreyho.voicedemo.control.ErrorTranslation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.react.ReactActivity;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.modules.core.PermissionListener;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Locale;

import javax.annotation.Nullable;

public class VoiceModule extends ReactContextBaseJavaModule{

  final ReactApplicationContext reactContext;
  EventManager eventManager;
  private boolean isRecognizing = false;
  private String locale = null;

  public VoiceModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;

    eventManager = EventManagerFactory.create(this.reactContext ,"asr");
    eventManager.registerListener(new EventListener() {
        @Override
        public void onEvent(String name, String s1, byte[] bytes, int i, int i1) {
            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_READY)) {
                    WritableMap event = Arguments.createMap();
                    event.putBoolean("error", false);
                    sendEvent("onSpeechStart", event);
                    Log.d("ASR", "onSpeechStart()");
               }
            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_BEGIN)) {
                WritableMap event = Arguments.createMap();
                event.putBoolean("error", false);
                sendEvent("onSpeechRecognized", event);
                Log.d("ASR", "onSpeechRecognized()");
           }
           if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)) {
                RecogResult recogResult = RecogResult.parseJson(s1);
                WritableMap event = Arguments.createMap();
                if (recogResult.hasError()) {
                    int errorCode = recogResult.getError();
                    int subErrorCode = recogResult.getSubError();
                    String errorMessage = String.format("%d/%s", subErrorCode, ErrorTranslation.recogError(errorCode));
                    WritableMap error = Arguments.createMap();
                    error.putString("message", errorMessage);
                    event.putMap("error", error);
                    sendEvent("onSpeechError", event);
                    Log.d("ASR", "onSpeechError() - " + errorMessage);
                }else{
                    event.putBoolean("error", false);
                    sendEvent("onSpeechEnd", event);
                    Log.d("ASR", "onSpeechEnd()");
                    isRecognizing = false;
                }

           }
            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                WritableArray arr = Arguments.createArray();
                String obj = getMatcher(s1, MatcherRegex.RESULT, 1);
                arr.pushString(obj);

                WritableMap event = Arguments.createMap();
                event.putArray("value", arr);

                RecogResult _rr = RecogResult.parseJson(s1);
                String[] results = _rr.getResultsRecognition();
                if (_rr.isFinalResult()) {
                    sendEvent("onSpeechResults", event);
                    Log.d("ASR", "onSpeechResults()");
                }else if (_rr.isPartialResult()){
                    sendEvent("onSpeechPartialResults", event);
                    Log.d("ASR", "onSpeechPartialResults()");
                }
            }
            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_END)) {
                WritableArray arr = Arguments.createArray();
                String obj = getMatcher(s1, MatcherRegex.RESULT, 1);
                arr.pushString(obj);

                WritableMap event = Arguments.createMap();
                event.putArray("value", arr);
                sendEvent("onSpeechResults", event);
                Log.d("ASR", "onSpeechResults()");
           }
            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_VOLUME)) {
//                RecogResult _rr = RecogResult.parseJson(s1);
                try{
                    JSONObject _obj = new JSONObject(s1);
                    double _vol = (int)_obj.get("volume-percent");
                    WritableMap event = Arguments.createMap();
                    event.putDouble("value", _vol);
                    sendEvent("onSpeechVolumeChanged", event);
                    Log.d("ASR", "onSpeechVolumeChanged()");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
           }
        }
    });

  }

    static class MatcherRegex {
        public static final String RESULT = ".*\\{\"word\":\\[\"(.*?)\"\\]\\}.*";
    }

    private String getMatcher(String info,String regex, int index) {
        Matcher m = Pattern.compile(regex).matcher(info);
        m.find();
        try {
            return m.group(index);
        } catch (RuntimeException e) {
            return null;
        }
    }


  private String getLocale(String locale) {
    if (locale != null && !locale.equals("")) {
      return locale;
    }

    return Locale.getDefault().toString();
  }


  @Override
  public String getName() {
    return "RCTVoice";
  }

  @ReactMethod
  public void startSpeech(final String locale, final ReadableMap opts, final Callback callback) {
    if (!isPermissionGranted() && opts.getBoolean("REQUEST_PERMISSIONS_AUTO")) {
      String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO};
      if (this.getCurrentActivity() != null) {
        ((ReactActivity) this.getCurrentActivity()).requestPermissions(PERMISSIONS, 1, new PermissionListener() {
          public boolean onRequestPermissionsResult(final int requestCode,
                                                    @NonNull final String[] permissions,
                                                    @NonNull final int[] grantResults) {
            boolean permissionsGranted = true;
            for (int i = 0; i < permissions.length; i++) {
              final boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
              permissionsGranted = permissionsGranted && granted;
            }

            return permissionsGranted;
          }
        });
      }
      return;
    }

    this.locale = locale;

    Handler mainHandler = new Handler(this.reactContext.getMainLooper());
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        try {
          Log.d("ASR", "11startListening()");
           String json = "{\"accept-audio-data\":false,\"disable-punctuation\":false,\"accept-audio-volume\":true,\"pid\":1536}";
           eventManager.send(SpeechConstant.ASR_START, json, null, 0, 0);
          isRecognizing = true;
          callback.invoke(false);
        } catch (Exception e) {
          callback.invoke(e.getMessage());
        }
      }
    });
  }

  @ReactMethod
  public void stopSpeech(final Callback callback) {
    Handler mainHandler = new Handler(this.reactContext.getMainLooper());
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        try {
          eventManager.send(SpeechConstant.ASR_STOP, null, null, 0, 0);
          isRecognizing = false;
          callback.invoke(false);
        } catch(Exception e) {
          callback.invoke(e.getMessage());
        }
      }
    });
  }

  @ReactMethod
  public void cancelSpeech(final Callback callback) {
    Handler mainHandler = new Handler(this.reactContext.getMainLooper());
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        try {
          eventManager.send(SpeechConstant.ASR_CANCEL, null, null, 0, 0);
          isRecognizing = false;
          callback.invoke(false);
        } catch(Exception e) {
          callback.invoke(e.getMessage());
        }
      }
    });
  }

  @ReactMethod
  public void destroySpeech(final Callback callback) {
    Handler mainHandler = new Handler(this.reactContext.getMainLooper());
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        try {
          eventManager.send(SpeechConstant.ASR_STOP, null, null, 0, 0);
          isRecognizing = false;
          callback.invoke(false);
        } catch(Exception e) {
          callback.invoke(e.getMessage());
        }
      }
    });
  }

  @ReactMethod
  public void isSpeechAvailable(final Callback callback) {
    final VoiceModule self = this;
    Handler mainHandler = new Handler(this.reactContext.getMainLooper());
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        try {
          // Boolean isSpeechAvailable = SpeechRecognizer.isRecognitionAvailable(self.reactContext);
          // callback.invoke(isSpeechAvailable, false);
        } catch(Exception e) {
          callback.invoke(false, e.getMessage());
        }
      }
    });
  }

  private boolean isPermissionGranted() {
    String permission = Manifest.permission.RECORD_AUDIO;
    int res = getReactApplicationContext().checkCallingOrSelfPermission(permission);
    return res == PackageManager.PERMISSION_GRANTED;
  }

  @ReactMethod
  public void isRecognizing(Callback callback) {
    callback.invoke(isRecognizing);
  }

  private void sendEvent(String eventName, @Nullable WritableMap params) {
    this.reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

}
