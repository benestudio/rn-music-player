import {useEffect, useState} from 'react';
import * as PianoPlayer from './PianoPlayer';

const usePlayer = () => {
  const [playingNote, setPlayingNote] = useState<number | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);

  useEffect(() => {
    PianoPlayer.addOnNoteChangeListener(setPlayingNote);
    return () => PianoPlayer.removeOnNoteChangeListener(setPlayingNote);
  }, []);

  const play = async (notes: number[][], tempo = 120) => {
    setIsPlaying(true);
    await PianoPlayer.play(notes, tempo);
    setIsPlaying(false);
    setPlayingNote(null);
  };

  const stop = () => {
    PianoPlayer.stop();
    setIsPlaying(false);
    setPlayingNote(null);
  };

  const playPitch = (pitchIndex: number) => {
    play([[pitchIndex]]);
  };

  return {playingNote, isPlaying, playPitch, play, stop};
};

export default usePlayer;
