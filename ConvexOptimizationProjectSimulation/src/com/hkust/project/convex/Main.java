package com.hkust.project.convex;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	public static final int[] totalServerOptions = { 1 };
	public static final int totalTrials = 1;
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
	public static List<double[]> assigned = new ArrayList<>();;

	// Job Scheduler
	public static FIFOJobScheduler fifoScheduler = new FIFOJobScheduler();
	public static SVFJobScheduler svfScheduler = new SVFJobScheduler();
	public static EDFJobScheduler edfScheduler = new EDFJobScheduler();
	public static STRFJobScheduler strfScheduler = new STRFJobScheduler();
	public static ProposedJobScheduler proposedScheduler = new ProposedJobScheduler();

	public static boolean startLog = false;

	// Job Inputter
	public static JobInputter jobInputter;
	public static JobInputterCallback mJICallback = new JobInputterCallback() {
		@Override
		public void onJobInserted(Job job) {
			queue.put(job.index, job);
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
		}

		@Override
		public void onJobReceived(int index, Job job, Server server) {
			servers.put(index, server);
			if (startLog) {
				double tmp[] = new double[3];
				tmp[0] = job.index;
				tmp[1] = server.index;
				tmp[2] = time;
				assigned.add(tmp);
			}
		}

		@Override
		public void onServerJobUpdate(int index, Job job) {
			if(job.remainingWorkload == 0) {
				job.completedTime = Main.time;
				completed.put(job.index, job);
			}else {
				queue.put(job.index, job);
			}
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
				// Load Job
				NavigableMap<Integer, Job> map = new ConcurrentSkipListMap<>();
				Backup backup = Backup.loadBackups("totalserver_" + totalServers + "_trial_" + trial + ".txt");
				for (int i = 0; i < totalJobs; i++) {
					map.put(backup.jobs.get(i).index, backup.jobs.get(i));
					map.get(backup.jobs.get(i).index).remainingWorkload = map.get(backup.jobs.get(i).index).workload;
				}
				// Generate Jobs
				// for (int i = 0; i < totalJobs; i++) {
				// Job job = Job.initialize(i, totalDuration, maxWorkload);
				// map.put(i, job);
				// }
				// Backup.instance(new ArrayList<>(map.values()))
				// .exportBackups("totalserver_" + totalServers + "_trial_" + trial + ".txt");
				for (int p = 0; p < 5; p++) {
					// Reinitialized all values
					completed.clear();
					queue.clear();
					for (Job j : map.values()) {
						j.completedTime = -1L;
					}
					time = 0;
					jobInputter = JobInputter.instance(map, mJICallback);
					if (p == 3) {
						proposedScheduler.bindSchedule(trial);
					}
					while (completed.size() < totalJobs) {
						jobInputter.run();
						if (queue.size() > 0) {
							NavigableMap<Integer, Job> tmpQueue = new ConcurrentSkipListMap<>(queue);
							Map<Integer, Job> schedule;
							switch (p) {
							case 0:
								schedule = fifoScheduler.assignJob(new ArrayList<>(queue.values()),
										new ArrayList<>(servers.values()));
								startLog = false;
								break;
							case 1:
								schedule = svfScheduler.assignJob(new ArrayList<>(queue.values()),
										new ArrayList<>(servers.values()));
								startLog = false;
								break;
							case 2:
								schedule = edfScheduler.assignJob(new ArrayList<>(queue.values()),
										new ArrayList<>(servers.values()));
								startLog = false;
								break;
							case 3:
								schedule = strfScheduler.assignJob(new ArrayList<>(queue.values()),
										new ArrayList<>(servers.values()));
								startLog = false;
								break;
							case 4:
								schedule = proposedScheduler.assignJob(new ArrayList<>(queue.values()),
										new ArrayList<>(servers.values()));
								startLog = true;
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
						if (j.completedTime - j.arrivalTime > max)
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
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < totalServerOptions.length; j++) {
				double average = 0.0, variation = 0.0, reliability = 0.0, max = 0.0;
				for (int k = 0; k < totalTrials; k++) {
					average += averageFlowtime[i][k][j];
					variation += variationFlowtime[i][k][j];
					reliability += relability[i][k][j];
					max += maxFlowtime[i][k][j];
				}
				System.out.println("Total Server: " + totalServerOptions[j] + ",\t  Schedule: " + getScheduleName(i)
						+ ",\t Average Flowtime: " + average / totalTrials + ",\t Variation of Flowtime: "
						+ variation / totalTrials + ",\t Reliability: " + reliability / totalTrials
						+ ",\t Max Flowtime: " + max / totalTrials);
				Results result = new Results();
				result.totalServer = String.valueOf(totalServerOptions[j]);
				result.schedule = getScheduleName(i);
				result.averageFlowtime = average / totalTrials > totalDuration ? String.valueOf(20)
						: String.valueOf(average / totalTrials);
				result.variationFlowtime = String.valueOf(variation / totalTrials);
				result.reliability = String.valueOf(reliability / totalTrials);
				result.maxFlowtime = String.valueOf(max / totalTrials);
				results.add(result);
			}
		}
		Utility.exportResults(results);
		Collections.sort(assigned, new Comparator<double[]>() {
			@Override
			public int compare(double[] arg0, double[] arg1) {
				return arg0[2] > arg1[2] ? 1 : arg0[2] < arg1[2] ? -1 : 0;
			}

		});
		for (int i = 0; i < assigned.size(); i++) {
			System.out.println("Job#" + assigned.get(i)[0] + " received by server#" + assigned.get(i)[1] + "\t @ time#"
					+ assigned.get(i)[2] + ", given workload=" + completed.get((int) assigned.get(i)[0]).workload
					+ ", arrival time=" + completed.get((int) assigned.get(i)[0]).arrivalTime + ", complete time="
					+ completed.get((int) assigned.get(i)[0]).completedTime + ", deadline = "
					+ completed.get((int) assigned.get(i)[0]).deadline);
		}
		for (int i = 0; i < servers.size(); i++) {
			System.out.println("Server #" + i + " records = " + servers.get(i).getRecords());
		}
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
