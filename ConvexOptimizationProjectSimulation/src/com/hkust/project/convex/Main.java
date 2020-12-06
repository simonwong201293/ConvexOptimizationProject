package com.hkust.project.convex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.hkust.project.convex.backup.Backup;
import com.hkust.project.convex.backup.Results;
import com.hkust.project.convex.input.JobInputter;
import com.hkust.project.convex.input.JobInputter.JobInputterCallback;
import com.hkust.project.convex.job.Job;
import com.hkust.project.convex.scheduler.EDFJobScheduler;
import com.hkust.project.convex.scheduler.FIFOJobScheduler;
import com.hkust.project.convex.scheduler.JobScheduler;
import com.hkust.project.convex.scheduler.ProposedJobScheduler;
import com.hkust.project.convex.scheduler.STRFJobScheduler;
import com.hkust.project.convex.scheduler.SVFJobScheduler;
import com.hkust.project.convex.server.Server;
import com.hkust.project.convex.server.Server.ServerCallback;
import com.hkust.project.convex.util.Utility;

public class Main {

	public static final String BASE_PATH = System.getProperty("user.dir");
	public static final String GENERATION_PAYH = BASE_PATH + File.separator + "backup";

	public static final int totalJobs = 100;
	public static final int[] totalServerOptions = { 1, 10, 100 };
	public static final int totalTrials = 10;
	public static int totalServers = 1;
	public static final long totalDuration = 100;
	public static final int maxWorkload = 10;
	public static long time = 0L;
	public static NavigableMap<Integer, Job> queue = new ConcurrentSkipListMap<>();
	public static NavigableMap<Integer, Job> completed = new ConcurrentSkipListMap<>();

	// Result
	public static double averageFlowtime[][][] = new double[5][totalTrials][totalServerOptions.length];
	public static double relability[][][] = new double[5][totalTrials][totalServerOptions.length];
	public static double variationFlowtime[][][] = new double[5][totalTrials][totalServerOptions.length];
	public static double maxFlowtime[][][] = new double[5][totalTrials][totalServerOptions.length];
	
	// Job Scheduler
	public static FIFOJobScheduler fifoScheduler = new FIFOJobScheduler();
	public static SVFJobScheduler svfScheduler = new SVFJobScheduler();
	public static EDFJobScheduler edfScheduler = new EDFJobScheduler();
	public static STRFJobScheduler strfScheduler = new STRFJobScheduler();
	public static ProposedJobScheduler proposedScheduler = new ProposedJobScheduler();

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
//			System.out.println(
//					"Job#" + job.index + " Done by Server#" + index + ", Completed Job size = " + completed.size());
		}

		@Override
		public void onJobReceived(int index, Job job, Server server) {
			servers.put(index, server);
//			System.out.println(
//					"Job#" + job.index + " Received by Server#" + index + ", Remaining Queue size = " + queue.size());
		}
	};

	public static void main(String[] args) {
		if (!new File(GENERATION_PAYH).exists())
			new File(GENERATION_PAYH).mkdirs();

		for (int k = 0; k < totalServerOptions.length; k++) {
			totalServers = totalServerOptions[k];
			// Initialize servers
			servers.clear();
			for (int i = 0; i < totalServers; i++) {
				Server server = Server.instance(i, mSCallback);
				servers.put(i, server);
			}
			for (int trial = 0; trial < totalTrials; trial++) {
				// Initialize jobs & job inputer
				NavigableMap<Integer, Job> map = new ConcurrentSkipListMap<>();
				Backup backup = Backup.loadBackups("totalserver_"+totalServers+"_trial_"+trial+".txt");
				for(int i = 0 ; i < totalJobs; i++) {
					map.put(backup.jobs.get(i).index, backup.jobs.get(i));
				}
//				for (int i = 0; i < totalJobs; i++) {
//					Job job = Job.initialize(i, totalDuration, maxWorkload);
//					map.put(i, job);
//				}
//				Backup.instance(new ArrayList<>(map.values()))
//						.exportBackups("totalserver_" + totalServers + "_trial_" + trial + ".txt");
				for (int p = 0; p < 5; p++) {
					// Reinitialized all values
					completed.clear();
					queue.clear();
					for(Job j : map.values()) {
						j.completedTime = -1L;
					}
					time = 0;
					jobInputter = JobInputter.instance(map, mJICallback);
					if(p == 3) {
						proposedScheduler.bindSchedule(trial);
					}
					while (completed.size() < totalJobs) {
						jobInputter.run();
//						System.out.println(
//								"Jobs in queue(" + queue.size() + "),completed(" + completed.size() + ") @" + time);
						if (queue.size() > 0) {
							NavigableMap<Integer, Job> tmpQueue = new ConcurrentSkipListMap<>(queue);
							Map<Integer, Job> schedule;
							switch (p) {
							case 0:
								schedule = fifoScheduler.assignJob(new ArrayList<>(queue.values()),
										new ArrayList<>(servers.values()));
								break;
							case 1:
								schedule = svfScheduler.assignJob(new ArrayList<>(queue.values()),
										new ArrayList<>(servers.values()));
								break;
							case 2:
								schedule = edfScheduler.assignJob(new ArrayList<>(queue.values()),
										new ArrayList<>(servers.values()));
								break;
							case 3:
								schedule = strfScheduler.assignJob(new ArrayList<>(queue.values()),
										new ArrayList<>(servers.values()));
								break;
							case 4:
								schedule = proposedScheduler.assignJob(new ArrayList<>(queue.values()),
										new ArrayList<>(servers.values()));
								break;
							default:
								continue;
							}
							
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
					long totalFlowtime = 0;
					long variation = 0;
					long totalCompleted = 0;
					long max = 0;
					for (Job j : completed.values()) {
						totalFlowtime += j.completedTime - j.arrivalTime;
						variation += (j.deadline - j.completedTime) * (j.deadline - j.completedTime);
						totalCompleted += (j.completedTime <= j.deadline) ? 1 : 0;
						if(j.completedTime - j.arrivalTime > max)
							max = j.completedTime - j.arrivalTime;
					}
					averageFlowtime[p][trial][k] = (totalFlowtime * 1.0 / totalJobs);
					variationFlowtime[p][trial][k] = (variation * 1.0 / totalJobs);
					relability[p][trial][k] = (totalCompleted * 1.0 / totalJobs);
					maxFlowtime[p][trial][k] = max;
				}
			}
		}
		List<Results> results = new ArrayList<>();
		System.out.println("Final Result");
		for(int i = 0 ; i < 5; i++) {
			for(int j = 0; j < totalServerOptions.length; j++) {
				double average = 0.0, variation = 0.0, reliability = 0.0, max = 0.0;
				for(int k = 0 ; k < totalTrials; k++) {
					average +=  averageFlowtime[i][k][j];
					variation += variationFlowtime[i][k][j];
					reliability += relability[i][k][j];
					max += maxFlowtime[i][k][j];
				}
				System.out.println(
						"Total Server: " + totalServerOptions[j] 
								+ ",\t  Schedule: " + getScheduleName(i) 
				+ ",\t Average Flowtime: " + average/totalTrials 
				+ ",\t Variation of Flowtime: " + variation/totalTrials 
				+ ",\t Reliability: " + reliability/totalTrials
				+ ",\t Max Flowtime: " + max/totalTrials
				);
				Results result = new Results();
				result.totalServer = String.valueOf(totalServerOptions[j]);
				result.schedule = getScheduleName(i);
				result.averageFlowtime =  average/totalTrials > totalDuration ? String.valueOf(20) : String.valueOf(average/totalTrials);
				result.variationFlowtime =  String.valueOf(variation/totalTrials);
				result.reliability =  String.valueOf(reliability/totalTrials);
				result.maxFlowtime = String.valueOf(max/totalTrials);
				results.add(result);
			}
		}
		Utility.exportResults(results);
	}
	
	public static String getScheduleName(int i) {
		switch (i) {
		case 0:
			return "FIFO";
		case 1:
			return "SVF";
		case 2:
			return "EDF";
		case 3:
			return "STRF";
		case 4:
			return "Proposed";
		default:
			return "Unknown";
		}
	}

}
