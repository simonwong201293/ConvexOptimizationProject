package com.hkust.project.convex.input;

import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.hkust.project.convex.Main;
import com.hkust.project.convex.job.Job;

public class JobInputter {

	public static interface JobInputterCallback {
		public void onJobInserted(Job job);

		public void onJobSummary();
	}

	public NavigableMap<Integer, Job> map = new ConcurrentSkipListMap<>();
	private JobInputterCallback mCallback;

	public static JobInputter instance(NavigableMap<Integer, Job> map, JobInputterCallback callback) {
		JobInputter jobinputter = new JobInputter();
		jobinputter.map = map;
		jobinputter.mCallback = callback;
		return jobinputter;
	}

	public void updateJobStatus(int index, boolean done) {
		if (done)
			map.remove(index);
	}

	public void run() {
		NavigableMap<Integer, Job> tmpMap = new ConcurrentSkipListMap<>(map);
		for (Job job : map.values()) {
			if (job.completedTime != -1L)
				continue;
			if(job.arrivalTime == Main.time) {
				if(mCallback != null)
					mCallback.onJobInserted(job);
				tmpMap.remove(job.index);
			}
		}
		map = tmpMap;
	}
}
