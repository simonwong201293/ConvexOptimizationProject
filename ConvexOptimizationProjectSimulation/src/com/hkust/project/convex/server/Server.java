package com.hkust.project.convex.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;
import com.hkust.project.convex.Main;
import com.hkust.project.convex.job.Job;
import com.hkust.project.convex.util.Utility;

public class Server {
	public interface ServerCallback {
		public void onJobReceived(int index, Job job, Server server);

		public void onServerJobUpdate(int index, Job job);

		public void onServerCompleted(int index, Job job);
	}

	public int index = -1;
	public boolean occupied = false;
	public double multiplier;
	public Long inputTime = -1L;
	public Job existingJob = null;
	public ServerCallback mCallback;
	public List<Integer> savedJobs = new ArrayList<>();

	public static Server instance(int index, ServerCallback callback) {
		Server server = new Server();
		server.index = index;
		server.multiplier = Main.heterogenous ? Utility.convert(Main.serverRand.nextInt(4)) : 1.0;
		server.mCallback = callback;
		return server;
	}

	public void bindCallback(ServerCallback callback) {
		mCallback = callback;
	}

	public Job assign(Job job) {
		Job result = null;
		if (occupied && existingJob != null) {
			result = existingJob;
			if (mCallback != null) {
				mCallback.onServerJobUpdate(index, existingJob);
			}
		}
		occupied = true;
		existingJob = job;
		inputTime = Main.time;
		if (mCallback != null)
			mCallback.onJobReceived(index, job, this);
		return result;
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
		if (existingJob.remainingWorkload - multiplier < 0)
			existingJob.remainingWorkload = 0;
		else
			existingJob.remainingWorkload -= multiplier;
		// System.out.println("Existing Job = " + new Gson().toJson(existingJob));
		if (existingJob.remainingWorkload <= 0 || existingJob.deadline <= Main.time) {
			if (mCallback != null)
				mCallback.onServerCompleted(index, existingJob);
			existingJob = null;
			occupied = false;
			inputTime = -1L;
		}
	}
}
