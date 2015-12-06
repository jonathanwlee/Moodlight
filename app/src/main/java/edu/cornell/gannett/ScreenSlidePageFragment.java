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

import com.philips.lighting.data.MoodlightSharedPreferences;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import edu.cornell.gannett.ui.UIManager;
import edu.cornell.gannettML.R;

/**
 * A fragment representing a single step in a wizard. The fragment shows a dummy title indicating
 * the page number, along with some dummy text.
 *
 * <p>This class is used by the {@link CardFlipActivity} and {@link
 * ChooseMode} samples.</p>
 */

public class ScreenSlidePageFragment extends Fragment {
	private TextView intro; 
	private UIManager uiManager;
	private Button mode1;
	private Button mode2;
	private Button mode3; 
	private MoodlightSharedPreferences prefs;

	private TextView modeText;
	private TextView modeTitle;
	/**	
	 * The argument key for the page number this fragment represents.
	 */
	public static final String ARG_PAGE = "page";

	/**
	 * The fragment's page number, which is set to the argument value for {@link #ARG_PAGE}.
	 */
	private int mPageNumber;

	/**
	 * Factory method for this fragment class. Constructs a new fragment for the given page number.
	 */
	public static ScreenSlidePageFragment create(int pageNumber) {
		ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_PAGE, pageNumber);
		fragment.setArguments(args);

		return fragment;
	}

	public ScreenSlidePageFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPageNumber = getArguments().getInt(ARG_PAGE);
		uiManager = UIManager.getInstance(getActivity());
		prefs = MoodlightSharedPreferences.getInstance(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout containing a title and body text.

		if (mPageNumber == 0) { 
			ViewGroup rootView = (ViewGroup) inflater
					.inflate(R.layout.layout_intro, container, false);
			((TextView) rootView.findViewById(R.id.intro)).setTypeface(uiManager.tfLight);
			((TextView) rootView.findViewById(R.id.troubleshoot)).setTypeface(uiManager.tfLight);
			return rootView;
		}

		else if (mPageNumber == 1) { 
			ViewGroup rootView = (ViewGroup) inflater
					.inflate(R.layout.layout_mode_1, container, false);
			initViews(rootView);
			
			
			final Button startSession = ((Button) rootView.findViewById(R.id.startSession));
			resetStartSession(startSession);
			startSession.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					((ChooseMode) getActivity()).discoverPips("1P",startSession);
				}
			});
			
			return rootView;
		}

		else if (mPageNumber==2) { 
			ViewGroup rootView = (ViewGroup) inflater
					.inflate(R.layout.layout_mode_2, container, false);
			initViews(rootView);
			
			final Button startSession = ((Button) rootView.findViewById(R.id.startSession));
			resetStartSession(startSession);
			startSession.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					((ChooseMode) getActivity()).discoverPips("1P",startSession);
					prefs.setBiDirectional(true);
				}
			});

			return rootView;
		} 

		else if (mPageNumber ==3) { 
			ViewGroup rootView = (ViewGroup) inflater
					.inflate(R.layout.layout_mode_3, container, false);
			initViews(rootView);
			
			final Button startSession = ((Button) rootView.findViewById(R.id.startSession));
			resetStartSession(startSession);
			startSession.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					((ChooseMode) getActivity()).discoverPips("1P",startSession);
					prefs.setFullSpectrum(true);
				}
			});

			return rootView;
		}

		else { 
			ViewGroup rootView = (ViewGroup) inflater
					.inflate(R.layout.layout_mode_1, container, false);
			return rootView;
		}


	}
	
	public void initViews(ViewGroup rootView) { 
		((TextView) rootView.findViewById(R.id.modeText)).setTypeface(uiManager.tfLight);
		((TextView) rootView.findViewById(R.id.modeTitle)).setTypeface(uiManager.tfBold);
		((Button) rootView.findViewById(R.id.startSession)).setTypeface(uiManager.tfBold);

	}
	
	/**
	 * Returns the page number represented by this fragment object.
	 */
	public int getPageNumber() {
		return mPageNumber;
	}

	public void resetStartSession(Button button) {
		button.setText("start session");
		button.setClickable(true);
	}

}
