package com.sharshar.taskservice.algorithms;

import com.sharshar.taskservice.beans.PriceData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Algorithm to search the data for micro (or macro) trends in data. This detects whether a price increases or decreases
 * significantly  over a period of time
 *
 * Created by lsharshar on 3/28/2018.
 */
@Service
public class SignalSearcher {
	private List<PriceData> dataToSearch;

	public class SignalIdentifier {
		Date startDate;
		Date endDate;
		String ticker;
		short exchange;
		double multiplier;

		public Date getStartDate() {
			return startDate;
		}

		public void setStartDate(Date startDate) {
			this.startDate = startDate;
		}

		public Date getEndDate() {
			return endDate;
		}

		public void setEndDate(Date endDate) {
			this.endDate = endDate;
		}

		public String getTicker() {
			return ticker;
		}

		public void setTicker(String ticker) {
			this.ticker = ticker;
		}

		public short getExchange() {
			return exchange;
		}

		public void setExchange(short exchange) {
			this.exchange = exchange;
		}

		public double getMultiplier() {
			return multiplier;
		}

		public void setMultiplier(double multiplier) {
			this.multiplier = multiplier;
		}

		public SignalIdentifier(Date startDate, Date endDate, String ticker, short exchange, double multiplier) {
			this.startDate = startDate;
			this.endDate = endDate;
			this.ticker = ticker;
			this.exchange = exchange;
			this.multiplier = multiplier;
		}
	}

	public void setDataToSearch(List<PriceData> dataToSearch) {
		this.dataToSearch = dataToSearch;
		// sort it
		this.dataToSearch = this.dataToSearch.stream()
				.sorted(Comparator.comparing(PriceData::getUpdateTime)).collect(Collectors.toList());
	}

	public List<List<PriceData>> splitUpData(List<PriceData> priceData, long timeSpan) {
		List<List<PriceData>> splitData = new ArrayList<>();
		if (timeSpan == 0 || priceData == null || priceData.isEmpty()) {
			return splitData;
		}
		List<PriceData> priceDataCopy = priceData.stream()
				.sorted(Comparator.comparing(PriceData::getUpdateTime)).collect(Collectors.toList());
		while (!priceDataCopy.isEmpty()) {
			List<PriceData> inInterval = getDataWithinInterval(priceDataCopy, timeSpan);
			priceDataCopy.removeAll(inInterval);
			splitData.add(inInterval);
		}
		return splitData;
	}

	/**
	 * Determine if there are signals within a range of data. A signal is considered anyplace where price
	 * data fluctuates more than a minMultiplier within that interval time. For example, if the minMultiplier
	 * is 2, it looks to see if the min price in that range and max price in that range are greater than or equal
	 * to a multiple of 2. If the min price is 25, it will test to see if the max price is >= 50
	 *
	 * @param listToSearch - list to search for signals
	 * @return the list of signals within the data
	 */

	public List<SignalIdentifier> getSignals(List<PriceData> listToSearch, double maxMultiplier, double minMultiplier) {
		// Create a new list with the data list so that if we remove any, it doesn't mess with class data.
		List<SignalIdentifier> identifiers = new ArrayList<>();
		PriceData maxPrice = Collections.max(listToSearch, Comparator.comparing(PriceData::getPrice));
		PriceData minPrice = Collections.min(listToSearch, Comparator.comparing(PriceData::getPrice));
		PriceData maxDate = Collections.max(listToSearch, Comparator.comparing(PriceData::getUpdateTime));
		PriceData minDate = Collections.min(listToSearch, Comparator.comparing(PriceData::getUpdateTime));

		// Find the value of the multiplier
		double multiplier = 0;

		// If the price increased over time, the multiplier is over the min price
		if (minPrice.getUpdateTime().getTime() <= maxPrice.getUpdateTime().getTime()) {
			multiplier = (maxPrice.getPrice() - minPrice.getPrice())/minPrice.getPrice();
			System.out.println("Mult: " + multiplier);
			if (multiplier < maxMultiplier) {
				// We didn't find anything that might apply along the entire data set, we're done
				return identifiers;
			}
		}
		// If the price decreased over time, the multiplier is over the max price and negative
		if (minPrice.getUpdateTime().getTime() > maxPrice.getUpdateTime().getTime()) {
			multiplier = (maxPrice.getPrice() - minPrice.getPrice())/maxPrice.getPrice();
			multiplier = multiplier * -1;
			System.out.println("Mult: " + multiplier);
			// Since the value is negative, a greater decrease means a lower number
			if (multiplier > minMultiplier) {
				return identifiers;
			}
		}

		SignalIdentifier signal = new SignalIdentifier(minDate.getUpdateTime(), maxDate.getUpdateTime(),
				minDate.getTicker(), minDate.getExchange(), multiplier);
		identifiers.add(signal);
		return identifiers;
	}

	public List<PriceData> getDataWithinInterval(List<PriceData> list, long interval) {
		List<PriceData> dataInInterval = new ArrayList<>();
		if (list == null || list.isEmpty() || interval == 0) {
			return dataInInterval;
		}
		// Get everything between the 1st update time in the list to and up to that date plus the interval
		return list.stream()
				.filter(c -> c.getUpdateTime().getTime() < list.get(0).getUpdateTime().getTime() + interval)
				.collect(Collectors.toList());
	}
}
