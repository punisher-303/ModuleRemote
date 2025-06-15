import React, { useEffect, useState } from 'react';
import {
  SafeAreaView,
  Text,
  TouchableOpacity,
  View,
  PermissionsAndroid,
  Platform,
  Alert,
} from 'react-native';
import RNBluetoothClassic from 'react-native-bluetooth-classic';

export default function App() {
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    requestPermissions();
    RNBluetoothClassic.isEnabled()
      .then(enabled => {
        if (!enabled) RNBluetoothClassic.requestEnabled();
      })
      .catch(err => Alert.alert('Bluetooth Error', err.message));
  }, []);

  async function requestPermissions() {
    if (Platform.OS === 'android') {
      const granted = await PermissionsAndroid.requestMultiple([
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
      ]);
      if (granted['android.permission.ACCESS_FINE_LOCATION'] !== 'granted') {
        Alert.alert('Permission required', 'Location permission is needed for Bluetooth to work');
      }
    }
  }

  const connectToHC05 = async () => {
    try {
      const devices = await RNBluetoothClassic.getBondedDevices();
      const hc05 = devices.find(d => d.name === 'HC-05');
      if (hc05) {
        const connectedDevice = await RNBluetoothClassic.connect(hc05.address);
        setConnected(!!connectedDevice);
        Alert.alert('Connected', `Connected to ${hc05.name}`);
      } else {
        Alert.alert('Device not found', 'HC-05 not found');
      }
    } catch (err) {
      Alert.alert('Error', err.message);
    }
  };

  const sendCommand = async (cmd) => {
    if (connected) {
      try {
        await RNBluetoothClassic.write(cmd);
      } catch (err) {
        Alert.alert('Send Error', err.message);
      }
    } else {
      Alert.alert('Not connected', 'Please connect to HC-05 first');
    }
  };

  const Button = ({ label, onPress }) => (
    <TouchableOpacity
      onPress={onPress}
      style={{
        backgroundColor: '#1e90ff',
        padding: 20,
        margin: 10,
        borderRadius: 100,
        alignItems: 'center',
      }}>
      <Text style={{ color: 'white', fontSize: 24 }}>{label}</Text>
    </TouchableOpacity>
  );

  return (
    <SafeAreaView style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#111' }}>
      <Button label="Connect to HC-05" onPress={connectToHC05} />

      <View style={{ flexDirection: 'column', alignItems: 'center' }}>
        <Button label="↑" onPress={() => sendCommand('F')} />
        <View style={{ flexDirection: 'row' }}>
          <Button label="←" onPress={() => sendCommand('L')} />
          <Button label="●" onPress={() => sendCommand('S')} />
          <Button label="→" onPress={() => sendCommand('R')} />
        </View>
        <Button label="↓" onPress={() => sendCommand('B')} />
      </View>
    </SafeAreaView>
  );
}

// Optional helper for connect + send in one step
export const connectAndSend = async (cmd) => {
  try {
    const devices = await RNBluetoothClassic.getBondedDevices();
    const hc05 = devices.find(d => d.name === 'HC-05');
    if (hc05) {
      const connectedDevice = await RNBluetoothClassic.connect(hc05.address);
      if (connectedDevice) {
        await RNBluetoothClassic.write(cmd);
        Alert.alert('Sent', `Sent "${cmd}" to ${hc05.name}`);
      } else {
        Alert.alert('Connection Failed', 'Could not connect to HC-05');
      }
    } else {
      Alert.alert('Device not found', 'HC-05 not found');
    }
  } catch (err) {
    Alert.alert('Error', err.message);
  }
};
