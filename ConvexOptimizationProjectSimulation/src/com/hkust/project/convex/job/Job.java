package com.hkust.project.convex.job;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.hkust.project.convex.util.Utility;

public class Job {
	public int index = -1;
	public long arrivalTime = -1L;
	public long deadline = -1L;
	public long completedTime = -1L;
	public int workload;

	public static Job initialize(int index, long totalDuration, int maxWorkLoad) {
		Job job = new Job();
		Random rand = new Random(System.currentTimeMillis());
		job.index = index;
		job.workload = rand.nextInt(maxWorkLoad-1)+1;
		while (job.arrivalTime == -1L) {
			long tmpArrivalTime = ThreadLocalRandom.current().nextLong(totalDuration - ((long)job.workload));
			if (tmpArrivalTime + job.workload < totalDuration-1) {
				job.arrivalTime = tmpArrivalTime;
			}
		}
		job.deadline = job.arrivalTime + ThreadLocalRandom.current().nextLong(job.workload, totalDuration - job.arrivalTime);
		return job;
	}
}
