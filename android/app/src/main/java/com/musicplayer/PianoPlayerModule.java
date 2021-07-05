package com.musicplayer;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Build;
import android.util.Log;
import android.widget.ScrollView;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PianoPlayerModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  private SoundPool soundPool;
  private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
  private HashMap<String, Integer> soundIds = new HashMap<String, Integer>();
  private HashMap<Integer, Integer> streamIds = new HashMap<Integer, Integer>();
  private ScheduledFuture[] scheduledFutures;

  private int totalLoadedSounds = 0;
  private int totalLoadableSounds = 0;

  @Override
  public String getName() {
    return "PianoPlayerModule";
  }

  public PianoPlayerModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  private void initializeSoundPool() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      soundPool =
        new SoundPool.Builder()
          .setMaxStreams(6)
          .setAudioAttributes(
            new AudioAttributes.Builder()
              .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
              .build()
          )
          .build();
    } else soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
  }

  private void sendEvent(
    ReactApplicationContext reactContext,
    String eventName,
    int voice,
    int bar,
    int barNote,
    int num
  ) {
    WritableMap params = Arguments.createMap();
    params.putInt("num", num);
    params.putInt("voice", voice);
    params.putInt("bar", bar);
    params.putInt("barNote", barNote);
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

  private String getNoteName(String noteName) {
    switch (noteName) {
      case "piano":
      case "slap":
        return noteName;
      default:
        return "piano" + noteName;
    }
  }

  @ReactMethod
  public void loadSounds(Promise promise) {
    for (Integer i = 0; i <= 14; i++) {
      String identifier = "piano" + i.toString();
      int sound =
        this.reactContext.getResources()
          .getIdentifier(identifier, "raw", this.reactContext.getPackageName());
      soundIds.put(identifier, soundPool.load(this.reactContext, sound, 1));
    }
    promise.resolve(true);
  }

  private Runnable playSound(
    ReadableArray pianoNotes,
    int streamId
  ) {
    return new Runnable() {
      @Override
      public void run() {
        // if (scoreScroll != null) {
        //   sendEvent(reactContext, "noteChange", voice, bar, barNote, num);
        // }
        for (int i = 0; i < pianoNotes.size(); i++) {
            Integer pianoNote = pianoNotes.getInt(i);
            Integer soundId = soundIds.get("piano" + pianoNote);
            int stream = soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
            streamIds.put(streamId, stream);
        }
        // if (barNote == 0 && scoreScroll != null) {
        //   scoreScroll.smoothScrollTo(0, scrollTo);
        // }
      }
    };
  }

  // private Runnable stopSound(int streamId) {
  //   return new Runnable() {
  //     @Override
  //     public void run() {
  //       soundPool.stop(streamIds.get(streamId));
  //       soundPool.stop(streamIds.get(streamId + 10000));
  //       soundPool.stop(streamIds.get(streamId + 20000));
  //       streamIds.remove(streamId);
  //     }
  //   };
  // }
  //
  // private Runnable endPlaying(Callback callback) {
  //   return new Runnable() {
  //     @Override
  //     public void run() {
  //       callback.invoke();
  //     }
  //   };
  // }

  @ReactMethod
  public void init() {
    if (soundPool == null) {
      initializeSoundPool();
    }
  }

  @ReactMethod
  public void play(ReadableArray notes, Integer tempo, Callback onEnd) {
    // ScrollView scoreScroll = null;
    // if (nativeTag != -1) {
    //   scoreScroll = (ScrollView) getCurrentActivity()
    //     .findViewById(nativeTag);
    // }
    executor.setRemoveOnCancelPolicy(true);
    streamIds = new HashMap<Integer, Integer>();
    scheduledFutures = new ScheduledFuture[notes.size() * 2];
    for (int i = 0; i < notes.size(); i++) {
      // ReadableType type = notes.getType(i);
      ReadableMap note = notes.getMap(i);
      ReadableArray pianoNotes = note.getArray("notes");
      float time = i * (60.0f / tempo * 1000);
      Integer timeInt = (int)time;
      ScheduledFuture noteStart = executor.schedule(
        playSound(
          pianoNotes,
          i
        ),
        timeInt,
        TimeUnit.MILLISECONDS
      );
      // Log.v("time", time.toString());
      // if (pianoNote != "rest") {
      //   ScheduledFuture noteEnd = executor.schedule(
      //     stopSound(notes.size() * voice + i),
      //     time + duration,
      //     TimeUnit.MILLISECONDS
      //   );
      //   scheduledFutures[i * 2 + 1] = noteEnd;
      // }
      // scheduledFutures[i * 2] = noteStart;
      // if (i == notes.size() - 1) {
      //   stopTime = time + duration;
      // }
    }

    // ScheduledFuture playEnd = executor.schedule(
    //   endPlaying(onEnd),
    //   stopTime,
    //   TimeUnit.MILLISECONDS
    // );
    // scheduledFutures[notes.size()] = playEnd;
  }

  @ReactMethod
  public void stop() {
    if (soundPool != null) {
      for (int i = 0; i < scheduledFutures.length; i++) {
        scheduledFutures[i].cancel(false);
      }
      Iterator it = streamIds.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<Integer, Integer> pair = (Map.Entry) it.next();
        soundPool.stop(pair.getValue());
        it.remove();
      }
    }
  }
}
