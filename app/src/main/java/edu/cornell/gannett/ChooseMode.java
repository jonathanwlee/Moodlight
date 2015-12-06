/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cornell.gannett;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.galvanic.pipsdk.PIP.Pip;
import com.galvanic.pipsdk.PIP.PipConnectionListener;
import com.galvanic.pipsdk.PIP.PipInfo;
import com.galvanic.pipsdk.PIP.PipManager;
import com.galvanic.pipsdk.PIP.PipManagerListener;
import com.philips.lighting.data.AccessPointListAdapter;
import com.philips.lighting.data.MoodlightSharedPreferences;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHHueParsingError;

import edu.cornell.gannett.one_player.ReflectiveLightingActivity;
import edu.cornell.gannett.tools.CustomViewPager;
import edu.cornell.gannett.ui.PHPushlinkActivity;
import edu.cornell.gannett.ui.PHWizardAlertDialog;
import edu.cornell.gannettML.R;

/**
 * @author Jonathan
 *
 */
public class ChooseMode extends FragmentActivity implements OnItemClickListener, PipManagerListener {
	private static PHHueSDK phHueSDK;
	private MoodlightSharedPreferences prefs;
	public static final String TAG = "Main Menu";
	private boolean lastSearchWasIPScan = false;
	private boolean bridgeConnected = false; 
	private boolean alertShow = false; 
	private boolean wifiErrorShow = false; 
	private AlertDialog showBridgeListAlert;
	public static PipManager pipManager;
	public static HashMap<Integer, PipDataItem> pipDataItemList = new HashMap<Integer, PipDataItem>(); 
	private int numConnected; 

	public static float modeTitleY =0;
	public static float modeTextY =0 ;
	public static float modeButtonY =0;
	
	
	/**
	 * The number of pages (wizard steps) to show in this demo.
	 */
	private static final int NUM_PAGES = 4;

	/**
	 * The pager widget, which handles animation and allows swiping horizontally to access previous
	 * and next wizard steps.
	 */
	private CustomViewPager mPager;


	/**
	 * The pager adapter, which provides the pages to the view pager widget.
	 */
	private PagerAdapter mPagerAdapter;
	private AccessPointListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//requestWindowFeature(Window.FEATURE_ACTION_BAR);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_screen_slide);
		prefs = MoodlightSharedPreferences.getInstance(getApplicationContext());

		/* TODO SHOW TIPS DIALOG */
		boolean showTip = false;
		if (showTip) { 
			final AlertDialog alert=  PHWizardAlertDialog.showTipsDialog(ChooseMode.this);
			alert.show();
		}

		// Instantiate a ViewPager and a PagerAdapter.
		mPager = (CustomViewPager) findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mPager.setPagingEnabled(true);

		//Set Animations between Pages
		//mPager.setPageTransformer(true, new ZoomOutPageTransformer());

		//Bridge Init
		phHueSDK = PHHueSDK.create();
		phHueSDK.getNotificationManager().registerSDKListener(listener);

		// Set the Device Name (name of your app). This will be stored in your bridge whitelist entry.
		phHueSDK.setAppName("Moodlight");
		phHueSDK.setDeviceName(android.os.Build.MODEL);
		adapter = new AccessPointListAdapter(getApplicationContext(), phHueSDK.getAccessPointsFound());

		// Obtain reference to PipManager singleton.
		pipManager = PipManager.getInstance();
		// Initialize the PipManager - the first argument is an the main
		// activity for the application, the second is the listener 
		// which will receive events raised by PipManager.
		pipManager.initialize(this, this);


	}
	protected void onResume() { 
		super.onResume();

		//Init Bridge and Bluetooth if Wifi enabled. 
		if (checkWifi()) { 
			initBridge();
			setBluetooth(true);
		}
		
		prefs.setBiDirectional(false);
		prefs.setFullSpectrum(false);

		//Redraw
		mPagerAdapter.notifyDataSetChanged();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		//Here you can get the size!

	}

	protected void onStop() { 
		super.onStop();
		//LogCat still errors out even with this. 
		if (pipManager != null) {
			pipManager.suspend();
		}
	}


	public void showBridgeList() { 
		showBridgeListAlert =  PHWizardAlertDialog.showBridgeList(ChooseMode.this);
		showBridgeListAlert.show();      
		ListView accessPointList = (ListView) ((AlertDialog) showBridgeListAlert).findViewById(R.id.bridge_list);
		accessPointList.setOnItemClickListener(this);        
		accessPointList.setAdapter(adapter);
	}

	//Checks for Wifi Connection. Returns true if Wifi is active else false. 
	public boolean checkWifi() { 
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (!mWifi.isConnected()) {
			//Open Dialog to Turn on Wifi 
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.wifi_error).setMessage(R.string.no_wifi)
			.setPositiveButton(R.string.btn_enable_wifi, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					wifiErrorShow = false;
					startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));

				}
			}).setNegativeButton("Refresh", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					wifiErrorShow = false; 

					//Call onResume. Resume activity to check Wifi.
					onResume();

				}
			});
			AlertDialog alert = builder.create();
			alert.setCancelable(false);
			if (!wifiErrorShow) { 
				wifiErrorShow = true;
				alert.show();
			}

			return false;
		}
		else { 
			return true;
		}
	}

	public boolean setBluetooth(boolean enable) {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		boolean isEnabled = bluetoothAdapter.isEnabled();
		if (enable && !isEnabled) {
			return bluetoothAdapter.enable(); 
		}
		else if(!enable && isEnabled) {
			return bluetoothAdapter.disable();
		}
		// No need to change bluetooth state
		return true;
	}   

	public void clickTroubleshoot(View view) { 
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		AlertDialog alert = null;
		builder.setTitle("Troubleshoot Application").setMessage(R.string.troubleshoot_message)
		.setPositiveButton("Ok", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				prefs.setReset(true);
				onResume();

			}
		}).setNegativeButton("Cancel", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		alert = builder.create();
		alert.show();
	}

	public boolean getBridgeConnected() { 
		return bridgeConnected;
	}

	public static PHHueSDK getHueSDK() { 
		return phHueSDK;
	}

	public static void setHueSDK(PHHueSDK sdk) { 
		phHueSDK = sdk;
	}

	public void initBridge() { 
		String lastIpAddress   = prefs.getLastConnectedIPAddress();
		String lastUsername    = prefs.getUsername();
		boolean doReset 	   = prefs.getReset();

		// Automatically try to connect to the last connected IP Address.  For multiple bridge support a different implementation is required.
		if (lastIpAddress !=null && !lastIpAddress.equals("") && !doReset) {
			PHAccessPoint lastAccessPoint = new PHAccessPoint();
			lastAccessPoint.setIpAddress(lastIpAddress);
			lastAccessPoint.setUsername(lastUsername);

			if (!phHueSDK.isAccessPointConnected(lastAccessPoint)) {
				PHWizardAlertDialog.getInstance().showProgressDialog(R.string.connecting, ChooseMode.this);
				phHueSDK.connect(lastAccessPoint);
			}
		}
		else {  
			showBridgeList();
			doBridgeSearch();
			prefs.setReset(false);
		}
	}

	public void doBridgeSearch() {
		PHWizardAlertDialog.getInstance().showProgressDialog(R.string.search_progress, ChooseMode.this);
		PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
		// Start the UPNP Searching of local bridges.
		sm.search(true, true);
	}

	// Local SDK Listener
	private PHSDKListener listener = new PHSDKListener() {

		@Override
		public void onAccessPointsFound(List<PHAccessPoint> accessPoint) {
			Log.w(TAG, "Access Points Found. " + accessPoint.size());

			PHWizardAlertDialog.getInstance().closeProgressDialog();
			if (accessPoint != null && accessPoint.size() > 0) {
				phHueSDK.getAccessPointsFound().clear();
				phHueSDK.getAccessPointsFound().addAll(accessPoint);

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						adapter.updateData(phHueSDK.getAccessPointsFound());
					}
				});

			} 

		}

		@Override
		public void onCacheUpdated(List<Integer> arg0, PHBridge bridge) {
			Log.w(TAG, "On CacheUpdated");

		}

		@Override
		public void onBridgeConnected(PHBridge b) {
			//On Bridge Connected Init
			phHueSDK.setSelectedBridge(b);
			phHueSDK.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);
			phHueSDK.getLastHeartbeat().put(b.getResourceCache().getBridgeConfiguration() .getIpAddress(), System.currentTimeMillis());
			prefs.setLastConnectedIPAddress(b.getResourceCache().getBridgeConfiguration().getIpAddress());
			prefs.setUsername(prefs.getUsername());
			PHWizardAlertDialog.getInstance().closeProgressDialog();    
			mPager.setPagingEnabled(true);
			bridgeConnected=true;

			prefs = MoodlightSharedPreferences.getInstance(getApplicationContext());
			Log.w("Moodlight","On Bridge Connected...");
			if (PHPushlinkActivity.isVisible) { 
				startMainActivity();
				PHPushlinkActivity.isVisible = false; 
			}
			//Redraw mPage
			//mPagerAdapter.notifyDataSetChanged();
		}

		@Override
		public void onAuthenticationRequired(PHAccessPoint accessPoint) {
			Log.w(TAG, "Authentication Required.");
			phHueSDK.startPushlinkAuthentication(accessPoint);
			startActivity(new Intent(ChooseMode.this, PHPushlinkActivity.class));
		}

		@Override
		public void onConnectionResumed(PHBridge bridge) {
			if (ChooseMode.this.isFinishing())
				return;

			Log.v(TAG, "onConnectionResumed" + bridge.getResourceCache().getBridgeConfiguration().getIpAddress());
			phHueSDK.getLastHeartbeat().put(bridge.getResourceCache().getBridgeConfiguration().getIpAddress(),  System.currentTimeMillis());
			for (int i = 0; i < phHueSDK.getDisconnectedAccessPoint().size(); i++) {

				if (phHueSDK.getDisconnectedAccessPoint().get(i).getIpAddress().equals(bridge.getResourceCache().getBridgeConfiguration().getIpAddress())) {
					phHueSDK.getDisconnectedAccessPoint().remove(i);
				}
			}

		}

		@Override
		public void onConnectionLost(PHAccessPoint accessPoint) {
			Log.v(TAG, "onConnectionLost : " + accessPoint.getIpAddress());
			if (!phHueSDK.getDisconnectedAccessPoint().contains(accessPoint)) {
				phHueSDK.getDisconnectedAccessPoint().add(accessPoint);
			}
		}

		@Override
		public void onError(int code, final String message) {
			Log.e(TAG, "on Error Called : " + code + ":" + message);

			if (code == PHHueError.NO_CONNECTION) {
				Log.w(TAG, "On No Connection");
			} 
			else if (code == PHHueError.AUTHENTICATION_FAILED || code==1158) {  
				PHWizardAlertDialog.getInstance().closeProgressDialog();
			} 
			else if (code == PHHueError.BRIDGE_NOT_RESPONDING) {
				Log.w(TAG, "Bridge Not Responding . . . ");
				PHWizardAlertDialog.getInstance().closeProgressDialog();
				ChooseMode.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						final AlertDialog alert=  PHWizardAlertDialog.showErrorDialog(ChooseMode.this, getString(R.string.bridge_not_responding), R.string.btn_reconnect);
						alert.setTitle("Error: Bridge not Responding");
						bridgeConnected = false; 

						if (!alertShow) {
							alert.show();
							alertShow= true;
						}
						alert.setOnDismissListener(new OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								initBridge();

								alertShow =false;
							}


						});

					}
				}); 
			} 
			else if (code == PHMessageType.BRIDGE_NOT_FOUND) {

				if (!lastSearchWasIPScan) {  // Perform an IP Scan (backup mechanism) if UPNP and Portal Search fails.
					phHueSDK = PHHueSDK.getInstance();
					PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
					sm.search(false, false, true);               
					lastSearchWasIPScan=true;
				}
				else {
					PHWizardAlertDialog.getInstance().closeProgressDialog();
					ChooseMode.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							bridgeConnected = false; 
							//Show Alert
							final AlertDialog alert=  PHWizardAlertDialog.showErrorDialog(ChooseMode.this, message, R.string.bridge_not_found);
							alert.setTitle("Error: Bridge not Found");
							prefs.setReset(true);
							if (!alertShow) {
								alert.show();
								alertShow= true;
							}

							alert.setOnDismissListener(new OnDismissListener() {

								@Override
								public void onDismiss(DialogInterface dialog) {
									initBridge();
									alertShow =false;
								}

							});

						}
					});  
				}


			}
		}

		@Override
		public void onParsingErrors(List<PHHueParsingError> parsingErrorsList) {
			for (PHHueParsingError parsingError: parsingErrorsList) {
				Log.e(TAG, "ParsingError : " + parsingError.getMessage());
			}      
		}
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		MoodlightSharedPreferences prefs = MoodlightSharedPreferences.getInstance(getApplicationContext());
		PHAccessPoint accessPoint = (PHAccessPoint) adapter.getItem(position);
		accessPoint.setUsername(prefs.getUsername());

		PHBridge connectedBridge = phHueSDK.getSelectedBridge();       

		if (connectedBridge != null) {
			String connectedIP = connectedBridge.getResourceCache().getBridgeConfiguration().getIpAddress();
			if (connectedIP != null) {   // We are already connected here:-
				phHueSDK.disableHeartbeat(connectedBridge);
				phHueSDK.disconnect(connectedBridge);
			}
		}
		PHWizardAlertDialog.getInstance().showProgressDialog(R.string.connecting, ChooseMode.this);
		phHueSDK.connect(accessPoint);  

		//Dismiss Alert Dialog
		showBridgeListAlert.dismiss();

	}

	public void startMainActivity() {   
		Intent intent = new Intent(getApplicationContext(), ChooseMode.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			intent.addFlags(0x8000); // equal to Intent.FLAG_ACTIVITY_CLEAR_TASK which is only available from API level 11
		startActivity(intent);

	}

	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return ScreenSlidePageFragment.create(position);
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}

		@Override
		public int getItemPosition(Object object) { 
			return POSITION_NONE;
		}

	}

	public void discoverPips(String mode, Button startButton) { 
		pipDataItemList.clear();
		pipManager.resetManager();
		pipManager.discoverPips();
		startButton.setClickable(false);
		startButton.setText("discovering...");
	}

	@Override
	public void onPipDiscovered() {
		// This method will be invoked each time a new PIP is discovered by
		// PipManager during a discovery process. Details of the most
		// recently discovered PIP will be stored at the highest index in 
		// PipManager's list.
		int numDiscovered = pipManager.getNumPipsDiscovered();
		PipInfo info = pipManager.getDiscoveryAtIndex(numDiscovered - 1);
		PipDataItem tempItem = new PipDataItem(info,true,true);
		// Add an entry for this PIP in the UI's list of discovered PIPs.

		pipDataItemList.put(info.pipID,tempItem);
		Pip pip = pipManager.getPip(info.pipID);

	}

	@Override
	public void onPipDiscoveryComplete(int pipID) {
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;

		numConnected = pipDataItemList.size();

		if (numConnected ==1) { 
			CharSequence text = "1 Pip Discovered. Connecting...";
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}

		else if (numConnected > 1){ 
			CharSequence text = numConnected + " Pips Discovered! Connecting...";
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}

		else  { 
			CharSequence text = "No Pips Discovered. Try rediscovering...";
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			mPagerAdapter.notifyDataSetChanged();
		}

		if (numConnected ==0) {

		}

		else { 
			Intent i = new Intent(this,ReflectiveLightingActivity.class);
			startActivity(i);
		}
	}
	@Override
	public void onPipManagerReady() {
		// TODO Auto-generated method stub

	}
	@Override
	public void onPipsResumed(int arg0) {
		// TODO Auto-generated method stub

	}
}


