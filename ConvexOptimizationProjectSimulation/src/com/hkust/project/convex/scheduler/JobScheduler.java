package com.hkust.project.convex.scheduler;

import com.hkust.project.convex.job.Job;

public abstract class JobScheduler {
	
	public abstract int assignJob(Job job); //return server index
	
}
