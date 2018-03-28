package com.sharshar.taskservice.algorithms;

import com.sharshar.taskservice.services.GlobalRepositories;
import com.sharshar.taskservice.services.NotificationService;
import com.sharshar.taskservice.utils.ScratchConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Perform tracking tasks for new tickers on an exchange
 *
 * Created by lsharshar on 3/19/2018.
 */
@Service
@Scope("prototype")
public class NewTickerAlgorithm {

	private Logger logger = LogManager.getLogger();

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private GlobalRepositories globalRepositories;

	@Autowired
	NotificationSchedulingManager notificationSchedulingManager;

	@Value( "${url}" )
	private String url;

	@Value( "${cacheSize}" )
	private int cacheSize;


	private List<String> newTickers;

	private short exchange;

	public NewTickerAlgorithm(short exchange, List<String> newTickers) {
		this.newTickers = newTickers;
		if (this.newTickers != null) {
			this.newTickers = newTickers.stream().distinct().collect(Collectors.toList());
		}
		this.exchange = exchange;
	}

	public void analyze() {
		if (newTickers.isEmpty() || newTickers.size() > 200) {
			return;
		}
		// If we are not stale, check to see if there is anything new
		// Right now, just notify me that there is a new one
		for (String newTicker : newTickers) {
			logger.info("New ticker for " + ScratchConstants.EXCHANGES[exchange] + ": " + newTicker);
			notifyMe(exchange, newTicker);
		}
	}

	public Date getDateForAlert() {
		int interval = ScratchConstants.PULL_INTERVAL;
		// We'll get to the point where the cache is full for the new items

		int timeToExpire = interval * cacheSize;
		// Back out 3 intervals just in case we have a delay, don't want the new price to fall off
		timeToExpire = timeToExpire - interval - interval - interval;

		return new Date(new Date().getTime() + timeToExpire);
	}

	private void notifyMe(int newExchange, String ticker) {
		try {
			notificationSchedulingManager.scheduleTask(ticker, newExchange, getDateForAlert());
		} catch (Exception ex) {
			logger.error("Unable to notify me", ex);
		}
	}
}
