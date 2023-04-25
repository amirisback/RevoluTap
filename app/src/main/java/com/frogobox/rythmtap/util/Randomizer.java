package com.frogobox.rythmtap.util;

import com.frogobox.rythmtap.R;

import java.util.Random;

public class Randomizer {
	
	public static final int OFF = 0;
	public static final int STATIC = 1;
	public static final int DYNAMIC = 2;
	
	private final Random rand;

	// SM
	private boolean[] lastPitches;
	
	/**
	 * One Randomizer per stepfile, initialize with stepfile's md5hash
	 */
	public Randomizer(int seed) {
		int randomize = Integer.parseInt(
				Tools.getSetting(R.string.randomize, R.string.randomizeDefault));
		if (randomize == DYNAMIC) {
			rand = new Random();
		} else {
			rand = new Random(seed);
		}
		// Initialize just to be safe
		lastPitches = new boolean[Tools.PITCHES];
	}
	
	/**
	 * Call after every line, only affects nextPitch() when not in osu! Mod
	 */
	public void setupNextLine() {
		lastPitches = new boolean[Tools.PITCHES];
	}

	/**
	 * Returns pitch [0,3] for SM
	 */
	public int nextPitch(boolean jumps) {
		int randPitch = rand.nextInt(Tools.PITCHES);
		if (jumps) {
			// Force refresh to avoid dead-end situation
			boolean available = false;
			for (int i = 0; i < Tools.PITCHES; i++) {
				if (!lastPitches[i]) {
					available = true;
					break;
				}
			}
			if (!available) {
				lastPitches = new boolean[Tools.PITCHES];
			}
			while (lastPitches[randPitch]) {
				randPitch = rand.nextInt(Tools.PITCHES);
			}
			lastPitches[randPitch] = true;
		}
		return randPitch;
	}
}
