import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:flutter_bluetooth_serial/flutter_bluetooth_serial.dart';

void main() {
  runApp(const HC05RemoteApp());
}

class HC05RemoteApp extends StatelessWidget {
  const HC05RemoteApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'hc05 module controller',
      theme: ThemeData.dark(),
      home: const BluetoothHomePage(),
      debugShowCheckedModeBanner: false,
    );
  }
}

class BluetoothHomePage extends StatefulWidget {
  const BluetoothHomePage({super.key});

  @override
  State<BluetoothHomePage> createState() => _BluetoothHomePageState();
}

class _BluetoothHomePageState extends State<BluetoothHomePage> {
  BluetoothDevice? selectedDevice;
  BluetoothConnection? connection;
  bool isConnected = false;
  bool isConnecting = false;

  @override
  void initState() {
    super.initState();
    FlutterBluetoothSerial.instance.state.then((state) {
      if (state == BluetoothState.STATE_OFF) {
        _requestEnableBluetooth();
      }
    });
  }

  Future<void> _requestEnableBluetooth() async {
    await FlutterBluetoothSerial.instance.requestEnable();
  }

  Future<void> _connectToDevice(BluetoothDevice device) async {
    setState(() {
      isConnecting = true;
    });

    try {
      final conn = await BluetoothConnection.toAddress(device.address);
      setState(() {
        connection = conn;
        selectedDevice = device;
        isConnected = true;
      });

      connection!.input!.listen(null).onDone(() {
        setState(() {
          isConnected = false;
          connection = null;
        });
      });
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Cannot connect: $e')),
      );
    }

    setState(() {
      isConnecting = false;
    });
  }

  void _disconnect() {
    connection?.close();
    setState(() {
      isConnected = false;
      connection = null;
    });
  }

  void _sendData(String data) async {
    if (connection != null && isConnected) {
      try {
        connection!.output.add(Uint8List.fromList(data.codeUnits));
        await connection!.output.allSent;
      } catch (e) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error sending data: $e')),
        );
      }
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Not connected to any device')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Center(
          child: Text(
            'hc05 module controller',
            style: TextStyle(fontWeight: FontWeight.bold),
          ),
        ),
        automaticallyImplyLeading: false,
        actions: isConnected
            ? [
                IconButton(
                  icon: const Icon(Icons.bluetooth_disabled),
                  onPressed: _disconnect,
                  tooltip: 'Disconnect',
                )
              ]
            : [],
      ),
      body: Column(
        children: [
          Expanded(
            child: Padding(
              padding: const EdgeInsets.all(20.0),
              child: Column(
                children: [
                  _buildDeviceList(),
                  const SizedBox(height: 20),
                  if (isConnected) _buildControlButtons(),
                ],
              ),
            ),
          ),
          const Padding(
            padding: EdgeInsets.only(bottom: 12.0),
            child: Text(
              'Made by Anand PM',
              style: TextStyle(fontSize: 14, color: Colors.white70),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDeviceList() {
    return FutureBuilder<List<BluetoothDevice>>(
      future: FlutterBluetoothSerial.instance.getBondedDevices(),
      builder: (context, snapshot) {
        if (!snapshot.hasData) {
          return const Center(child: CircularProgressIndicator());
        }

        final devices = snapshot.data!;
        if (devices.isEmpty) {
          return const Center(child: Text('No bonded devices found'));
        }

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'Tap to connect to HC-05:',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.w500),
            ),
            const SizedBox(height: 8),
            ...devices.map(
              (device) => ListTile(
                title: Text(device.name ?? 'Unknown device'),
                subtitle: Text(device.address),
                trailing: selectedDevice == device
                    ? isConnecting
                        ? const CircularProgressIndicator()
                        : const Icon(Icons.check, color: Colors.green)
                    : null,
                onTap: () {
                  if (!isConnected && !isConnecting) {
                    _connectToDevice(device);
                  }
                },
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _buildControlButtons() {
    return Expanded(
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(
              onPressed: () => _sendData('F'),
              style: ElevatedButton.styleFrom(
                shape: const CircleBorder(),
                padding: const EdgeInsets.all(24),
              ),
              child: const Icon(Icons.arrow_upward, size: 40),
            ),
            const SizedBox(height: 20),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                ElevatedButton(
                  onPressed: () => _sendData('L'),
                  style: ElevatedButton.styleFrom(
                    shape: const CircleBorder(),
                    padding: const EdgeInsets.all(24),
                  ),
                  child: const Icon(Icons.arrow_back, size: 40),
                ),
                const SizedBox(width: 40),
                ElevatedButton(
                  onPressed: () => _sendData('S'),
                  style: ElevatedButton.styleFrom(
                    shape: const CircleBorder(),
                    padding: const EdgeInsets.all(24),
                    backgroundColor: Colors.red,
                  ),
                  child: const Icon(Icons.radio_button_checked, size: 40),
                ),
                const SizedBox(width: 40),
                ElevatedButton(
                  onPressed: () => _sendData('R'),
                  style: ElevatedButton.styleFrom(
                    shape: const CircleBorder(),
                    padding: const EdgeInsets.all(24),
                  ),
                  child: const Icon(Icons.arrow_forward, size: 40),
                ),
              ],
            ),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: () => _sendData('B'),
              style: ElevatedButton.styleFrom(
                shape: const CircleBorder(),
                padding: const EdgeInsets.all(24),
              ),
              child: const Icon(Icons.arrow_downward, size: 40),
            ),
          ],
        ),
      ),
    );
  }
}