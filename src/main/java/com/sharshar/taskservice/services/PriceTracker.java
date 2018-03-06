package com.sharshar.taskservice.services;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerPrice;
import com.sharshar.taskservice.beans.PriceData;
import com.sharshar.taskservice.repository.PriceDataDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lsharshar on 1/15/2018.
 */
@Service
public class PriceTracker {
	Logger logger = LogManager.getLogger();

	// The maximum amount of time this service can be down
	// before the data is stale is 30 second
	private static final long MAX_DOWN_TIME = 1000 * 30;

	@Autowired
	private BinanceApiRestClient binanceApiRestClient;

	@Resource
	private PriceDataDAO priceDataDAO;

	// A cached version of the latest tickers to compare against
	private List<String> existingTickers;

	/**
	 * Retrieve the data from the exchange, determine if there is any new currencies
	 * and if there is, act on it. Save the new price data to the new database.
	 *
	 * @throws Exception if there was a problem executing the update
	 */
	public void processUpdate() throws Exception {
		// Make sure to determine if it is stale before you pull the
		// data. (If we check after we save the data to the database, of course
		// it won't be stale
		boolean staleData = isStale(priceDataDAO.getLatestUpdate());
		if (staleData) {
			System.out.println("Stale data - reloading");
			logger.info("Data is stale or older than "
					+ (MAX_DOWN_TIME / 1000) + " seconds.");
		}

		// Retrieve the new data from Binance
		List<PriceData> priceData = retrieveAllPriceDataFromBinance();

		// Determine which currencies are new. This also adds
		// tickers to the cache, even if we don't act on it.
		List<String> newTickers = getNewTickers(priceData);
		if (newTickers != null && newTickers.size() > 0) {
			String newTickerString = String.join(", ", newTickers);
			System.out.println("New Tickers: " + newTickerString);
			logger.info("New Tickers: " + newTickerString);
		}

		// Save the data to the database
		savePriceData(priceData);

		// If we are not stale, check to see if there is anything new
		if (!staleData && newTickers != null && newTickers.size() > 0) {
			// Right now, just notify me that there is a new one
			// TODO - Trade coins!
			System.out.println("We should act!");
		}
	}

	public List<PriceData> retrieveAllPriceDataFromBinance() throws Exception {
		List<PriceData> sampleList = new ArrayList<>();
		List<TickerPrice> allPrices = null;
		try {
			allPrices = binanceApiRestClient.getAllPrices();
		} catch (Exception ex) {
			logger.error("Unable to pull binance data", ex);
			throw new Exception("Unable to load prices");
		}
		if (allPrices == null) {
			throw new Exception("Unable to load prices");
		}
		for (TickerPrice tp : allPrices) {
			double price = Double.parseDouble(tp.getPrice());
			PriceData sample = new PriceData();
			sample.setPrice(price);
			sample.setTicker(tp.getSymbol());
			sample.setUpdateTime(new Date());
			sampleList.add(sample);
		}
		return sampleList;
	}

	/**
	 * Save the price data to the database
	 *
	 * @param samples - the price data
	 */
	private void savePriceData(List<PriceData> samples) {
		for (PriceData s : samples) {
			priceDataDAO.save(s);
		}
	}

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
	public boolean isStale(Date lastUpdate) throws SQLException{
		if (lastUpdate == null) {
			return true;
		}
		try {
			if (new Date().getTime() - lastUpdate.getTime() > MAX_DOWN_TIME) {
				return true;
			}
		} catch (Exception ex) {
			return true;
		}
		return false;
	}

	/**
	 * Give a list of retrieved price data, compare the tickers to the list
	 * of already loaded ones. If there are any new ones, return it in the
	 * list.
	 * @param priceData - the retrieved price data
	 * @return the list of new tickers
	 */
	public List<String> getNewTickers(List<PriceData> priceData) {
		List<String> newTickers = new ArrayList<>();
		// If this is the first time we've done this, load the existing
		// tickers
		if (existingTickers == null) {
			existingTickers = new ArrayList<>(); // priceDataRepository.getExistingTickers();
		}
		if (priceData == null) {
			// If there is no price data, none of them can be new
			return newTickers;
		}

		// These are the tickers we pulled from the exchange
		List<String> pulledTickers = priceData.stream().map(PriceData::getTicker).collect(Collectors.toList());
		// Remove all the items that already existed so we can end up with only
		// the new ones
		pulledTickers.removeAll(existingTickers);
		// Add the new ones to the list of existing ones
		existingTickers.addAll(pulledTickers);
		// Return the list of the new tickers
		return pulledTickers;
	}
}

