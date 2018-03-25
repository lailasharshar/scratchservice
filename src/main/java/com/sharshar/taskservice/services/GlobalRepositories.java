package com.sharshar.taskservice.services;

import com.sharshar.taskservice.beans.RepositoryDescriptor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of all the implemented exchanges
 *
 * Created by lsharshar on 3/19/2018.
 */
@Component
public class GlobalRepositories {
	private List<RepositoryDescriptor> trackerList;

	public GlobalRepositories() {
		trackerList = new ArrayList<>();
	}

	void addTracker(PriceTracker tracker, int exchangeId, int numberToSkip, ExchangeCache cache) {
		RepositoryDescriptor descriptor = new RepositoryDescriptor();
		descriptor.setTracker(tracker);
		descriptor.setExchangeId(exchangeId);
		descriptor.setNumberToSkip(numberToSkip);
		descriptor.setCurrentSkipValue(-1);
		descriptor.setCache(cache);
		tracker.setCache(cache);
		trackerList.add(descriptor);
	}

	public List<RepositoryDescriptor> getTrackerList() {
		return trackerList;
	}
}
