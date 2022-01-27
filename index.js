import React from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  Button,
  NativeModules
} from 'react-native';

const Navigation = NativeModules.NavigationModule
class MainScreen extends React.Component {
  render() {

    console.log('The React Native app is running')

    return (
      <View style={styles.container}>
        <Text style={styles.hello}>React Native page</Text>
        <Button title="Connect to Printer" onPress={() => Navigation.connect()} />
        <View style={styles.space} />
        <Button title="Print PDF" onPress={() => Navigation.print()} />
		    
      </View>
    );
  }
}
var styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  hello: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10
  },
  space: {
    width: 20, // or whatever size you need
    height: 20,
  }
});

AppRegistry.registerComponent(
  'MainScreen', // Name of the component for the Android side to pick up
  () => MainScreen 
);