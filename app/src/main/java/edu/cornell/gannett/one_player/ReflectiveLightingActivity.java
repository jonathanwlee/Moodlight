package edu.cornell.gannett.one_player;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.philips.lighting.data.MoodlightSharedPreferences;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import edu.cornell.gannett.ChooseMode;
import edu.cornell.gannett.MoodLightActivity;
import edu.cornell.gannett.light_updater.FlickerUpdater;
import edu.cornell.gannett.light_updater.FullSpectrumUpdater;
import edu.cornell.gannett.light_updater.TwoHuesUpdater;
import edu.cornell.gannett.tools.HSB;
import edu.cornell.gannett.tools.StressScore;
import edu.cornell.gannett.ui.MoodlightViewFrag;
import edu.cornell.gannett.ui.UIManager;
import edu.cornell.gannettML.R;

public class ReflectiveLightingActivity extends MoodLightActivity {

	HSB currHSB;
	int RELAX_HUE;
	int STRESS_HUE;
	boolean useFullSpectrum;
	boolean onlyRelaxing = true; // set to true temporarily
	boolean sound_enabled;
	boolean useFlicker = true; // set to true temporarily
	SharedPreferences myPref;
	FlickerUpdater flicker;
	FullSpectrumUpdater fullSpectrum;
	TwoHuesUpdater twoHues;
	MoodlightSharedPreferences prefs;

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.cornell.moodlight.MoodLightActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			getFragmentManager()
					.beginTransaction()
					.add(R.id.container, new ReflectiveLightingViewFrag(),
							"fragment_reflective").commit();
			getFragmentManager().executePendingTransactions();
		}
		
		myPref = getSharedPreferences("test",MODE_PRIVATE);
		prefs = MoodlightSharedPreferences.getInstance(getApplicationContext());

		moodlightViewFrag = (ReflectiveLightingViewFrag) getFragmentManager()
				.findFragmentByTag("fragment_reflective");
		
		sound_enabled = prefs.getSoundEnabled();
		RELAX_HUE = prefs.getRelaxHue();
		STRESS_HUE = prefs.getStressedHue();
		useFullSpectrum = prefs.getFullSpectrum();
		useFlicker = prefs.getFlicker();
		onlyRelaxing = !prefs.getBiDirectional();
		
		init(1, "1P");
		flicker = new FlickerUpdater();
		twoHues = new TwoHuesUpdater(RELAX_HUE, STRESS_HUE);
		fullSpectrum = new FullSpectrumUpdater();
		
		score = myStressScore.getScore();
		if (useFlicker) {
			currHSB = new HSB(17000, 230, 150);
		} else {
			int hue = useFullSpectrum ? score : myPref.getInt("stressedValue",
					500);
			int sat = useFullSpectrum ? 250 : 0;
			int bri = 230;
			currHSB = new HSB(hue, sat, bri);
		}

		start();

	}

	@Override
	public void initSpecific() {
		myStressScore = new StressScore("1P", onlyRelaxing);
	}

	public void onPause() {
		super.onPause();
		prefs.setBiDirectional(false);
		prefs.setFullSpectrum(false);
		// killUpdateLight = true;
	}

	public void onResume() {
		killUpdateLight = false;
		super.onResume();
		// onlyRelaxing = myPref.getBoolean("game_mode", false);
	}

	public void setPipStatusText(final int pipID, final String newText) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				moodlightViewFrag.setPipStatusText(pipID, newText);
			}
		});
	}
	public void onBackPressed() {
		super.onBackPressed();
		//handler.removeCallbacks(pipDiscoverRunnable);
		//handler.removeCallbacks(trialTextRunnable);
	}

	/**
	 * Update the light according to hue value
	 * 
	 * @param null
	 * @return updateLight (the new hue to transition to)
	 * @see edu.cornell.moodlight.MoodLightActivity#updateLight()
	 */
	@Override
	public Runnable updateLight() {
		Runnable updateLight = new Runnable() {
			public void run() {
				computeNext();

				// GSR logging
//				double GSR = myPIPStatus[0].getGSR();
//				String PIPState = Boolean.toString(myPIPStatus[0]
//						.getStressing());
				
				//myGSRLogger.log1P(GSR, PIPState, score, currHSB);

				new Thread(new Runnable() {
					public void run() {

						for (PHLight light : allLights) {
						    PHLightState lightState = new PHLightState();
						    lightState.setHue(currHSB.getHue());
						    lightState.setBrightness(currHSB.getBri());
						    lightState.setSaturation(currHSB.getSat());
						    bridge.updateLightState(light, lightState);
						}

					}
				}).start();

				if (!killUpdateLight) {
					handler.postDelayed(this, MoodlightSharedPreferences.UPDATE_PERIOD);
				}
			}
		};
		return updateLight;
	}

	/**
	 * Helper method for computing next color step to display on lamps
	 */
	private void computeNext() {
		if (sound_enabled && myStressScore.isEndGame()) {
			playSound();
		}
		
		if (myStressScore.isEndGame()) { 
			((ReflectiveLightingViewFrag) moodlightViewFrag).displayEnd();
			
			//Play ending sound once
			sound_enabled =false;
		}
		score = myStressScore.getScore();
		Log.w("SCORE",((Integer) score).toString());
		if (useFlicker) {
			currHSB = flicker.next(currHSB, score);
		} else if (useFullSpectrum) {
			currHSB = fullSpectrum.next(currHSB, score);
		} else {
			currHSB = twoHues.next(currHSB, score);
		}
	}

	public static class ReflectiveLightingViewFrag extends MoodlightViewFrag {
		protected View rootView;
		protected TextView pipStatus;
		protected TextView trialStatus;
		protected Button returnButton;
		protected RelativeLayout layout;
		protected MoodlightSharedPreferences prefs;

		public ReflectiveLightingViewFrag() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			rootView = inflater.inflate(R.layout.fragment_reflective,
					container, false);
			return rootView;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			initViews();
			initText();

		}

		public void onResume() {
			super.onResume();

		}

		public void onPause() {
			super.onPause();
		}

		public void setPipStatusText(final int pipID, final String newText) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
//					pipStatus.setText(String.format("Pip %d is %s ", pipID,
//							newText));
					pipStatus.setText(String.format("%s",newText));

				}
			});
		}
		

		public void initViews() {
			prefs=MoodlightSharedPreferences.getInstance(getActivity());
			uiManager = UIManager.getInstance(getActivity());
			pipStatus = (TextView) rootView.findViewById(R.id.pipStatus);
			trialStatus = (TextView) rootView.findViewById(R.id.trialStatus);
			returnButton = (Button) rootView.findViewById(R.id.returnButton);
			layout= (RelativeLayout) rootView.findViewById(R.id.reflectiveLayout);
			if (prefs.getBiDirectional()) { 
				layout.setBackgroundResource(R.drawable.blue_gradient);
			}
			
			else if (prefs.getFullSpectrum()) { 
				layout.setBackgroundResource(R.drawable.purple_gradient);
			}
			
			else { 
				layout.setBackgroundResource(R.drawable.red_gradient);
			}
		}
		
		public void displayEnd() {
			pipStatus.setVisibility(View.GONE);
			trialStatus.setVisibility(View.VISIBLE);
			//returnButton.setVisibility(View.VISIBLE);
		}
		
		public void initText() {
			pipStatus.setTypeface(uiManager.tfLight);
			trialStatus.setTypeface(uiManager.tfLight);

		}

	}






}