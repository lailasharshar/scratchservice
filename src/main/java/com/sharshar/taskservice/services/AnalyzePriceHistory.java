package com.sharshar.taskservice.services;

import com.sharshar.taskservice.beans.PriceData;
import com.sharshar.taskservice.repository.PriceDataES;
import com.sharshar.taskservice.utils.ScratchConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Class to analyze different approaches to holding/selling currencies
 *
 * It takes different
 *
 * Created by lsharshar on 1/20/2018.
 */
@Service
public class AnalyzePriceHistory {
	public class Analysis {
		String exchange;
		double initialPrice;
		double sellPrice;
		double gain;
		String errorMessage;
	}

	@Autowired
	PriceDataES priceDataES;

	private List<PriceData> getIntialPriceDataForTicker(String ticker, short exchange, int num) {
		return priceDataES.findByTicker(ticker, exchange);
	}

	/**
	 * Analyze what we would have sold a particular currency for based on a parameterized step list
	 *
	 * @param ticker - the ticker to analyze
	 * @param steps - the steps to analyze
	 * @return the price the steps would have ended up advising to sell at
	 * @throws Exception if there is a configuration or database problem
	 */
	public Analysis analyzeSellPrice(String ticker, List<Pair<Double, Double>> steps, short exchange) throws Exception {
		PriceStepper stepper = new PriceStepper(steps);
		List<PriceData> dataList = getIntialPriceDataForTicker(ticker, exchange, 500);
		if (dataList == null || dataList.isEmpty()) {
			Analysis analysis = new Analysis();
			analysis.initialPrice = 0;
			analysis.sellPrice = 0;
			analysis.gain = 0.0;
			analysis.errorMessage = "Unable to retrieve data on " + ticker;
			return analysis;
		}
		boolean first = true;
		for (PriceData data : dataList) {
			if (first) {
				stepper.setPurchasePrice(data.getPrice());
				first = false;
			} else {
				double price = data.getPrice();
				if (stepper.shouldSell(price)) {
					Analysis analysis = new Analysis();
					analysis.exchange = ScratchConstants.EXCHANGES[exchange];
					analysis.initialPrice = stepper.getPurchasePrice();
					analysis.sellPrice = price;
					analysis.gain = price/analysis.initialPrice;
					analysis.errorMessage = "";
					return analysis;
				}
			}
		}
		Analysis analysis = new Analysis();
		analysis.exchange = ScratchConstants.EXCHANGES[exchange];
		analysis.initialPrice = stepper.getPurchasePrice();
		analysis.sellPrice = 0;
		analysis.gain = 0.0;
		analysis.errorMessage = "Still shouldn't sell " + ticker;
		return analysis;
	}
}
