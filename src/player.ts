import { useRef, useState } from "react";
import Sound from "react-native-sound";

const fadeOutSound = (note: Sound, volume = 10) => {
    if (volume > 0) {
        note.setVolume(((volume - 2) / 10));
        requestAnimationFrame(() => fadeOutSound(note, volume - 2));
    } else {
        note.stop();
        note.release();
    }
}

interface SoundSchedule {
    sound: Sound | null,
    time: number,
    beatIndex: number,
};

const player = () => {
    const [playingNote, setPlayingNote] = useState(-1);
    const [isPlaying, setIsPlaying] = useState(false);
    const timeouts = useRef<ReturnType<typeof setTimeout>[]>([]).current;

    const play = async (notes: number[][], tempo = 180) => {
        const promises = notes.map((beat, beatIndex) => {
            return new Promise<SoundSchedule[]>((resolve) => {
                const totalNotes = beat.length;
                let loadedNotes = 0;
                const preloadedSounds: SoundSchedule[] = []
                if (beat.length === 0) {
                    resolve([{
                        sound: null,
                        time: beatIndex * 60 / tempo * 1000,
                        beatIndex,
                    }]);
                    return;
                }
                beat.map((note) => {
                    const sound = new Sound(`piano${note}.mp3`, Sound.MAIN_BUNDLE, () => {
                        loadedNotes++;
                        preloadedSounds.push({
                            sound,
                            time: beatIndex * 60 / tempo * 1000,
                            beatIndex,
                        })
                        if (loadedNotes === totalNotes) {
                            resolve(preloadedSounds);
                        }
                    });
                });
            })
        });
        const sounds = await Promise.all(promises);
        setIsPlaying(true);
        sounds.reduce((all, bar) => [...all, ...bar], []).forEach(sound => {
            const noteTimeout = setTimeout(() => {
                setPlayingNote(sound.beatIndex);
                if (sound.sound) {
                    sound.sound.play();
                    setTimeout(() => {
                        fadeOutSound(sound.sound!);
                    }, 60 / tempo * 1000 + 100);
                }
            }, sound.time);
            timeouts.push(noteTimeout);
        });
        const stopTimeout = setTimeout(stop, notes.length * (60 / tempo) * 1000);
        timeouts.push(stopTimeout);
    };
    
    const stop = () => {
        timeouts.forEach(timeout => {
            clearTimeout(timeout);
        });
        timeouts.length = 0;
        setPlayingNote(-1);
        setIsPlaying(false);
    };

    const playPitch = (pitchIndex: number) => {
        const note = new Sound(`piano${pitchIndex}.mp3`, Sound.MAIN_BUNDLE, () => {
            note.play();
            setTimeout(() => {
                fadeOutSound(note)
            }, 600);
        });
    }

    return [playingNote, isPlaying, playPitch, play, stop] as [number, boolean, (pitchIndex: number) => void, (notes: number[][]) => void, () => void];
};

export default player;
