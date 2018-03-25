package com.sharshar.taskservice.beans;

import com.sharshar.taskservice.services.ExchangeCache;
import com.sharshar.taskservice.services.PriceTracker;

/**
 * Used to describe all the common objects needed for an exchange description
 *
 * Created by lsharshar on 3/20/2018.
 */
public class RepositoryDescriptor {
	/* the tracker used to pull and serialize the data associated with an exchange */
	private PriceTracker tracker;
	/* The constant id (used in the database) to define the exchange */
	private int exchangeId;
	/* If there is a problem with accessing the service, how many attempts to skip */
	private int numberToSkip;
	/* If there was a problem and we needed to skip, what number are we on now */
	private int currentSkipValue;
	/* The cache to use for this exchange so we don't have to go to the DB every time */
	private ExchangeCache cache;

	public PriceTracker getTracker() {
		return tracker;
	}

	public void setTracker(PriceTracker tracker) {
		this.tracker = tracker;
	}

	public int getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(int exchangeId) {
		this.exchangeId = exchangeId;
	}

	public int getNumberToSkip() {
		return numberToSkip;
	}

	public void setNumberToSkip(int numberToSkip) {
		this.numberToSkip = numberToSkip;
	}

	public int getCurrentSkipValue() {
		return currentSkipValue;
	}

	public void setCurrentSkipValue(int currentSkipValue) {
		this.currentSkipValue = currentSkipValue;
	}

	public void incrementSkip() {
		this.currentSkipValue++;
	}

	public ExchangeCache getCache() {
		return cache;
	}

	public void setCache(ExchangeCache cache) {
		this.cache = cache;
	}
}
