type OnNoteChangeListener = (num: number) => void;

let listeners: OnNoteChangeListener[] = [];

export const play = (notes: number[][], tempo = 120) =>
  new Promise<void>(resolve => {
    console.log('playing', { notes, tempo });
    resolve();
  });

export const stop = () => null;

export const addOnNoteChangeListener = (listener: OnNoteChangeListener) => {
  listeners.push(listener);
};

export const removeOnNoteChangeListener = (listener: OnNoteChangeListener) => {
  listeners = listeners.filter(l => l !== listener);
};
