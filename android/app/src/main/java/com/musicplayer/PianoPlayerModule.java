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
    initializeSoundPool();
    loadSounds();
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
    int i
  ) {
    WritableMap params = Arguments.createMap();
    params.putInt("num", i);
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

  private void loadSounds() {
    for (int i = 0; i <= 14; i++) {
      String identifier = "piano" + i;
      int sound =
        this.reactContext.getResources()
          .getIdentifier(identifier, "raw", this.reactContext.getPackageName());
      soundIds.put(identifier, soundPool.load(this.reactContext, sound, 1));
    }
  }

  private Runnable playSound(
    ReadableArray pianoNotes,
    int streamId
  ) {
    return new Runnable() {
      @Override
      public void run() {
        sendEvent(reactContext, "noteChange", streamId);
        for (int i = 0; i < pianoNotes.size(); i++) {
            int pianoNote = pianoNotes.getInt(i);
            int soundId = soundIds.get("piano" + pianoNote);
            int stream = soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
            streamIds.put(streamId + i * 10000, stream);
        }
      }
    };
  }

  private Runnable stopSound(
    ReadableArray pianoNotes,
    int streamId
  ) {
    return new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < pianoNotes.size(); i++) {
          soundPool.stop(streamIds.get(streamId + i * 10000));
          streamIds.remove(streamId + i * 10000);
        }

      }
    };
  }
  
  private Runnable endPlaying(Callback callback) {
    return new Runnable() {
      @Override
      public void run() {
        callback.invoke();
      }
    };
  }

  @ReactMethod
  public void play(ReadableArray notes, Integer tempo, Callback onEnd) {
    executor.setRemoveOnCancelPolicy(true);
    streamIds = new HashMap<Integer, Integer>();
    scheduledFutures = new ScheduledFuture[notes.size() * 2 + 2];
    float noteDuration = 60.0f / tempo * 1000;
    for (int i = 0; i < notes.size(); i++) {
      ReadableMap note = notes.getMap(i);
      ReadableArray pianoNotes = note.getArray("notes");
      float time = i * noteDuration;
      ScheduledFuture noteStart = executor.schedule(
        playSound(
          pianoNotes,
          i
        ),
        (int)time,
        TimeUnit.MILLISECONDS
      );
      float endTime = time + noteDuration + 100;
      ScheduledFuture noteEnd = executor.schedule(
        stopSound(
          pianoNotes,
          i
        ),
        (int)endTime,
        TimeUnit.MILLISECONDS
      );
      scheduledFutures[i * 2] = noteStart;
      scheduledFutures[i * 2 + 1] = noteEnd;
    }


    float trackEndTime = noteDuration * notes.size();
    ScheduledFuture playEnd = executor.schedule(
      endPlaying(onEnd),
      (int)trackEndTime,
      TimeUnit.MILLISECONDS
    );
    scheduledFutures[notes.size() * 2 + 1] = playEnd;
  }

  @ReactMethod
  public void stop() {
    if (soundPool != null) {
      for (int i = 0; i < scheduledFutures.length; i++) {
        if (scheduledFutures[i] != null) {
          scheduledFutures[i].cancel(false);
        }
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
