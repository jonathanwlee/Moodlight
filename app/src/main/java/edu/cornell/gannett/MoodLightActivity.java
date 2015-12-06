package edu.cornell.gannett;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;

import com.galvanic.pipsdk.PIP.Pip;
import com.galvanic.pipsdk.PIP.PipAnalyzerListener;
import com.galvanic.pipsdk.PIP.PipAnalyzerOutput;
import com.galvanic.pipsdk.PIP.PipConnectionListener;
import com.galvanic.pipsdk.PIP.PipControlListener;
import com.galvanic.pipsdk.PIP.PipInfo;
import com.galvanic.pipsdk.PIP.PipManager;
import com.galvanic.pipsdk.PIP.PipManagerListener;
import com.galvanic.pipsdk.PIP.PipStandardAnalyzer;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import edu.cornell.gannett.tools.GSRLogger;
import edu.cornell.gannett.tools.HSB;
import edu.cornell.gannett.tools.StressScore;
import edu.cornell.gannett.ui.MoodlightViewFrag;
import edu.cornell.gannettML.R;

/**
 * Abstract class that provides basic functionality (connection, setting, etc)
 * Other activities should extend this class
 */
public abstract class MoodLightActivity extends Activity implements PipAnalyzerListener,PipConnectionListener {
	public static MoodlightViewFrag moodlightViewFrag;
	protected Boolean discovering = false;
	protected StressScore myStressScore;
	public static boolean trialOver = false;
	protected SharedPreferences settings;
	protected final String SHARED_PREF_NAME = "MoodLightPref";
	protected Runnable pipDiscoverRunnable;
	protected Runnable trialTextRunnable; 
	protected GSRLogger myGSRLogger;
	protected Integer score;
	protected Context context;
	protected PHHueSDK phHueSDK;
	protected PHBridge bridge;
	protected List<PHLight> allLights;

	protected boolean isPlayback = false;
	protected TextView[] myPIPStatusText;
	protected String myHueStatus;
	protected TextView[] myHueStatusText;
	protected int numPlayers;
	protected TimerTask updateLightTask;
	protected MediaPlayer mediaPlayer = null;
	protected TextView score_num;
	protected TextView title;
	protected TextView discover_pips;
	protected TextView state_details, score_details;
	protected TextView register_client;
	protected SoundPool soundPool;
	protected int soundID1, soundID2;
	protected boolean soundIDLoaded1 = false, soundIDLoaded2 = false;
	protected float soundVolume;
	protected PipManager pipManager = null;
	protected int numConnected;
	protected Handler handler = new Handler();
	public Boolean killUpdateLight = false;

	protected String lastTrend = "";

	protected void onPause() {
		super.onPause();
		this.killUpdateLight = true;
		handler.removeCallbacksAndMessages(updateLight());
		disconnectAllPips();

		finish();
	}

	public abstract void initSpecific();

	/**
	 * Sets up UI elements Should be extended and called m
	 * 
	 * @param numPlayers
	 * 
	 */
	protected void init(int numPlayers, String mode) {
		context = this;
		settings = context.getSharedPreferences(SHARED_PREF_NAME,
				Context.MODE_PRIVATE);
		trialOver = false;
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.layout_moodlight);

		this.numPlayers = numPlayers;
		lastTrend = "";
		initSpecific();
		myGSRLogger = new GSRLogger(mode);

		initSoundPool();
		initSoundPlayer();
		phHueSDK = ChooseMode.getHueSDK();
		bridge = phHueSDK.getSelectedBridge();
		allLights = bridge.getResourceCache().getAllLights();
		pipManager = ChooseMode.pipManager;
		initPipListeners();
		
	}

	protected void start() {
		handler.post(updateLight());
	}

	/*
	 * @mm
	 * 
	 * Sets up Media Manager to play sounds at either end of the stress-relax
	 * scale
	 */
	protected void initSoundPlayer() {
		System.out.println("initSoundPlayer()");
		if (mediaPlayer != null) {
			mediaPlayer.reset();
			mediaPlayer.release();
		}
		mediaPlayer = MediaPlayer.create(this, R.raw.reinsamba);
		// not doing at loading checking yet
		// should implement an onload Listener flag
	}

	/*
	 * sets up the soundPool for either end of the stress-relax scale
	 */
	private void initSoundPool() {
	}

	/*
	 * plays sound file at either end of the Hue Range
	 */
	protected void playSound() {
		// System.out.println("Running playSound");
		// System.out.println("SoundPlaying? " + mediaPlayer.isPlaying());
		// check if sound loaded
		 mediaPlayer.start();

		// check if sound playing
		if (mediaPlayer.isPlaying() == false) {
			mediaPlayer.start();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.killUpdateLight = true;
		handler.removeCallbacksAndMessages(updateLight());

		//Disconnect PIPS
		disconnectAllPips();
		finish();


	}

	public abstract Runnable updateLight();


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	/**
	 * Sends new illumination setting to hue lights Replaced Method
	 * 
	 * @param hue
	 * @param sat
	 * @param bri
	 */
	public void sendToLights(HSB hsb) {
		for (PHLight light : allLights) {
			PHLightState lightState = new PHLightState();
			lightState.setHue(hsb.getHue());
			lightState.setBrightness(hsb.getBri());
			lightState.setSaturation(hsb.getSat());
			bridge.updateLightState(light, lightState);
		}
	}

	public abstract void setPipStatusText(final int pipID, final String newText);

	public void setPipDiscoverText(final String text) {
		pipDiscoverRunnable = new Runnable() {
			public void run() {
				moodlightViewFrag.setPipDiscoverText(text);
			}

		};
		handler.post(pipDiscoverRunnable);
	}

	public void setTrialStatusText(final String text) {
		trialTextRunnable = new Runnable() {
			public void run() {
				moodlightViewFrag.setTrialText(text);
			}

		};
		handler.post(trialTextRunnable);

	}

	public void disconnectAllPips() { 
		for (Map.Entry<Integer, PipDataItem> entry : ChooseMode.pipDataItemList.entrySet()) {
			Pip pip = pipManager.getPip(entry.getKey());
			pip.stopStreaming();
			pip.disconnect();
		}
	}
	
	public void initPipListeners() { 
		for (Map.Entry<Integer, PipDataItem> entry : ChooseMode.pipDataItemList.entrySet()) {
			Pip pip = pipManager.getPip(entry.getKey());
			pip.connect();
			pip.setPipConnectionListener(this);
		}
	}


	@Override
	public void onAnalyzerOutputEvent(int pipID, int status) {
		// A PIP's signal analyzer has updated its output.
		// Check if the PIP reports itself as active (i.e. that the user's fingers
		// are in contact with its sensor discs).

		if (!pipManager.getPip(pipID).isActive()) {
			moodlightViewFrag.setPipStatusText(pipID,"Inactive");
			Log.w("PIP","Inactive");
		} 

		else {
			// If the PIP is active, then examine its analyzer output to determine
			// the current stress trend.
			ArrayList<PipAnalyzerOutput> output = pipManager.getPip(pipID)
					.getAnalyzerOutput();
			int stressTrend = (int) output
					.get(PipStandardAnalyzer.CURRENT_TREND_EVENT.ordinal()).outputValue;

			switch (stressTrend) {
			// If the user is neither stressing nor relaxing, simply
			// display a message to indicate that the PIP is active (i.e.
			// actively processing the user's electrodermal activity).

			case PipAnalyzerListener.STRESS_TREND_CONSTANT:
				Log.w("PIP", ((Integer) pipID).toString() + ": Constant");
				if (lastTrend.isEmpty()) { 
					moodlightViewFrag.setPipStatusText(pipID,"Active");
				}

				else { 
					moodlightViewFrag.setPipStatusText(pipID,lastTrend);
					//myStressScore.updateScore(pipID, false);

					/*Extra boost to relaxation if last trend was relaxing even if constant. 
					if (lastTrend=="Relaxing") { 
						myStressScore.updateScore(pipID, false);
					}		*/
				}
				break;

			case PipAnalyzerListener.STRESS_TREND_RELAXING:
				Log.w("PIP", ((Integer) pipID).toString() + ": Relaxing");
				myStressScore.updateScore(pipID, false);
				lastTrend="Relaxing";
				moodlightViewFrag.setPipStatusText(pipID,"Relaxing");
				break;

			case PipAnalyzerListener.STRESS_TREND_STRESSING:
				Log.w("PIP", ((Integer) pipID).toString() + ": Stressing");
				myStressScore.updateScore(pipID, true);
				lastTrend="Stressing";
				moodlightViewFrag.setPipStatusText(pipID,"Stressing");
				
				break;

			default:
				// The PIP's sensor discs are not being held by the user.
				Log.w("PIP", ((Integer) pipID).toString() + ": Inactive");
				moodlightViewFrag.setPipStatusText(pipID,"Inactive");
				break;
			}
		}
	}
	@Override
	public void onPipConnected(int status, int pipID) {
		pipManager.getPip(pipID).startStreaming();
		pipManager.getPip(pipID).setPipAnalyzerListener(this);
	}

	@Override
	public void onPipConnectionError(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPipDisconnected(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPipPaired(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
}