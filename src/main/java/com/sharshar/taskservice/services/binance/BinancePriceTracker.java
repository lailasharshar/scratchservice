package com.sharshar.taskservice.services.binance;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.TickerPrice;
import com.sharshar.taskservice.beans.PriceData;
import com.sharshar.taskservice.services.ExchangeCache;
import com.sharshar.taskservice.utils.ScratchConstants;
import com.sharshar.taskservice.utils.ScratchException;
import com.sharshar.taskservice.services.AbstractPriceTracker;
import com.sharshar.taskservice.services.PriceTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Does the analysis to see if Binance has a new coin
 *
 * Created by lsharshar on 1/15/2018.
 */
@Service
public class BinancePriceTracker extends AbstractPriceTracker implements PriceTracker {
	private static Logger logger = LogManager.getLogger();
	private static short exchange = ScratchConstants.BINANCE;

	@Autowired
	private BinanceApiRestClient binanceApiRestClient;

	@Override
	public int getExchange() {
		return exchange;
	}

	@Override
	public List<String> getTickers() {
		List<String> tickers = cache.getTickers();
		if (tickers == null || tickers.isEmpty()) {
			List<TickerPrice> data = binanceApiRestClient.getAllPrices();
			List<String> tickerStrings = data.stream().map(TickerPrice::getSymbol).collect(Collectors.toList());
			super.cache.setTickers(tickerStrings);
			return tickerStrings;
		}
		return tickers;
	}

	@Override
	public void processUpdate(ExchangeCache cache) throws ScratchException {
		super.processUpdate(retrieveAllPriceData(), exchange);
	}

	@Override
	public List<PriceData> retrieveAllPriceData() throws ScratchException {
		List<PriceData> sampleList = new ArrayList<>();
		List<TickerPrice> allPrices;
		try {
			allPrices = binanceApiRestClient.getAllPrices();
		} catch (Exception ex) {
			logger.error("Unable to pull binance data", ex);
			throw new ScratchException("Unable to load prices from Binance", ex);
		}
		if (allPrices == null) {
			throw new ScratchException("Unable to load prices from Binance");
		}
		for (TickerPrice tp : allPrices) {
			double price = Double.parseDouble(tp.getPrice());
			PriceData sample = new PriceData();
			sample.setPrice(price);
			sample.setTicker(tp.getSymbol());
			sample.setUpdateTime(new Date());
			sample.setExchange(exchange);
			sampleList.add(sample);
		}
		return sampleList;
	}
}

