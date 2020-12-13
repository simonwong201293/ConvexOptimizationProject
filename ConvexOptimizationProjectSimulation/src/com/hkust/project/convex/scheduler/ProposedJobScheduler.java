package com.hkust.project.convex.scheduler;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.hkust.project.convex.Main;
import com.hkust.project.convex.backup.JobBackup;
import com.hkust.project.convex.job.Job;
import com.hkust.project.convex.server.Server;

public class ProposedJobScheduler extends JobScheduler {

	int[][][] schedule;
	int[][] singleServerSchedule;

	@Override
	public Map<Integer, Job> assignJob(List<Job> jobs, List<Server> servers) {
		Set<Integer> assignedSet = new HashSet<>();
		Map<Integer, Job> result = new HashMap<>();
		if (singleServerSchedule != null) {
			for (int j = 0; j < Main.totalJobs; j++) {
				if (singleServerSchedule[j][(int) Main.time] == 1) {
					Job job = findJob(j, jobs);
					if (job != null) {
						result.put(0, job);
					}
				}
			}
		} else if(schedule != null){
			if (Main.time <= Main.totalDuration) {
				for (int i = 0; i < schedule.length; i++) {
					for (int j = 0; j < schedule[0].length; j++) {
						if (schedule[i][j][(int) Main.time] == 1) {
							Job job = findJob(i, jobs);
							if (job != null) {
								result.put(j, job);
							}
						}
					}
				}
			}
//			else {
//				Collections.sort(jobs, new Comparator<Job>() {
//					@Override
//					public int compare(Job arg0, Job arg1) {
//						return arg0.deadline > arg1.deadline ? 1 : arg0.deadline < arg1.deadline ? -1 : 0;
//					}
//
//				});
//				for (Server server : servers) {
//					if (server.occupied)
//						continue;
//					for (Job j : jobs) {
//						if (assignedSet.contains(j.index))
//							continue;
//						result.put(server.index, j);
//						assignedSet.add(j.index);
//						servers.get(server.index).occupied = true;
//						break;
//					}
//				}
//			}
		}else {
			for (Server server : servers) {
				if(server.occupied) continue;
				for(Job j : jobs) {
					if(assignedSet.contains(j.index)) continue;
					result.put(server.index, j);
					assignedSet.add(j.index);
					servers.get(server.index).occupied = true;
				}
			}
		}
		return result;
	}

	public Job findJob(int jobIndex, List<Job> jobs) {
		for (Job job : jobs) {
			if (job.index == jobIndex)
				return job;
		}
		return null;
	}

	public static int[] findMaximumIndex(Double[][] a) {
		double maxVal = -1;
		int[] answerArray = new int[2];
		for (int row = 0; row < a.length; row++) {
			for (int col = 0; col < a[row].length; col++) {
				if (a[row][col] > maxVal) {
					maxVal = a[row][col];
					answerArray[0] = row; // server
					answerArray[1] = col; // time
				}
			}
		}
		if (maxVal == -1) {
			answerArray[0] = -1; // server
			answerArray[1] = -1; // time
		}
		return answerArray;
	}

	public void bindSchedule(int trial) {
		try {
			String text = new String(Files.readAllBytes(Paths.get(Main.SOLUTION_PATH + File.separator + "totalserver_"
					+ Main.totalServers + "_trial_" + trial + "_result.txt")), StandardCharsets.UTF_8);
			try {
				schedule = new Gson().fromJson(text, int[][][].class);
			} catch (Exception ignored) {
				singleServerSchedule = new Gson().fromJson(text, int[][].class);
			}
		} catch (Exception ignored) {
			System.out.println("Schedule import e = " + ignored.toString());
		}
	}

}
