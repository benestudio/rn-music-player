import React from 'react';
import {Pressable} from 'react-native';

import {BEATS_PER_BAR} from '../constants';
import styles from './styles';

interface NoteProps {
  onPress: (barIndex: number, beatIndex: number, pitchIndex: number) => void;
  notes: number[][][];
  isPlaying: boolean;
  currentBeat: number | null;
  barIndex: number;
  beatIndex: number;
  pitchIndex: number;
}

const Note = ({
  onPress,
  notes,
  isPlaying,
  currentBeat,
  barIndex,
  beatIndex,
  pitchIndex,
}: NoteProps) => {
  const isSelected = notes[barIndex][beatIndex].includes(pitchIndex);
  const isCurrentBeat =
    isPlaying && currentBeat === barIndex * BEATS_PER_BAR + beatIndex;
  return (
    <Pressable
      onPress={() => onPress(barIndex, beatIndex, pitchIndex)}
      style={[
        styles.pitch,
        isSelected ? styles.selectedPitch : null,
        isCurrentBeat ? styles.playingPitch : null,
        isSelected && isCurrentBeat ? styles.playingSelectedPitch : null,
      ]}
    />
  );
};

export default Note;
