package com.example.testapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.Debug;
import android.os.IBinder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ipspapplication.LbrService;

import com.mbed.coap.exception.CoapCodeException;
import com.mbed.coap.packet.Code;
import com.mbed.coap.server.CoapExchange;
import com.mbed.coap.server.CoapServer;
import com.mbed.coap.utils.CoapResource;

import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnClickListener {
	private BluetoothAdapter mBluetoothAdapter = null;
	private BroadcastReceiver mReceiver = null;
	private ArrayList<String> mDeviceNameList;
	private ArrayList<BluetoothDevice> mDeviceList;
	private ArrayAdapter<String> mArrayAdapter = null;
	private ListView mDeviceListView;

	private Button btnConnect;
	private Button btnRefresh;
	private Button btnRpl;
	private BluetoothDevice mSelectedDevice;
	private BluetoothLeScanner mBLEScanner;

	private LbrService.LbrBinder mLbrBinder;
	private Boolean mLbrBound;

	private CoapServer mServer;

	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			mLbrBinder = (LbrService.LbrBinder) iBinder;
			mLbrBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mLbrBound = false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		try {
			this.mDeviceList = new ArrayList<>();
			this.mDeviceNameList = new ArrayList<>();
			this.mDeviceListView = (ListView) findViewById(R.id.listView1);
			this.mDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					mSelectedDevice = mDeviceList.get(position);
				}
			});

			this.mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, this.mDeviceNameList);
			this.mDeviceListView.setAdapter(this.mArrayAdapter);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
					this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("This app needs location access");
					builder.setMessage("Please grant location access so this app can detect beacons");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
									Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
						}
					});
					builder.show();
				}
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("This app needs BLE scan access");
					builder.setMessage("Please grant BLE scan access so this app can detect beacons");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
						}
					});
					builder.show();
				}
			}

			btnConnect = (Button) findViewById(R.id.buttonConnect);
			btnConnect.setOnClickListener(this);
			btnRefresh = (Button) findViewById(R.id.buttonRefresh);
			btnRefresh.setOnClickListener(this);
			btnRpl = (Button) findViewById(R.id.buttonRpl);
			btnRpl.setOnClickListener(this);

			Intent intent = VpnService.prepare(this);
			if (intent != null) {
				startActivityForResult(intent, 0);
			} else {
				onActivityResult(0, Activity.RESULT_OK, null);
			}

			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			mReceiver = new BroadcastReceiver() {
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();

					//Finding devices
					if (BluetoothDevice.ACTION_FOUND.equals(action))
					{
						// Get the BluetoothDevice object from the Intent
						BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

						if(!existsInDeviceList(device)) {
							Log.d("DEVICE", "found " + device.getName() + "  ++  " + device.getAddress());
							// Add the name and address to an array adapter to show in a ListView
							mDeviceList.add(device);
							mDeviceNameList.add(device.getName() + "\n" + device.getAddress());
							mArrayAdapter.notifyDataSetChanged();
						}
					}
				}
			};
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			registerReceiver(mReceiver, filter);

			mBluetoothAdapter.startDiscovery();

			mServer = CoapServer.builder().transport(5683).build();
			mServer.addRequestHandler("/test", new TestCoapResource());
			mServer.start();
		} catch (Exception e) {
			Toast.makeText(this,e.toString(), Toast.LENGTH_LONG).show();
		}
	}

	private Intent getServiceIntent() {
		return new Intent(this, LbrService.class);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			this.startService(getServiceIntent().setAction(LbrService.ACTION_CONNECT));

			// Also bind to the service to get control of it
			this.bindService(getServiceIntent(), connection, Context.BIND_AUTO_CREATE);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		Log.e("###", "onDestroy() called");
		Debug.stopMethodTracing();

		super.onDestroy();
		unregisterReceiver(mReceiver);
	}

	@Override
	public void onClick(View v) {
		if (v == btnConnect) {
			if (mSelectedDevice != null) {
				mLbrBinder.connectBluetoothDevice(mSelectedDevice);
			}
		} else if (v == btnRpl) {
			TextView dialogView = new TextView(this);
			dialogView.setText(mLbrBinder.printRoutingTable());
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Таблица маршрутизации");
			builder.setView(dialogView);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// User clicked OK button
							dialog.dismiss();
						}
					}
			);
			builder.create();
			builder.show();
		} else if (v == btnRefresh) {
			mBluetoothAdapter.startDiscovery();
		}
	}

	private boolean existsInDeviceList(BluetoothDevice btDevice) {
		for (BluetoothDevice dev : mDeviceList) {
			if (dev.getAddress().equals(btDevice.getAddress()))
				return true;
		}
		return false;
	}

	class TestCoapResource extends CoapResource {
		// here we should do something with sensors data
		private String body = "Hello World";

		@Override
		public void get(CoapExchange ex) throws CoapCodeException {
			ex.setResponseBody(body);
			ex.setResponseCode(Code.C205_CONTENT);
			ex.sendResponse();
		}

		@Override
		public void put(CoapExchange ex) throws CoapCodeException {
			body = ex.getRequestBodyString();
			ex.setResponseCode(Code.C204_CHANGED);
			ex.sendResponse();

			Toast.makeText(MainActivity.this,
					"CoAP from" + ex.getRemoteAddress().toString() + ", text: " + body,
					Toast.LENGTH_LONG).show();
		}
	}
}
