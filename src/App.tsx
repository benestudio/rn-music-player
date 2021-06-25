import React, { useState } from "react";
import { Button, Pressable, ScrollView, View } from "react-native";
import { SafeAreaProvider, useSafeAreaInsets, } from "react-native-safe-area-context";
import {
  totalBars,
  beatsPerBar,
  notesInOctave,
  totalOctaves,
} from "./config";
import styles from "./styles";
import { INotes } from "./interfaces";
import player from "./player";

const App = () => {
  const insets = useSafeAreaInsets();
  const [playingNote, isPlaying, playPitch, playTrack, stopTrack] = player();
  const [notes, setNotes] = useState<INotes>(
    Array.from({ length: totalBars }).map(() =>
      Array.from({ length: beatsPerBar }).map(() => [])
    )
  );
  const handlePlay = () => {
    if (isPlaying) {
      stopTrack();
    } else {
      playTrack(notes.reduce((all, bar) => [...all, ...bar], []))
    }
  }
  const handleToggleNote = (barIndex: number, beatIndex: number, pitchIndex: number) => {
    setNotes(oldNotes => oldNotes.map((bar, barI) => {
      if (barI !== barIndex) {
        return bar;
      }
      return bar.map((beat, beatI) => {
        if (beatI !== beatIndex) {
          return beat;
        }
        if (beat.includes(pitchIndex)) {
          return beat.filter(pitch => pitch !== pitchIndex);
        }
        playPitch(pitchIndex);
        return [...beat, pitchIndex];
      })
    }))
  }
  return (
    <View style={styles.container}>
      <ScrollView
        horizontal
        style={styles.scrollView}
        contentContainerStyle={{
          paddingLeft: insets.left,
          paddingRight: insets.right,
        }}
        showsHorizontalScrollIndicator={false}
      >
        {Array.from({ length: totalBars }).map((_, barIndex) => (
          <View style={styles.bar} key={barIndex}>
            {Array.from({ length: beatsPerBar }).map((_, beatIndex) => (
              <View style={styles.beat} key={beatIndex}>
                {Array.from({ length: notesInOctave * totalOctaves + 1 }).map((_, pitchIndex) => (
                  <Pressable
                    onPress={() => handleToggleNote(barIndex, beatIndex, pitchIndex)}
                    style={[
                      styles.pitch,
                      notes[barIndex][beatIndex].includes(pitchIndex) ? styles.selectedPitch : null,
                      playingNote === (barIndex * beatsPerBar + beatIndex) ? styles.playingPitch : null,
                      playingNote === (barIndex * beatsPerBar + beatIndex) && notes[barIndex][beatIndex].includes(pitchIndex) ? styles.playingSelectedPitch : null
                    ]}
                    key={pitchIndex}
                  />
                ))}
              </View>
            ))}
          </View>
        ))}
      </ScrollView>
      <View style={styles.tabBar}>
          <Button title={isPlaying ? "Stop" : "Play"} onPress={handlePlay}/>
      </View>
    </View>
  );
}

export default () => (
  <SafeAreaProvider>
    <App />
  </SafeAreaProvider>
);