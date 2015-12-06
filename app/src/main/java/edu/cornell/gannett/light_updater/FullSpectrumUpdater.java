package edu.cornell.gannett.light_updater;

import edu.cornell.gannett.tools.HSB;

public class FullSpectrumUpdater implements LightUpdater {
	
	public FullSpectrumUpdater () {
		// do nothing
	}
	
	public HSB next(HSB hsb, int score) {
		return new HSB(score, 250, 230);
	}
}
