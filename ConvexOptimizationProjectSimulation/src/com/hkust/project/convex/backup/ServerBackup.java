package com.hkust.project.convex.backup;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.hkust.project.convex.Main;
import com.hkust.project.convex.job.Job;
import com.hkust.project.convex.server.Server;

public class ServerBackup {
	
	public Configuration config;
	public List<Server> servers;

	public static ServerBackup instance(List<Server> servers) {
		ServerBackup backup = new ServerBackup();
		backup.config = new Configuration();
		backup.config.maxWorkload = Main.maxWorkload;
		backup.config.totalDuration = Main.totalDuration;
		backup.config.totalJobs = Main.totalJobs;
		backup.config.totalServers = Main.totalServers;
		backup.servers = servers;
		return backup;
	}
	
	public void exportServerBackups(String name){
		List<String> lines = Arrays.asList(new Gson().toJson(this));
		Path file = Paths.get(Main.GENERATION_PAYH + File.separator + name);
		try {
			Files.write(file, lines, StandardCharsets.UTF_8);
			System.out.println("Exported to " + Main.GENERATION_PAYH + File.separator + name);
		} catch (IOException ignored) {
			System.out.println("Export failure : " + ignored.toString());
		}
	}
	
	public static ServerBackup loadBackups(String name) {
		try {
			String text = new String(Files.readAllBytes(Paths.get(Main.GENERATION_PAYH + File.separator + name)), StandardCharsets.UTF_8);
			return new Gson().fromJson(text, ServerBackup.class);
		}catch(Exception ignored) {
			return null;
		}
	}
}
