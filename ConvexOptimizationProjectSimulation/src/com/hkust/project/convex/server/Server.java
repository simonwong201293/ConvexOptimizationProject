package com.hkust.project.convex.server;

import com.hkust.project.convex.Main;
import com.hkust.project.convex.job.Job;

public class Server{
	public interface ServerCallback {
		public void onJobReceived(int index, Job job, Server server);
		public void onServerCompleted(int index, Job job);
	}
	
	public int index = -1;
	public boolean occupied = false;
	public Long inputTime = -1L;
	public Job existingJob = null;
	public ServerCallback mCallback;
	
	public static Server instance(int index, ServerCallback callback) {
		Server server = new Server();
		server.index = index;
		server.mCallback = callback;
		return server;
	}
	
	public void assign(Job job) {
		occupied = true;
		existingJob = job;
		inputTime = Main.time;
		if(mCallback != null)
			mCallback.onJobReceived(index, job, this);
	}
	
	public void run() {
		if(!occupied) return;
		if(Main.time == inputTime + existingJob.workload) {
			if(mCallback != null)
				mCallback.onServerCompleted(index, existingJob);
			existingJob = null;
			occupied = false;
			inputTime = -1L;
		}
	}
}
