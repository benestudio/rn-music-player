import {useRef, useState} from 'react';
import Sound from 'react-native-sound';
import {
  NativeModules,
  NativeEventEmitter,
  EmitterSubscription,
} from 'react-native';

const {PianoPlayerModule} = NativeModules;

const eventEmitter = new NativeEventEmitter(PianoPlayerModule);

const fadeOutSound = (note: Sound, volume = 10) => {
  if (volume > 0) {
    note.setVolume((volume - 2) / 10);
    requestAnimationFrame(() => fadeOutSound(note, volume - 2));
  } else {
    note.stop();
    note.release();
  }
};

interface SoundSchedule {
  sound: Sound | null;
  time: number;
  beatIndex: number;
}

const usePlayer = () => {
  const [playingNote, setPlayingNote] = useState(-1);
  const [isPlaying, setIsPlaying] = useState(false);
  const eventListener = useRef<EmitterSubscription | null>(null);

  const play = async (notes: number[][], tempo = 120) => {
    eventListener.current = eventEmitter.addListener('noteChange', ({num}) => {
      setPlayingNote(num);
    });
    PianoPlayerModule.play(
      notes.map(note => ({
        notes: note,
      })),
      tempo,
      () => {
        stop();
      },
    );
    setIsPlaying(true);
  };

  const stop = () => {
    eventListener.current!.remove();
    PianoPlayerModule.stop();
    setIsPlaying(false);
    setPlayingNote(-1);
  };

  const playPitch = (pitchIndex: number) => {
    /* const note = new Sound(`piano${pitchIndex}.mp3`, Sound.MAIN_BUNDLE, () => {
      note.play();
      setTimeout(() => {
        fadeOutSound(note);
      }, 600);
    }); */
  };

  return [playingNote, isPlaying, playPitch, play, stop] as [
    number,
    boolean,
    (pitchIndex: number) => void,
    (notes: number[][]) => void,
    () => void,
  ];
};

export default usePlayer;
