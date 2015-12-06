package edu.cornell.gannett.light_updater;

import edu.cornell.gannett.tools.HSB;
import edu.cornell.gannett.tools.StressScore;

public class TwoHuesUpdater implements LightUpdater {
	
	int relax;
	int stress;
	
	public TwoHuesUpdater (int rx, int st) {
		relax = rx;
		stress = st;
	}
	
	public HSB next(HSB hsb, int score) {
		int mid = (StressScore.scoreMax - StressScore.scoreMin) / 2;
		int sat_factor = 96;
		int bri_factor = 240;
		int bri_offset = 130;
		
		int h = (score >= mid) ? relax : stress;
		int s = Math.abs(score - mid) / sat_factor;
		int b = Math.abs(score - mid) / bri_factor + bri_offset;
		
		return new HSB(h, s, b);
	}
}
