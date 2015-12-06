package edu.cornell.gannett.light_updater;

import java.util.Random;
import edu.cornell.gannett.tools.HSB;

public class FlickerUpdater implements LightUpdater {
	int hue_high;
	int hue_low;
	int hue_delta;
	int sat_high;
	int sat_low;
	int sat_delta;
	int bri_high;
	int bri_low;
	int bri_factor;
	
	public FlickerUpdater (
			int hh, int hl, int hd,
			int sh, int sl, int sd,
			int bh, int bl, int bf
			) {
		this.hue_high = hh;
		this.hue_low = hl;
		this.hue_delta = hd;
		this.sat_high = sh;
		this.sat_low = sl;
		this.sat_delta = sd;
		this.bri_high = bh;
		this.bri_low = bl;
		this.bri_factor = bf;
	}
	
	public FlickerUpdater () {
		this.hue_high = 18000;
		this.hue_low = 15000;
		this.hue_delta = 10;
		this.sat_high = 240;
		this.sat_low = 220;
		this.sat_delta = 5;
		this.bri_high = 200;
		this.bri_low = 100;
		this.bri_factor = 1600;
	}
	
	public HSB next(HSB hsb, int score) {
		int h = hsb.getHue();
		int s = hsb.getSat();
		int b = hsb.getBri();
		
		int bri_delta = score / bri_factor;
		
		Random rand = new Random();
		int p = rand.nextInt(10);
		
		if (p < 7) {
			h = randWalk(h, hue_delta, hue_low, hue_high);
			s = randWalk(s, sat_delta, sat_low, sat_high);
			b = randWalk(b, bri_delta, bri_low, bri_high);
		}
		
		return new HSB(h, s, b);
	}
	
	private int randWalk(int curr, int delta, int limitA, int limitB) {
		Random r = new Random();
		int w = r.nextInt(2);
		
		if (w < 1) {
			curr += delta;
		} else if (w == 1){
			curr -= delta;
		}
		
		if (curr < limitA) {
			return limitA;
		} else if (curr > limitB) {
			return limitB;
		}
		
		return curr;
	}
}
