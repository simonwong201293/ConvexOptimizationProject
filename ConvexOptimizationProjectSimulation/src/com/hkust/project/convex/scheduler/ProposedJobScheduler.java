package com.hkust.project.convex.scheduler;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.hkust.project.convex.Main;
import com.hkust.project.convex.backup.Backup;
import com.hkust.project.convex.job.Job;
import com.hkust.project.convex.server.Server;

public class ProposedJobScheduler extends JobScheduler{

	Double[][][] schedule;
	
	@Override
	public  Map<Integer, Job> assignJob(List<Job> jobs, List<Server> servers) {
		Set<Integer> assignedSet = new HashSet<>();
		Map<Integer, Job> result = new HashMap<>();
		System.out.println("Time = " + Main.time);
		for(Job j : jobs) {
//			boolean isDone = false;
//			if(assignedSet.contains(j.index)) continue;
//			while(!isDone) {
//				int[] options = findMaximumIndex(getMatrixfromIndex(schedule, j.index));
//				System.out.println("Job#"+j.index+", max = server=" + options[0]+", time = " + options[1] +" @ " + Main.time);
//				if(options[0] == -1 && options[1] == -1) {
//					for(Server s : servers) {
//						if(!s.occupied) {
//							result.put(s.index, j);
//							assignedSet.add(j.index);
//							servers.get(s.index).occupied = true;
//							System.out.println("Job#"+j.index+"assigned to server#"+s.index +" By condition 1");
//							isDone = true;
//							break;
//						}
//					}
//					break;
//				}else if(options[1] > Main.time) {
//					break;
//				}else if(servers.get(options[0]).occupied) {
//					schedule[j.index][options[0]][options[1]] = -1d;
//					continue;
//				}
//				result.put(options[0], j);
//				assignedSet.add(j.index);
//				servers.get(options[0]).occupied = true;
//				System.out.println("Job#"+j.index+"assigned to server#"+options[0] +" By condition 2");
//				isDone = true;
//			}
			
		}
//		for (Server server : servers) {
//			if(server.occupied) continue;
//			for(Job j : jobs) {
//				if(assignedSet.contains(j.index)) continue;
//				result.put(server.index, j);
//				assignedSet.add(j.index);
//			}
//		}
		return result;
	}
	
	public static Double[][] getMatrixfromIndex(Double[][][] a, int jobIndex){
		Double[][] matrix = new Double[a[0].length][a[0][0].length];
//		System.out.println("i = " + a[0].length + ", j = " + a[0][0].length);
		for(int i = 0 ; i < a[0].length; i++) {
			for (int j = 0; j < a[0][0].length; j++) {
				matrix[i][j] = a[jobIndex][i][j];
			}
		}
		return matrix;
	}
	
	 public static int[] findMaximumIndex(Double[][] a)
	 {
		 double maxVal = -1;
		 int[] answerArray = new int[2];
	     for(int row = 0; row < a.length; row++)
	     {
	         for(int col = 0; col < a[row].length; col++)
	         {
	             if(a[row][col] > maxVal)
	             {
	                 maxVal = a[row][col];
	                 answerArray[0] = row; // server
	                 answerArray[1] = col; // time
	             }
	         }
	     }
	     if(maxVal == -1) {
	    	 answerArray[0] = -1; // server
             answerArray[1] = -1; // time
	     }
	     return answerArray;
	 }
	
	public void bindSchedule(int trial) {
		try {
			String text = new String(Files.readAllBytes(Paths.get(Main.GENERATION_PAYH + File.separator + "totalserver_"+Main.totalServers+"_trial_"+trial+"_result.txt")), StandardCharsets.UTF_8);
			schedule = new Gson().fromJson(text, Double[][][].class);
		}catch(Exception ignored) {
		}
	}
	
}
