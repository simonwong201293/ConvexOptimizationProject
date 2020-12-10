package com.hkust.project.convex.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;
import com.hkust.project.convex.Main;
import com.hkust.project.convex.backup.Results;

public class Utility {
	
	public static long generateRandomTime(Random rng, long max) {
		long bits, val;
		do {
			bits = (rng.nextLong() << 1) >>> 1;
			val = bits % max;
		} while (bits - val + (max - 1) < 0L);
		return val;
	}
	
	public static double convert(int option) {
		switch (option) {
		case 0:
			return 0.5;
		case 1:
			return 1.0;
		case 2:
			return 2.0;
		case 3:
			return 4.0;
		default:
			return 1.0;
		}
	}
	
	public static void exportResults(List<Results> results){
		List<String> lines = Arrays.asList(new Gson().toJson(results));
		Path file = Paths.get(Main.GENERATION_PAYH + File.separator + "result.json");
		try {
			Files.write(file, lines, StandardCharsets.UTF_8);
			System.out.println("Exported to " + Main.GENERATION_PAYH + File.separator + "result.json");
		} catch (IOException ignored) {
			System.out.println("Export failure : " + ignored.toString());
		}
	}
}
