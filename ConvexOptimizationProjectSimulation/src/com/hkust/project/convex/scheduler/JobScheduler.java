package com.hkust.project.convex.scheduler;

import java.util.List;
import java.util.Map;

import com.hkust.project.convex.job.Job;
import com.hkust.project.convex.server.Server;

public abstract class JobScheduler {
	public abstract Map<Integer, Job> assignJob(List<Job> jobs, List<Server> servers); //return schedules
}
