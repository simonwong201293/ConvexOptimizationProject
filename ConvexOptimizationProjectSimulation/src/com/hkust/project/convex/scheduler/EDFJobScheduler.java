package com.hkust.project.convex.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hkust.project.convex.job.Job;
import com.hkust.project.convex.server.Server;

public class EDFJobScheduler extends JobScheduler{

	@Override
	public  Map<Integer, Job> assignJob(List<Job> jobs, List<Server> servers) {
		Collections.sort(jobs, new Comparator<Job>() {
			@Override
			public int compare(Job arg0, Job arg1) {
				return arg0.deadline > arg1.deadline ? 1 : arg0.deadline < arg1.deadline ? -1 : 0;
			}
			
		});
		Set<Integer> assignedSet = new HashSet<>();
		Map<Integer, Job> result = new HashMap<>();
		for (Server server : servers) {
			if(server.occupied) continue;
			for(Job j : jobs) {
				if(assignedSet.contains(j.index)) continue;
				result.put(server.index, j);
				assignedSet.add(j.index);
			}
		}
		return result;
	}


}
