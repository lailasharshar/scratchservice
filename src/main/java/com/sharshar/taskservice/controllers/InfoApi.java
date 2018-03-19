package com.sharshar.taskservice.controllers;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.api.client.domain.market.TickerStatistics;
import com.sharshar.taskservice.beans.PriceData;
import com.sharshar.taskservice.repository.PriceDataDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Basic service to provide access to Binance endpoints
 *
 * Created by lsharshar on 1/14/2018.
 */
@RestController
public class InfoApi {

	@Autowired
	private BinanceApiRestClient binanceApiRestClient;

	@Resource
	private PriceDataDAO priceDataDAO;

	private static SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
	private static final CandlestickInterval DEFAILT_CANDLESTICK = CandlestickInterval.DAILY;
	private static final Map<String, CandlestickInterval> candleIntervals;
	static {
		Map<String, CandlestickInterval> intervalMap = new HashMap<>();
		intervalMap.put("1m", CandlestickInterval.ONE_MINUTE);
		intervalMap.put("3m", CandlestickInterval.THREE_MINUTES);
		intervalMap.put("5m", CandlestickInterval.FIVE_MINUTES);
		intervalMap.put("15m", CandlestickInterval.FIFTEEN_MINUTES);
		intervalMap.put("30m", CandlestickInterval.HALF_HOURLY);
		intervalMap.put("1h", CandlestickInterval.HOURLY);
		intervalMap.put("2h", CandlestickInterval.TWO_HOURLY);
		intervalMap.put("4h", CandlestickInterval.FOUR_HORLY);
		intervalMap.put("6h", CandlestickInterval.SIX_HOURLY);
		intervalMap.put("8h", CandlestickInterval.EIGHT_HOURLY);
		intervalMap.put("12h", CandlestickInterval.TWELVE_HOURLY);
		intervalMap.put("1d", CandlestickInterval.DAILY);
		intervalMap.put("3d", CandlestickInterval.THREE_DAILY);
		intervalMap.put("1w", CandlestickInterval.WEEKLY);
		intervalMap.put("1M", CandlestickInterval.MONTHLY);
		candleIntervals = Collections.unmodifiableMap(intervalMap);
	}

	/**
	 * Ping the Binance server to determine if it's up
	 *
	 * @return true if ping worked
	 */
	@RequestMapping("/ping")
	public boolean ping() {
		binanceApiRestClient.ping();
		return true;
	}

	/**
	 * Return the system time from Binance
	 *
	 * @return the time
	 */
	@RequestMapping("/systemTime")
	public String getSystemTime() {
		long serverTime = binanceApiRestClient.getServerTime();
		Date d = new Date(serverTime);
		return sdf.format(d);
	}

	/**
	 * The last time we updated the data in the database - the last time a pull
	 * was done. Used to determine how stale the data is
	 *
	 * @return the max date in the price data
	 */
	@RequestMapping("/lastRunTime")
	public String lastUpdateTime() {
		Date d = priceDataDAO.getLatestUpdate();
		if (d == null) {
			return "";
		}
		return sdf.format(d);
	}

	/**
	 * Determine the current price of the a ticker
	 *
	 * @param ticker - for example ETHBTC to determine the exchange rate between
	 *               Ethereum and Bitcoin
	 * @return the current price
	 */
	@RequestMapping("/price/{ticker}")
	public String getPrice(@PathVariable String ticker) {
		return binanceApiRestClient.get24HrPriceStatistics(ticker).getLastPrice();
	}

	/**
	 * Retrieve the current price of all items on the Binance exchange. This is
	 * useful for determining if or when new currencies come online
	 *
	 * @return the list of all currencies and their prices
	 */
	@RequestMapping("/allPrices")
	public List<TickerPrice> getAllBooks() {
		List<TickerPrice> prices = binanceApiRestClient.getAllPrices();
		return prices;
	}

	/**
	 * The last day's prices for a currency
	 * @param ticker
	 * @return
	 */
	@RequestMapping("/history24/{ticker}")
	public TickerStatistics getHistory24(@PathVariable String ticker) {
		TickerStatistics stats = binanceApiRestClient.get24HrPriceStatistics(ticker);
		return stats;
	}


	/**
	 * Retrieve a history list of a particular ticket
	 *
	 * @param ticker - Ticker ID
	 * @param startDate - Start date in the format of "10/31/2017 18:14"
	 * @param endDate - Start date in the format of "10/31/2017 18:15"
	 * @param interval - The sampling interval between the start and end date to retrieve
	 *        data. Valid values include:
	 *              1m - one minute
	 *              3m - 3 minutes
	 *              5m - 5 minutes
	 *              15m - 15 minutes
	 *              30m - 30 minutes
	 *              1h - 1 hour
	 *              2h - 2 hours
	 *              4h - 4 hours
	 *              6h - 6 hours
	 *              8h - 8 hours
	 *              12h - 12 hours
	 *              1d - 1 day
	 *              3d - 3 days
	 *              1w - 1 week
	 *              1M - 1 month
	 *
	 * @return The candlesticks (high, low range) for each interval between those two dates.
	 * The maximum number of results will be 500 returned.
	 */
	@RequestMapping("/history/{ticker}")
	public List<Candlestick> getHistory(@PathVariable String ticker,
				@RequestParam String startDate,
				@RequestParam String endDate,
				@RequestParam(defaultValue = "1d") String interval) {
		Date startDateVal = null;
		Date endDateVal = null;
		try {
			startDateVal = sdf.parse(startDate + ":00");
			endDateVal = sdf.parse(endDate + ":00");
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		return binanceApiRestClient.getCandlestickBars(ticker,
				getInterval(interval), 500,
				startDateVal.getTime(), endDateVal.getTime());
	}

	@RequestMapping("/launchData/{ticker}")
	public List<PriceData> getDataFromLaunch(@PathVariable String ticker) {
		return priceDataDAO.findTopByTicker(ticker, 500);
	}

	/**
	 * Translate a string candlestick value into the required passed value.
	 * For example, 1m == CandlestickInterval.ONE_MINUTE. If there is no
	 * match or it is not valid, the default interval will be used
	 *
	 * @param i - the string defining the interval.
	 * @return
	 */
	private static CandlestickInterval getInterval(String i) {
		if (i == null) {
			return DEFAILT_CANDLESTICK;
		}
		CandlestickInterval val = candleIntervals.get(i);
		if (val == null) {
			return DEFAILT_CANDLESTICK;
		}
		return val;
	}

}
