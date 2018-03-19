package com.sharshar.taskservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lsharshar on 1/15/2018.
 */
@Component
public class DataPullJob {
	public static boolean doJob = true;

	private static final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

	private boolean skipAFew = false;
	private int skipped = 0;
	private int numberToSkip = 5;

	@Autowired
	PriceTracker priceTracker;

	@Scheduled(fixedRate = 10000)
	public void reportCurrentTime() {
		if (skipAFew) {
			skipped++;
			if (skipped > numberToSkip) {
				System.out.println("We've skipped enough, restart");
				skipAFew = false;
			} else {
				return;
			}
		}
		if (doJob) {
			try {
				//System.out.println("Updating data: " + sdf.format(new Date()));
				priceTracker.processUpdate();
			} catch (Exception ex) {
				// We are having a problem skip tasks for a while
				skipAFew = true;
				ex.printStackTrace();
			}
		}
	}
}
