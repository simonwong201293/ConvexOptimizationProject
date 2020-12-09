package com.hkust.project.convex.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hkust.project.convex.job.Job;
import com.hkust.project.convex.server.Server;

public class FIFOJobScheduler extends JobScheduler{

	@Override
	public  Map<Integer, Job> assignJob(List<Job> jobs, List<Server> servers) {
		Set<Integer> assignedSet = new HashSet<>();
		Map<Integer, Job> result = new HashMap<>();
		for (Server server : servers) {
			if(server.occupied) continue;
			for(Job j : jobs) {
				if(assignedSet.contains(j.index)) continue;
				result.put(server.index, j);
				assignedSet.add(j.index);
				servers.get(server.index).occupied = true;
			}
		}
		return result;
	}


}
