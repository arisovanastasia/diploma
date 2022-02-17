package com.example.android6lbr;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class LbrService extends VpnService {
	private static final String TAG = LbrService.class.getSimpleName();

	public static final String ACTION_CONNECT = "com.example.android.toyvpn.START";
	public static final String ACTION_DISCONNECT = "com.example.android.toyvpn.STOP";

	/** Maximum packet size is constrained by the MTU, which is given as a signed short. */
	private static final int MAX_PACKET_SIZE = Short.MAX_VALUE;

	private PendingIntent mConfigureIntent;
	private ParcelFileDescriptor vpnInterface = null;

	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothLeScanner mBLEScanner;
	private BroadcastReceiver mReceiver = null;

	static MutableLiveData<String> _liveData = new MutableLiveData<String>();
	public static LiveData<String> liveData = _liveData;

	public ArrayList<ConnectThread> bluetoothConnections = new ArrayList<ConnectThread>();;

	BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			Log.d("CONNECT", "onServiceDiscovered: " + gatt.getServices().toString());
			super.onServicesDiscovered(gatt, status);
		}

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			Log.d("CONNECT", "on connection state changed: status: " + status + " newState: " + newState);
			Log.d("MTU", "requested MTU success: " + gatt.requestMtu(212));
			super.onConnectionStateChange(gatt, status, newState);
		}
	};

	@Override
	public void onCreate() {
		// Create the intent to "configure" the connection (just start ToyVpnClient).
		//mConfigureIntent = PendingIntent.getActivity(this, 0, new Intent(this, ToyVpnClient.class),
		//		PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && ACTION_DISCONNECT.equals(intent.getAction())) {
			disconnect();
			return START_NOT_STICKY;
		} else {
			connect();
			return START_STICKY;
		}
	}

	@Override
	public void onDestroy() {
		disconnect();
	}

	private void connect() {
		// This is the code from 6LoWPAN example
		// TODO: maybe move this code to some method like prepare()?
		if(false) {
			// Setup bluetooth beacon detection and automatic connection
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
						checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("This app needs location access");
					builder.setMessage("Please grant location access so this app can detect beacons");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							//requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
						}
					});
					builder.show();
				}
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("This app needs BLE scan access");
					builder.setMessage("Please grant BLE scan access so this app can detect beacons");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							//requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
						}
					});
					builder.show();
				}
			}
		}

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter.startDiscovery();
		mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();

		mReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				//Finding devices
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					Log.d("DEVICE", "found "+ device.getName() + "  ++  " + device.getAddress());

					// if the device is OK to connect - initiate the connection
					if ("nrf52_MIEM".equals(device.getName())){
						mBluetoothAdapter.cancelDiscovery(); // we are advised to do so before attempting to connect

						// Try to establish a GATT connection - seems to fix problems with certain android devices
						BluetoothGatt gatt = device.connectGatt(LbrService.this, false, gattCallback);

						ConnectThread connThread = new ConnectThread(LbrService.this, device);
						bluetoothConnections.add(connThread);
						connThread.start();
					}
				}
			}
		};

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // TODO check if already registered, maybe unregister it at another position in code

		// NOW the VPN example follows

		// Become a foreground service. Background services can be VPN services too, but they can
		// be killed by background check before getting a chance to receive onRevoke().
		updateForegroundNotification("connecting");

		VpnService.Builder builder = new Builder();

		// TODO: pass it as parameters
		try {
			builder.addAllowedApplication("com.example.retrorally");
		} catch (PackageManager.NameNotFoundException e) {
			// do nothing, can't be
		}
		builder.addAddress("2001:d8::1", 64);
		builder.addRoute("2001:d8::", 64);
		builder.setSession("6LoWPAN Service");
		builder.setConfigureIntent(mConfigureIntent);

		vpnInterface = builder.establish();

		FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
		FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());

		// Allocate the buffer for a single packet.
		ByteBuffer packet = ByteBuffer.allocate(MAX_PACKET_SIZE);

		if(false) {
			// Two code examples follow, probably these should be run as separate streams?

			// Example 1: This is how we read the outgoing packet from the input stream.
			try {
				int length = in.read(packet.array());
				if (length > 0) {
					// TODO: pass it down to actual 6LoWPAN clients
					Log.i(TAG, ConnectThread.bytesToHex(packet.array()));
					packet.clear();
				}
			} catch (IOException e) {
				// here we probably should end it all
			}

			// Example 2: This is how to write a packet back to the OS
			try {
				int length = 1000; // packet = /* ANYTHING */;
				// TODO: actually get the packet from the outside, maybe through a function call or anything
				if (length > 0) {
					// Write the incoming packet to the output stream.
					out.write(packet.array(), 0, length);
					packet.clear();
				}
			} catch (IOException e) {
				// here we probably should end it all
			}
		}
	}

	private void disconnect() {
		stopForeground(true);
		try {
			vpnInterface.close();
		} catch (IOException e) {
			// do nothing here
		}
	}

	private void updateForegroundNotification(final String message) {
		final String NOTIFICATION_CHANNEL_ID = "ToyVpn";
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(
				NOTIFICATION_SERVICE);
		mNotificationManager.createNotificationChannel(new NotificationChannel(
				NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID,
				NotificationManager.IMPORTANCE_DEFAULT));
		startForeground(1, new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
				//.setSmallIcon(R.drawable.ic_vpn)
				.setContentText(message)
				.setContentIntent(mConfigureIntent)
				.build());
	}
}
