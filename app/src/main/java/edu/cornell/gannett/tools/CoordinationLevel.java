package edu.cornell.gannett.tools;

public class CoordinationLevel {
	
	int briMin;
	int briMax;
	int briDelta;
	
	public CoordinationLevel(int min, int max, int del) {
		this.briMin = min;
		this.briMax = max;
		this.briDelta = del;	
	}
	
	public CoordinationLevel() {
		this.briMin = 100;
		this.briMax = 250;
		this.briDelta = 5;
	}
	
	public HSB genUpdatedHSB(HSB hsb, int numRelaxing, int numStressing) {	    		
		int bri = hsb.getBri();
		if (numRelaxing == 1 && numStressing == 1) {
			bri = computeBrightness(false, bri);
		} else if ((numRelaxing == 2) || (numStressing == 2)) {
			bri = computeBrightness(true, bri);
		}
		return new HSB(hsb.getHue(), hsb.getSat(), bri);
	}

	private int computeBrightness(boolean synced, int currBri) {
		int newBri = synced ? currBri + briDelta : currBri - briDelta;
		return briMin > newBri ? briMin : briMax < newBri ? briMax : newBri ;
	}
}
