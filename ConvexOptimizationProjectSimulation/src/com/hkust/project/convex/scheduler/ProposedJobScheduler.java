package com.hkust.project.convex.scheduler;

import java.util.ArrayList;
import java.util.List;

import com.hkust.project.convex.job.Job;
import com.hkust.project.convex.server.Server;

public class ProposedJobScheduler extends JobScheduler{
	
	private List<Server> tmpServerStatus = new ArrayList<>();
	
	
	public void updateServerStatus(List<Server> tmpServerStatus) {
		this.tmpServerStatus = tmpServerStatus;
	}
	

	@Override
	public int assignJob(Job job) {
		for (Server s : tmpServerStatus) {
			if(!s.occupied)
				return s.index;
		}
		return -1;
	}

}
