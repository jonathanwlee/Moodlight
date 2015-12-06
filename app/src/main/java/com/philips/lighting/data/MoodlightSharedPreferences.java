package com.philips.lighting.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.philips.lighting.hue.sdk.connection.impl.PHBridgeInternal;

public class MoodlightSharedPreferences {
	private static final String HUE_SHARED_PREFERENCES_STORE = "HueSharedPrefs";
	private static final String LAST_CONNECTED_USERNAME      = "LastConnectedUsername";
	private static final String LAST_CONNECTED_IP            = "LastConnectedIP";
	private static final String RESET_BRIDGE_SEARCH 		 = "ResetBridgeSearch"; 
	private static final String SHOW_HOME_TIPS 		         = "ShowHomeTips"; 
	private static final String FULL_SPECTRUM 		         = "UseFullSpectrum"; 
	private static final String BI_DIRECTIONAL 		         = "UseBiDirectional"; 
	private static final String RELAX_HUE 					 = "RelaxHue";
	private static final String STRESS_HUE 					 = "StressHue";
	private static final String FLICKER 					 = "Flicker";
	private static final String SOUND_ENABLED 				 = "SoundEnabled";

	public static final long UPDATE_PERIOD = 300;
	private static MoodlightSharedPreferences instance = null;
	private SharedPreferences mSharedPreferences = null;

	private Editor mSharedPreferencesEditor = null;


	public void create() {

	}

	public static MoodlightSharedPreferences getInstance(Context ctx) {
		if (instance == null) {
			instance = new MoodlightSharedPreferences(ctx);
		}
		return instance;
	}

	private MoodlightSharedPreferences(Context appContext) {
		mSharedPreferences = appContext.getSharedPreferences(HUE_SHARED_PREFERENCES_STORE, 0); // 0 - for private mode
		mSharedPreferencesEditor = mSharedPreferences.edit();
	}


	public String getUsername() {
		String username = mSharedPreferences.getString(LAST_CONNECTED_USERNAME, "");
		if (username==null || username.equals("")) {
			username = PHBridgeInternal.generateUniqueKey();
			setUsername(username);  // Persist the username in the shared prefs
		}
		return username;
	}

	public boolean setUsername(String username) {
		mSharedPreferencesEditor.putString(LAST_CONNECTED_USERNAME, username);
		return (mSharedPreferencesEditor.commit());
	}

	public String getLastConnectedIPAddress() {
		return mSharedPreferences.getString(LAST_CONNECTED_IP, "");
	}

	public boolean setLastConnectedIPAddress(String ipAddress) {
		mSharedPreferencesEditor.putString(LAST_CONNECTED_IP, ipAddress);
		return (mSharedPreferencesEditor.commit());
	}

	public boolean setReset(boolean reset) { 
		mSharedPreferencesEditor.putBoolean(RESET_BRIDGE_SEARCH, reset);
		return (mSharedPreferencesEditor.commit());
	}

	public boolean getReset() {
		boolean reset = mSharedPreferences.getBoolean(RESET_BRIDGE_SEARCH, false);
		return reset; 
	}

	public boolean setHomeTips(boolean tips) { 
		mSharedPreferencesEditor.putBoolean(SHOW_HOME_TIPS, tips);
		return (mSharedPreferencesEditor.commit());
	}

	public boolean getHomeTips() { 
		boolean showHomeTips = mSharedPreferences.getBoolean(SHOW_HOME_TIPS, false);
		return showHomeTips; 
	}

	public boolean setFullSpectrum(boolean spectrum) { 
		mSharedPreferencesEditor.putBoolean(FULL_SPECTRUM, spectrum);
		return (mSharedPreferencesEditor.commit());
	}

	public boolean getFullSpectrum() { 
		boolean fullSpectrum = mSharedPreferences.getBoolean(FULL_SPECTRUM, false);
		return fullSpectrum; 
	}

	public boolean setBiDirectional(boolean bidirectional) { 
		mSharedPreferencesEditor.putBoolean(BI_DIRECTIONAL, bidirectional);
		return (mSharedPreferencesEditor.commit());
	}

	public boolean getBiDirectional() { 
		boolean biDirectional = mSharedPreferences.getBoolean(BI_DIRECTIONAL, false);
		return biDirectional; 
	}

	public boolean getFlicker() { 
		boolean flicker = mSharedPreferences.getBoolean(FLICKER, false);
		return flicker; 

	}

	public boolean setFlicker(boolean flicker) { 
		mSharedPreferencesEditor.putBoolean(FLICKER, flicker);
		return (mSharedPreferencesEditor.commit());

	}

	public boolean getSoundEnabled() { 
		boolean sound_enabled = mSharedPreferences.getBoolean(SOUND_ENABLED, true);
		return sound_enabled; 
	}

	public boolean setSoundEnabled(boolean sound) { 
		mSharedPreferencesEditor.putBoolean(SOUND_ENABLED, sound);
		return (mSharedPreferencesEditor.commit());
	}

	public int getRelaxHue() { 
		int relax_hue = mSharedPreferences.getInt(RELAX_HUE, 46920);
		return relax_hue; 

	}

	public boolean setRelaxHue(int hue) { 
		mSharedPreferencesEditor.putInt(RELAX_HUE, hue);
		return (mSharedPreferencesEditor.commit());
	}

	public int getStressedHue() {
		int stress_hue = mSharedPreferences.getInt(STRESS_HUE, 500);
		return stress_hue; 

	}

	public boolean setStressedHue(int hue) { 
		mSharedPreferencesEditor.putInt(STRESS_HUE, hue);
		return (mSharedPreferencesEditor.commit());
	}
}
