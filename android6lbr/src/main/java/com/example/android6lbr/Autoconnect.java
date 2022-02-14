package com.example.android6lbr;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.os.Build;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;

public class Autoconnect {
	private final String CONNECT_TAG = "CONN-DeviceTryToConnect";

	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothLeScanner mBLEScanner;
	private Activity mContext;
	private BroadcastReceiver mReceiver = null;

	MutableLiveData<String> _liveData = new MutableLiveData<String>();
	public LiveData<String> liveData = _liveData;

	public ArrayList<ConnectThread> bluetoothConnections = new ArrayList<ConnectThread>();;

	BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			Log.d(CONNECT_TAG, "onServiceDiscovered: " + gatt.getServices().toString());
			super.onServicesDiscovered(gatt, status);
		}

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			Log.d(CONNECT_TAG, "on connection state changed: status: " + status + " newState: " + newState);
			Log.d("MTU", "requested MTU success: " + gatt.requestMtu(212));
			super.onConnectionStateChange(gatt, status, newState);
		}
	};

	public Autoconnect(Activity c){
		mContext = c;

		// Setup bluetooth beacon detection and automatic connection
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
				mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle("This app needs location access");
				builder.setMessage("Please grant location access so this app can detect beacons");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						mContext.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
					}
				});
				builder.show();
			}
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			if (mContext.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle("This app needs BLE scan access");
				builder.setMessage("Please grant BLE scan access so this app can detect beacons");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						mContext.requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
					}
				});
				builder.show();
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
						BluetoothGatt gatt = device.connectGatt(mContext, false, gattCallback);

						ConnectThread connThread = new ConnectThread(Autoconnect.this, device);
						bluetoothConnections.add(connThread);
						connThread.start();
					}
				}
			}
		};

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		mContext.registerReceiver(mReceiver, filter); // TODO check if already registered, maybe unregister it at another position in code
	}
}
