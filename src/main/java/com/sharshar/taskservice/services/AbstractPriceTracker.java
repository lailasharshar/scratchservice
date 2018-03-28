package com.sharshar.taskservice.services;

import com.sharshar.taskservice.algorithms.NewTickerAlgorithm;
import com.sharshar.taskservice.beans.PriceData;
import com.sharshar.taskservice.repository.PriceDataES;
import com.sharshar.taskservice.utils.ScratchConstants;
import com.sharshar.taskservice.utils.ScratchException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Since most of the functionality can be abstracted and reused, created an abstract price tracker for these
 * common methods
 *
 * Created by lsharshar on 3/19/2018.
 */
@Service
public abstract class AbstractPriceTracker implements PriceTracker {
	private static Logger logger = LogManager.getLogger();

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	NotificationService notificationService;

	@Autowired
	PriceDataES priceDataES;

	@Value( "${url}" )
	private String url;

	protected ExchangeCache cache;

	/**
	 * Returns true if the last updated data is older than the MAX_DOWN_TIME
	 * or there is no data in the database.
	 *
	 * This determines if the "new" tickers should be either trusted as really
	 * new or if it is only new to us, but has been around longer than is
	 * usable
	 *
	 * @return true if it's stale, false if it's new since the last time
	 * we've checked and that time difference was less than the MAX_DOWN_TIME
	 */
	protected boolean isStale(Date lastUpdate) {
		if (lastUpdate == null) {
			return true;
		}
		try {
			if (new Date().getTime() - lastUpdate.getTime() > ScratchConstants.MAX_EXCHANGE_DOWN_TIME) {
				return true;
			}
		} catch (Exception ex) {
			return true;
		}
		return false;
	}

	/**
	 * Retrieve the data from the exchange, determine if there is any new currencies
	 * and if there is, act on it. Save the new price data to the new database.
	 *
	 * @throws ScratchException if there was a problem executing the update
	 */
	public void processUpdate(List<PriceData> priceData, short exchange) throws ScratchException {
		// Make sure to determine if it is stale before you pull the
		// data. (If we check after we save the data to the database, of course
		// it won't be stale
		boolean staleData = isStale(cache.getLatestUpdate());
		if (staleData) {
			System.out.println("Stale data - reloading - Data is stale or older than " +
					(ScratchConstants.MAX_TIME_BEFORE_RELOAD_REQUIRED / 1000) + " seconds.");
			logger.info("Data is stale or older than "
					+ (ScratchConstants.MAX_TIME_BEFORE_RELOAD_REQUIRED / 1000) + " seconds.");
			// clear the cache. Isn't relevant anymore
			cache.clear();
		}

		// Determine which currencies are new. This also adds
		// tickers to the cache, even if we don't act on it.
		List<String> newTickers = getNewTickers(priceData);
		if (!newTickers.isEmpty() && !staleData && newTickers.size() < 200) {
			String newTickerString = String.join(", ", newTickers);
			logger.info("New " + ScratchConstants.EXCHANGES[exchange] + " Tickers: " + newTickerString);

			// Try seeing what a new ticker means
			NewTickerAlgorithm newTickerAlgorithm = applicationContext.getBean(
					NewTickerAlgorithm.class, exchange, newTickers);
			newTickerAlgorithm.analyze();
		}

		// Save the data to the database and cache
		savePriceData(priceData);
	}

	/**
	 * Save the price data to the database
	 *
	 * @param samples - the price data
	 */
	private void savePriceData(List<PriceData> samples) {
		if (samples == null) {
			return;
		}
		cache.addPricedata(samples);
		priceDataES.save(samples);
	}

	/**
	 * Give a list of retrieved price data, compare the tickers to the list
	 * of already existing ones. If there are any new ones, add it to the cache
	 * and return the list.
	 *
	 * @param priceData - the retrieved price data
	 * @return the list of new tickers
	 */
	private List<String> getNewTickers(List<PriceData> priceData) {
		List<String> newTickers = new ArrayList<>();
		if (priceData == null) {
			// If there is no price data, none of them can be new
			return newTickers;
		}

		// These are the tickers we just pulled from the exchange
		List<String> pulledTickers = priceData.stream().map(PriceData::getTicker).collect(Collectors.toList());

		// Get the cached tickers
		List<String> existingTickers = cache.getTickers();
		if (existingTickers == null) {
			existingTickers = new ArrayList<>();
		}

		// Remove all the items that already existed so we can end up with only
		// the new ones
		pulledTickers.removeAll(existingTickers);

		// Add the new ones to the list of existing ones
		cache.addAllTickers(pulledTickers);

		// Return the list of the new tickers
		return pulledTickers;
	}

	@Override
	public void setCache(ExchangeCache cache) {
		this.cache = cache;
	}
}
