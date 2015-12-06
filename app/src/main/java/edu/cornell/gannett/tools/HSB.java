package edu.cornell.gannett.tools;

import java.util.HashMap;
import java.util.Map;

public class HSB {
	private int hue;
	private int sat;
	private int bri;
	
	public HSB (int h, int s, int b) {
		hue = h;
		sat = s;
		bri = b;
	}
	
	public int getHue() {
		return hue;
	}
	
	public int getSat() {
		return sat;
	}
	
	public int getBri() {
		return bri;
	}
	
	public Map<String, String> getMapHSB() {
		Map<String, String> hm = new HashMap<String, String>();
		hm.put("hue", Integer.toString(hue));
		hm.put("sat", Integer.toString(sat));
		hm.put("bri", Integer.toString(bri));
		return hm;
	}
	
	public String toString() {
		return Integer.toString(hue) + ", " + 
				Integer.toString(sat) + ", " + 
				Integer.toString(bri);
	}
}
