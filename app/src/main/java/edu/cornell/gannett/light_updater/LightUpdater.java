package edu.cornell.gannett.light_updater;

import edu.cornell.gannett.tools.HSB;

public interface LightUpdater {
	public HSB next(HSB hsb, int score);
}
