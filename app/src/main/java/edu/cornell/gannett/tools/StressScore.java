package edu.cornell.gannett.tools;

public class StressScore {
	public static final int scoreMin = 0;
	public static final int scoreMax = 48000;
	static final int scoreDiff1P = 150;
	static final int scoreDiff2PCoop = 75;
	static final int scoreDiff2PToW = 150;
	
	private String mode;
	private boolean onlyRelaxing;
	private int score;
	private int scoreDiff;
	private int numPlayers;
	//private PIPStatus[] pipStatuses;
	
	private int numRelaxing = 0;
	private int numStressing = 0;
	private boolean shouldUpdate = false;
	
	public StressScore(String gameMode, boolean unidirectional) {
		//pipStatuses = pips;
		onlyRelaxing = unidirectional;
		score = onlyRelaxing ? scoreMin : (scoreMax-scoreMin)/2;
		mode = gameMode;
		if (mode.equals("1P")) {
			scoreDiff = scoreDiff1P;
			numPlayers = 1;
		} else if (mode.equals("2P_coop")) {
			scoreDiff = scoreDiff2PCoop;
			numPlayers = 2;
		} else if (mode.equals("2P_ToW")) {
			scoreDiff = scoreDiff2PToW;
			numPlayers = 2; // shouldn't matter
			onlyRelaxing = false;
		}
	}
	
	public synchronized int getScore() {
		return score;
	}
	
	public synchronized void updateScore(int pipID, boolean isStressing) {
		if (mode.equals("2P_ToW")) {
			int prevScore = score;
			if (pipID == 0) {
				if (isStressing == false && scoreInRange(score)) {
					score -= scoreDiff;
				}
			}
			if (pipID == 1) {
				if (isStressing == false && scoreInRange(score)) {
					score += scoreDiff;
				}
			}
			if (score != prevScore) {
				shouldUpdate = true;
			}
		} else {
			//for (int i = 0; i < numPlayers; i++) {
				//if (pipStatuses[i].getActive()) {
					if (isStressing == false && scoreInRange(score)) {
						score += scoreDiff;
						shouldUpdate = true;
						numRelaxing = numRelaxing + 1;
					} else if (isStressing == true && scoreInRange(score)) {
						if (!onlyRelaxing) {
							score -= scoreDiff;
						}
						shouldUpdate = true;
						numStressing = numStressing + 1;
					}
				//}
			//}
		}
		// Probably unnecessary, but being safe for now
		if (score > scoreMax) {
			score = scoreMax;
		} else if (score < scoreMin) {
			score = scoreMin;
		}
	}
	
	public synchronized int getNumRelaxing() {
		return numRelaxing;
	}
	
	public synchronized  int getNumStressing() {
		return numStressing;
	}
	
	public synchronized boolean isEndGame() {
		boolean endGameA = (onlyRelaxing && (score == scoreMax));
		boolean endGameB = (!onlyRelaxing && ((score == scoreMin) || 
				(score == scoreMax)));
		return endGameA || endGameB;
	}

	private synchronized boolean scoreInRange(int currentScore) {
		if (onlyRelaxing) {
			return currentScore < scoreMax;
		}
		return (currentScore < scoreMax) && (currentScore > scoreMin);
	}
}