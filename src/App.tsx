import React from 'react';
import {SafeAreaProvider} from 'react-native-safe-area-context';
import Player from './components/Player';

const App = () => (
  <SafeAreaProvider>
    <Player />
  </SafeAreaProvider>
);

export default App;
