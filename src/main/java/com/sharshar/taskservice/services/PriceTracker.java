package com.sharshar.taskservice.services;

import com.sharshar.taskservice.beans.PriceData;
import com.sharshar.taskservice.utils.ScratchException;

import java.util.List;

/**
 * Created by lsharshar on 3/19/2018.
 */
public interface PriceTracker {
	void processUpdate(ExchangeCache cache) throws ScratchException;
	List<PriceData> retrieveAllPriceData() throws ScratchException;
	int getExchange();
	List<String> getTickers();
	void setCache(ExchangeCache cache);
}
