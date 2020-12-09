package com.hkust.project.convex.server;

import java.util.ArrayList;
import java.util.List;

import com.hkust.project.convex.Main;
import com.hkust.project.convex.job.Job;

public class Server {
	public interface ServerCallback {
		public void onJobReceived(int index, Job job, Server server);
		public void onServerJobUpdate(int index, Job job);
		public void onServerCompleted(int index, Job job);
	}

	public int index = -1;
	public boolean occupied = false;
	public Long inputTime = -1L;
	public Job existingJob = null;
	public ServerCallback mCallback;
	public List<Integer> savedJobs = new ArrayList<>();

	public static Server instance(int index, ServerCallback callback) {
		Server server = new Server();
		server.index = index;
		server.mCallback = callback;
		return server;
	}

	public void assign(Job job) {
		if(occupied) {
			if(mCallback != null) {
				mCallback.onServerJobUpdate(index, job);
			}
		}
		occupied = true;
		existingJob = job;
		inputTime = Main.time;
		if (mCallback != null)
			mCallback.onJobReceived(index, job, this);
	}

	public List<Integer> getRecords() {
		return savedJobs;
	}

	public void run() {
		if (!occupied) {
			if (Main.startLog)
				savedJobs.add(0);
			return;
		}
		if (Main.startLog)
			savedJobs.add(existingJob.index);
		existingJob.remainingWorkload--;
		if (existingJob.remainingWorkload == 0) {
			if (mCallback != null)
				mCallback.onServerCompleted(index, existingJob);
			existingJob = null;
			occupied = false;
			inputTime = -1L;
		}
	}
}
