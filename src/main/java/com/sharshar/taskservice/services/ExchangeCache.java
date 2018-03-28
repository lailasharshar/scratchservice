package com.sharshar.taskservice.services;

import com.sharshar.taskservice.beans.PriceData;
import com.sharshar.taskservice.utils.LimitedArrayList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Used to cache the list of tickers so we don't have to continually go to the database
 *
 * Created by lsharshar on 3/19/2018.
 */

@Service
@Scope("prototype")
public class ExchangeCache {

	@Value("${cacheSize}")
	private int cacheSize;

	private List<String> tickers;

	private Map<String, List<PriceData>> priceCache;
	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SSS");
	public ExchangeCache() {
		if (cacheSize == 0) {
			cacheSize = 100;
		}
	}

	public void setCacheSize(int size) {
		cacheSize = size;
	}

	public void setTickers(List<String> tickers) {
		this.tickers = tickers;
		if (priceCache == null) {
			priceCache = new HashMap<>();
		}
		for (String ticker : tickers) {
			priceCache.put(ticker, new LimitedArrayList<>(cacheSize));
		}
	}

	public void addPriceData(List<PriceData> priceData) {
		if (priceData == null) {
			return;
		}
		if (tickers == null) {
			tickers = new ArrayList<>();
		}
		if (priceCache == null) {
			priceCache = new HashMap<>();
		}
		for (PriceData p : priceData) {
			String ticker = p.getTicker();
			if (!tickerExists(ticker)) {
				tickers.add(ticker);
				List<PriceData> data = new LimitedArrayList<>(cacheSize);
				data.add(p);
				priceCache.put(ticker, data);
			} else {
				List<PriceData> data = priceCache.get(ticker);
				if (data == null) {
					data = new LimitedArrayList<>(cacheSize);
					priceCache.put(ticker, data);
				}
				data.add(p);
			}
			//if (ticker.equalsIgnoreCase("WANBNB")) {
			//	System.out.println(getPriceCache(ticker));
			//}
		}
	}

	 public String getPriceCache(String ticker) {
		if (priceCache == null || priceCache.get(ticker) == null) {
			return "Ticker: " + ticker + " (0)\n";
		}
		List<PriceData> data = priceCache.get(ticker);
		StringBuilder result = new StringBuilder();
		result.append("Ticker: ").append(ticker).append(" (").append(data.size()).append(")\n");
		data.forEach(p -> result.append("    Date: ").append(sdf.format(p.getUpdateTime())).append(" Price: ").append(p.getPrice()).append("\n"));
		return result.toString();
	}

	public Date getLatestUpdate() {
		Date longTimeAgo = new Date(0);
		if (priceCache == null || priceCache.size() == 0) {
			return longTimeAgo;
		}
		// Find first ticker with data
		Optional<String> firstTicker = priceCache.keySet().stream().findFirst();
		return firstTicker.map(s -> priceCache.get(s).stream()
				.map(PriceData::getUpdateTime).max(Date::compareTo).get()).orElse(longTimeAgo);
	}

	public List<PriceData> getPriceData(String ticker) {
		List<PriceData> data = priceCache.get(ticker);
		if (data != null) {
			return data;
		}
		return new LimitedArrayList<>(cacheSize);
	}

	public List<PriceData> getSortedPriceData(String ticker) {
		List<PriceData> data = getPriceData(ticker);
		return data.stream().sorted((o1, o2)->o1.getUpdateTime().
						compareTo(o2.getUpdateTime())).
						collect(Collectors.toList());
	}

	/**
	 * Add a ticker to the list if it isn't already there
	 *
	 * @param ticker - the ticker to add
	 */
	public void addTicker(String ticker) {
		// Use this way to access it so it creates it if it doesn't already exist
		List<String> tList = getTickers();
		if (tList != null && !tList.contains(ticker)) {
			tickers.add(ticker);
		}
	}

	public void addAllTickers(List<String> tickers) {
		// Loads them if they are not there
		if (tickers != null) {
			for (String t : tickers) {
				addTicker(t);
			}
		}
	}

	/**
	 * Responsible for loading the tickers the first time
	 *
	 * @return the list of tickers
	 */
	public List<String> getTickers() {
		if (tickers == null) {
			tickers = new ArrayList<>();
		}
		return tickers;
	}

	public boolean tickerExists(String ticker) {
		return ticker != null && getTickers().contains(ticker);
	}

	public void clear() {
		if (tickers != null) {
			tickers.clear();
		}
		if (priceCache != null) {
			priceCache.clear();
		}
	}
}
