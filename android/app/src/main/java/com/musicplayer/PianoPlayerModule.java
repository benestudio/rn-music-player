package com.musicplayer;

import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;

public class PianoPlayerModule extends ReactContextBaseJavaModule {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final HashMap<String, Integer> soundIds = new HashMap<>();
    private SoundPool soundPool;
    private HashMap<Integer, Integer> streamIds = new HashMap<>();

    public PianoPlayerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        initializeSoundPool();
        loadSounds();
    }

    @Override
    @NonNull
    public String getName() {
        return "PianoPlayerModule";
    }

    @ReactMethod
    public void play(ReadableArray beats, double tempo, Promise promise) {
        stop();
        streamIds = new HashMap<>();
        double noteDuration = 60.0f / tempo * 1000;
        for (int i = 0; i < beats.size(); i++) {
            ReadableArray notes = beats.getArray(i);
            double delay = i * noteDuration;
            int streamId = i;
            handler.postDelayed(() -> playSound(notes, streamId), (long) delay);
            handler.postDelayed(() -> stopSound(notes, streamId), (long) (delay + noteDuration + 100));
        }
        double endTime = noteDuration * beats.size();
        handler.postDelayed(() -> promise.resolve(null), (long) endTime);
    }

    @ReactMethod
    public void stop() {
        handler.removeCallbacksAndMessages(null);
        if (soundPool != null) {
            for (Integer streamId : streamIds.values()) {
                soundPool.stop(streamId);
            }
        }
        streamIds.clear();
    }

    private void initializeSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(15)
                .setAudioAttributes(audioAttributes)
                .build();
    }

    private void loadSounds() {
        for (int i = 0; i <= 14; i++) {
            String identifier = "piano" + i;
            int sound = getReactApplicationContext().getResources()
                    .getIdentifier(identifier, "raw", getReactApplicationContext().getPackageName());
            soundIds.put(identifier, soundPool.load(getReactApplicationContext(), sound, 1));
        }
    }

    private void sendOnNoteChangeEvent(int beat) {
        WritableMap params = Arguments.createMap();
        params.putInt("num", beat);
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("noteChange", params);
    }

    private void playSound(ReadableArray pianoNotes, int streamId) {
        sendOnNoteChangeEvent(streamId);
        for (int i = 0; i < pianoNotes.size(); i++) {
            int pianoNote = pianoNotes.getInt(i);
            int soundId = soundIds.get("piano" + pianoNote);
            int stream = soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
            streamIds.put(streamId + i * 10000, stream);
        }
    }

    private void stopSound(ReadableArray pianoNotes, int streamId) {
        for (int i = 0; i < pianoNotes.size(); i++) {
            soundPool.stop(streamIds.get(streamId + i * 10000));
            streamIds.remove(streamId + i * 10000);
        }
    }
}
