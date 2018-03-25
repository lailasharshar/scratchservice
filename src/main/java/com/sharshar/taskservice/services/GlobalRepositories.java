package com.sharshar.taskservice.services;

import com.sharshar.taskservice.beans.RepositoryDescriptor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of all the implemented exchanges
 *
 * Created by lsharshar on 3/19/2018.
 */
@Component
public class GlobalRepositories {
	List<RepositoryDescriptor> trackerList;

	public GlobalRepositories() {
		System.out.println("Instantiating GlobalRepositories");
		trackerList = new ArrayList<>();
	}

	public void addTracker(PriceTracker tracker, int exchangeId, int numberToSkip, ExchangeCache cache) {
		RepositoryDescriptor descriptor = new RepositoryDescriptor();
		descriptor.setTracker(tracker);
		descriptor.setExchangeId(exchangeId);
		descriptor.setNumberToSkip(numberToSkip);
		descriptor.setNumberToSkip(-1);
		descriptor.setCache(cache);
		tracker.setCache(cache);
		trackerList.add(descriptor);
	}

	public List<RepositoryDescriptor> getTrackerList() {
		return trackerList;
	}
}
