package edu.cornell.gannett.ui;

import android.app.Fragment;
import android.graphics.Typeface;
import android.widget.Button;

public abstract class MoodlightViewFrag extends Fragment {
	public Button discoverButton;
	protected UIManager uiManager;
	
	public abstract void setPipStatusText(final int pipID, final String newText);

	public void setPipDiscoverText(final String text) {

		discoverButton.setText(text);
	}

	public void setTrialText(final String text) {
		discoverButton.setClickable(false);
		discoverButton.setText(text);

	}

}
