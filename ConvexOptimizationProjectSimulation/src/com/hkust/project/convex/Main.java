package com.hkust.project.convex;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.hkust.project.convex.backup.Backup;
import com.hkust.project.convex.input.JobInputter;
import com.hkust.project.convex.input.JobInputter.JobInputterCallback;
import com.hkust.project.convex.job.Job;
import com.hkust.project.convex.scheduler.ProposedJobScheduler;
import com.hkust.project.convex.server.Server;
import com.hkust.project.convex.server.Server.ServerCallback;

public class Main {

	public static final String BASE_PATH = System.getProperty("user.dir");
	public static final String GENERATION_PAYH = BASE_PATH + File.separator + "backup";

	public static final int totalJobs = 1000;
	public static final int totalServers = 1;
	public static final long totalDuration = 1000L;
	public static final int maxWorkload = 10;
	public static long time = 0L;
	public static NavigableMap<Integer, Job> queue = new ConcurrentSkipListMap<>();
	public static NavigableMap<Integer, Job> completed = new ConcurrentSkipListMap<>();

	// Job Scheduler

	public static ProposedJobScheduler scheduler = new ProposedJobScheduler();

	// Job Inputter
	public static JobInputter jobInputter;
	public static JobInputterCallback mJICallback = new JobInputterCallback() {
		@Override
		public void onJobInserted(Job job) {
			queue.put(job.index, job);
			// System.out.println("Inserted a job to Queue (size = " + queue.size()+")");
		}

		@Override
		public void onJobSummary() {

		}

	};

	// Servers
	public static NavigableMap<Integer, Server> servers = new ConcurrentSkipListMap<>();
	public static ServerCallback mSCallback = new ServerCallback() {

		@Override
		public void onServerCompleted(int index, Job job) {
			job.completedTime = Main.time;
			completed.put(job.index, job);
			System.out.println(
					"Job#" + job.index + " Done by Server#" + index + ", Completed Job size = " + completed.size());
		}

		@Override
		public void onJobReceived(int index, Job job, Server server) {
			servers.put(index, server);
			System.out.println(
					"Job#" + job.index + " Received by Server#" + index + ", Remaining Queue size = " + queue.size());
		}
	};

	public static void main(String[] args) {
		System.out.println(GENERATION_PAYH);
		if (!new File(GENERATION_PAYH).exists())
			new File(GENERATION_PAYH).mkdirs();

		// Initialize jobs & job inputer
		NavigableMap<Integer, Job> map = new ConcurrentSkipListMap<>();
		for (int i = 0; i < totalJobs; i++) {
			Job job = Job.initialize(i, totalDuration, maxWorkload);
			map.put(i, job);
		}
//		Backup.instance(new ArrayList<>(map.values())).exportBackups("tmp.txt");
		jobInputter = JobInputter.instance(map, mJICallback);
		System.out.println("Initialized Jobs & Job Inputter");
		// Initialize servers
		for (int i = 0; i < totalServers; i++) {
			Server server = Server.instance(i, mSCallback);
			servers.put(i, server);
		}
		System.out.println("Initialized Servers");
		while (completed.size() < totalJobs) {
			jobInputter.run();
			System.out.println("Jobs in queue(" + queue.size() + "),completed(" + completed.size() + ") @" + time);
			if (queue.size() > 0) {
				NavigableMap<Integer, Job> tmpQueue = new ConcurrentSkipListMap<>(queue);
				Map<Integer, Job> schedule = scheduler.assignJob(new ArrayList<>(queue.values()),
						new ArrayList<>(servers.values()));
				for (int serverIndex : schedule.keySet()) {
					servers.get(serverIndex).assign(schedule.get(serverIndex));
					tmpQueue.remove(schedule.get(serverIndex).index);
				}
				queue = tmpQueue;
			}
			for (Server server : servers.values())
				server.run();
			time++;
		}
		// Calculate average flow time
		long total = 0;

		for (Job j : completed.values()) {
			total += j.completedTime - j.arrivalTime;
		}
		System.out.println("Average Flowtime : " + (total * 1.0 / totalJobs));
	}

}
