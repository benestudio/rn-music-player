import React, {useCallback, useState} from 'react';
import {Button, Pressable, View} from 'react-native';
import Animated, {
  Easing,
  scrollTo,
  useAnimatedRef,
  useDerivedValue,
  useSharedValue,
  withTiming,
} from 'react-native-reanimated';
import {
  SafeAreaProvider,
  useSafeAreaInsets,
} from 'react-native-safe-area-context';
import cloneDeep from 'lodash.clonedeep';

import {totalBars, beatsPerBar, notesInOctave, totalOctaves} from './config';
import styles from './styles';
import {INotes} from './interfaces';
import usePlayer from './usePlayer';

const TEMPO = 120;

const App = () => {
  const insets = useSafeAreaInsets();

  const {
    playingNote,
    isPlaying,
    playPitch,
    play: playTrack,
    stop: stopTrack,
  } = usePlayer();

  const [notes, setNotes] = useState<INotes>(
    Array.from({length: totalBars}).map(() =>
      Array.from({length: beatsPerBar}).map(() => []),
    ),
  );

  const [contentWidth, setContentWidth] = useState(0);
  const scrollViewRef = useAnimatedRef<Animated.ScrollView>();
  const scroll = useSharedValue(0);

  useDerivedValue(() => {
    scrollTo(scrollViewRef, scroll.value, 0, false);
  });

  const togglePlayback = () => {
    if (isPlaying) {
      scroll.value = scroll.value;
      stopTrack();
    } else {
      const beats = notes.reduce((all, bar) => [...all, ...bar], []);
      const offset = -2 * (contentWidth / beats.length);
      scroll.value = offset;
      const noteDuration = (60 / TEMPO) * 1000;
      scroll.value = withTiming(contentWidth + offset, {
        duration: beats.length * noteDuration,
        easing: Easing.linear,
      });
      playTrack(beats, TEMPO);
    }
  };

  const handleToggleNote = useCallback(
    (barIndex: number, beatIndex: number, pitchIndex: number) => {
      if (isPlaying) {
        return;
      }
      const shouldPlay = !notes[barIndex][beatIndex].includes(pitchIndex);
      setNotes(oldNotes => {
        const newNotes = cloneDeep(oldNotes);
        if (shouldPlay) {
          newNotes[barIndex][beatIndex].push(pitchIndex);
        } else {
          newNotes[barIndex][beatIndex].splice(
            newNotes[barIndex][beatIndex].indexOf(pitchIndex),
            1,
          );
        }
        return newNotes;
      });
      if (shouldPlay) {
        playPitch(pitchIndex);
      }
    },
    [isPlaying, notes, playPitch],
  );

  return (
    <View style={styles.container}>
      <Animated.ScrollView
        ref={scrollViewRef}
        horizontal
        style={styles.scrollView}
        contentContainerStyle={{
          paddingLeft: insets.left,
          paddingRight: insets.right,
        }}
        scrollEnabled={!isPlaying}
        onContentSizeChange={setContentWidth}
        showsHorizontalScrollIndicator={false}>
        {Array.from({length: totalBars}).map((_, barIndex) => (
          <View style={styles.bar} key={barIndex}>
            {Array.from({length: beatsPerBar}).map((_, beatIndex) => (
              <View style={styles.beat} key={beatIndex}>
                {Array.from({length: notesInOctave * totalOctaves + 1}).map(
                  (_, pitchIndex) => (
                    <Pressable
                      onPress={() =>
                        handleToggleNote(barIndex, beatIndex, pitchIndex)
                      }
                      style={[
                        styles.pitch,
                        notes[barIndex][beatIndex].includes(pitchIndex)
                          ? styles.selectedPitch
                          : null,
                        isPlaying &&
                        playingNote === barIndex * beatsPerBar + beatIndex
                          ? styles.playingPitch
                          : null,
                        isPlaying &&
                        playingNote === barIndex * beatsPerBar + beatIndex &&
                        notes[barIndex][beatIndex].includes(pitchIndex)
                          ? styles.playingSelectedPitch
                          : null,
                      ]}
                      key={pitchIndex}
                    />
                  ),
                )}
              </View>
            ))}
          </View>
        ))}
      </Animated.ScrollView>
      <View style={styles.tabBar}>
        <Button title={isPlaying ? 'Stop' : 'Play'} onPress={togglePlayback} />
      </View>
    </View>
  );
};

export default () => (
  <SafeAreaProvider>
    <App />
  </SafeAreaProvider>
);
