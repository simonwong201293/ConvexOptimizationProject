package com.hkust.project.convex.util;

import java.util.Random;

public class Utility {
	
	public static long generateRandomTime(Random rng, long max) {
		long bits, val;
		do {
			bits = (rng.nextLong() << 1) >>> 1;
			val = bits % max;
		} while (bits - val + (max - 1) < 0L);
		return val;
	}
}
