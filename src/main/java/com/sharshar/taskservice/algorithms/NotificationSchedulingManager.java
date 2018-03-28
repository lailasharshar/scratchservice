package com.sharshar.taskservice.algorithms;

import com.sharshar.taskservice.utils.ScratchConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Service for scheduling a future notification
 */
@Component
public class NotificationSchedulingManager {
	private static final Logger logger = LoggerFactory.getLogger(NotificationSchedulingManager.class);

	/**
	 * Base Constructor
	 */
	public NotificationSchedulingManager() {
		// Nothing in here
	}

	/**
	 * Schedules a notification for specified report and date
	 * 
	 * @param ticker - the ticker to send data about
	 * @param exchange - where the data came from
	 * @param taskDate date to run task
	 */
	@Async
	void scheduleTask(String ticker, int exchange, Date taskDate) {
		ScheduledExecutorService localExecutor = Executors.newSingleThreadScheduledExecutor();
		String msg = String.format("Scheduling task to verify new: %s on %s", ticker, ScratchConstants.EXCHANGES[exchange]);
		logger.info(msg);
		TaskScheduler scheduler = new ConcurrentTaskScheduler(localExecutor);
		// If we want to be able to un-schedule notifications, this value should be
		// stored as a scheduledFuture
		scheduler.schedule(new NotificationTask(ticker, exchange), taskDate);
	}
}
