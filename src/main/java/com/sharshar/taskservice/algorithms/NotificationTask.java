package com.sharshar.taskservice.algorithms;

import com.sharshar.taskservice.beans.RepositoryDescriptor;
import com.sharshar.taskservice.services.GlobalRepositories;
import com.sharshar.taskservice.services.NotificationService;
import com.sharshar.taskservice.utils.ScratchConstants;
import com.sharshar.taskservice.utils.ScratchException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Definition for processing a url report and sending a notification Stores the
 * report id instead of the whole report because the report may change between
 * the service being scheduled and the method actually running
 */
public class NotificationTask implements Runnable {

	static final Logger logger = LogManager.getLogger();

	private String ticker;
	private int exchange;

	@Autowired
	private NotificationSchedulingManager schedulingService;

	@Autowired
	private NotificationService sendService;

	@Autowired
	private GlobalRepositories repositories;

	public NotificationTask(String ticker, int exchange) {
		this.ticker = ticker;
		this.exchange = exchange;
	}

	public NotificationTask(String ticker, int exchange, NotificationSchedulingManager schedulingManager,
							NotificationService notificationService, GlobalRepositories repositories) {
		this.ticker = ticker;
		this.exchange = exchange;
		this.schedulingService = schedulingManager;
		this.sendService = notificationService;
		this.repositories = repositories;
	}

	@Override
	public void run() {
		try {
			logger.info("Notifying on New Tickers");
			processNotification();
		} catch (ScratchException ex) {
			logger.error("Unable to Notify on New Tickers");
		}
	}

	/**
	 * Retrieve report with specified id, check if url is still active. If not,
	 * marks report as reconciled. If the url is still active, increments
	 * notification level, sends the next template in the report's template series, 
	 * and schedules the next notification
	 * 
	 * @throws ScratchException if something went wrong on the notification
	 */
	public void processNotification() throws ScratchException {
		List<RepositoryDescriptor> trackers = repositories.getTrackerList();
		String summary = "";
		for (RepositoryDescriptor rp : trackers) {
			summary += ScratchConstants.EXCHANGES[rp.getExchangeId()] + " - " + rp.getCache().getPriceCache(ticker) + "\n\n";
		}
		logger.info("Notification sent out:\n" + summary);
		sendService.notifyMe("New Ticker: " + ticker + " for " + ScratchConstants.EXCHANGES[exchange],
				summary);
	}
}
