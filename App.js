/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  Platform,
  StyleSheet,
  Text,
  TouchableOpacity,
  NativeModules,
  View
} from 'react-native';
let toast = NativeModules.ToastNative;

const instructions = Platform.select({
  ios: 'Press Cmd+R to reload,\n' +
    'Cmd+D or shake for dev menu',
  android: 'Double tap R on your keyboard to reload,\n' +
    'Shake or press menu button for dev menu',
});

export default class App extends Component<{}> {
  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.title}>react-native 调用android原生模块</Text>
        <TouchableOpacity onPress={()=>{
          toast.show('Toast message',toast.SHORT);
        }}>
          <Text style={styles.btn}>Click Me</Text>
        </TouchableOpacity>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  title:{
    fontSize:16,
  },
  btn:{
    fontSize:16,
    paddingVertical:7,
    paddingHorizontal:10,
    borderColor:'#f00',
    borderWidth:1,
    borderRadius:5,
    marginTop:10,
    color:'#f00'
  }
});
