package com.sharshar.taskservice.services;

import com.sharshar.taskservice.beans.RepositoryDescriptor;
import com.sharshar.taskservice.utils.ScratchConstants;
import com.sharshar.taskservice.services.binance.BinancePriceTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Scheduled job to update price tracker info
 *
 * Created by lsharshar on 1/15/2018.
 */
@Service
public class DataPullJob {
	private Logger logger = LogManager.getLogger();

	@Autowired
	private ApplicationContext applicationContext;

	// All the exchange configuration data
	@Autowired
	private GlobalRepositories repositories;

	@Autowired
	private BinancePriceTracker binancePriceTracker;

	/**
	 * A scheduled job to update any price trackers we would like to use. As new ones are added,
	 * they should be processed here. If this is the first time, create and insert the trackers.
	 * Was having a problem with this when it was in the constructor since the repository was being
	 * created after this bean
	 */
	@Scheduled(fixedRate = 10000)
	public void doTracking() {
		if (this.repositories.getTrackerList() == null) {
			return;
		}
		// If this is the first time, load the trackers
		if (this.repositories.getTrackerList().isEmpty()) {
			// Binance
			this.repositories.addTracker(binancePriceTracker, ScratchConstants.BINANCE, 3,
					applicationContext.getBean(ExchangeCache.class));
		}
		for (RepositoryDescriptor descriptor : this.repositories.getTrackerList()) {
			logger.info("Updating " +
					ScratchConstants.EXCHANGES[descriptor.getExchangeId()] +
					": " + new Date());
			doTracking(descriptor);
		}
	}

	/**
	 * Update the tracking data and determine if there is anything new. If we have had problems with the
	 * service, we can skip a few scheduled updates.
	 *
	 * @param descriptor - the information related to the exchange
	 */
	private void doTracking(RepositoryDescriptor descriptor) {
		if (descriptor.getCurrentSkipValue() >= 0 && descriptor.getCurrentSkipValue() < descriptor.getNumberToSkip()) {
			descriptor.incrementSkip();
			if (descriptor.getCurrentSkipValue()  > descriptor.getNumberToSkip()) {
				logger.info("We've skipped enough, restart");
				descriptor.setCurrentSkipValue(-1);
			} else {
				return;
			}
		}
		try {
			descriptor.getTracker().processUpdate(descriptor.getCache());
		} catch (Exception ex) {
			// We are having a problem skip tasks for a while
			logger.error("Problem updating data, skip " + descriptor.getNumberToSkip(), ex);
			descriptor.setCurrentSkipValue(0);
		}
	}
}
