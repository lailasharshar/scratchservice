package com.sharshar.taskservice.algorithms;

import com.sharshar.taskservice.beans.PriceData;
import com.sharshar.taskservice.beans.RepositoryDescriptor;
import com.sharshar.taskservice.services.GlobalRepositories;
import com.sharshar.taskservice.services.NotificationService;
import com.sharshar.taskservice.utils.ScratchConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Perform tracking tasks for new tickers on an exchange
 *
 * Created by lsharshar on 3/19/2018.
 */
@Service
@Scope("prototype")
public class NewTickerAlgorithm {

	private Logger logger = LogManager.getLogger();

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private GlobalRepositories globalRepositories;

	@Value( "${url}" )
	private String url;

	private List<String> newTickers;

	private short exchange;

	public NewTickerAlgorithm(short exchange, List<String> newTickers) {
		this.newTickers = newTickers;
		if (this.newTickers != null) {
			this.newTickers = newTickers.stream().distinct().collect(Collectors.toList());
		}
		this.exchange = exchange;
	}

	public void analyze() {
		if (newTickers.isEmpty() || newTickers.size() > 200) {
			return;
		}
		// If we are not stale, check to see if there is anything new
		// Right now, just notify me that there is a new one
		for (String newTicker : newTickers) {
			List<PriceData> allPrices = getPrices(newTicker);
			logger.info("New ticker for " + ScratchConstants.EXCHANGES[exchange] + ": " + newTicker);
			notifyMe(allPrices, newTicker);
		}
	}

	private String getNotificationString(List<PriceData> allPrices, String ticker) {
		StringBuilder sb = new StringBuilder();
		sb.append("Price data for : ").append(ticker);
		StringBuilder otherPriceData = new StringBuilder();
		String format = "%2.10f";
		for (PriceData pd : allPrices) {
			if (pd.getExchange() == exchange) {
				sb.append("Listed newly on: ")
						.append(ScratchConstants.EXCHANGES[exchange])
						.append(" at ")
						.append(String.format(format, pd.getPrice()))
						.append(" - ")
						.append(url)
						.append("/data/history/")
						.append(ticker)
						.append("/")
						.append(exchange)
						.append("/firstText\n")
						.append("\n\n");
			} else {
				otherPriceData.append(ScratchConstants.EXCHANGES[pd.getExchange()])
						.append(": ")
						.append(String.format(format, pd.getPrice()))
						.append(" - ")
						.append(url)
						.append("/data/history/")
						.append(ticker)
						.append("/")
						.append(exchange)
						.append("/firstText\n")
						.append("\n");
			}
		}
		sb.append(otherPriceData);
		return sb.toString();
	}

	private List<PriceData> getPrices(String ticker)  {
		List<PriceData> pd = new ArrayList<>();
		List<RepositoryDescriptor> trackers = globalRepositories.getTrackerList();
		for (RepositoryDescriptor d : trackers) {
			PriceData lastPull = Collections.max(d.getCache().getPriceData(ticker),
					Comparator.comparing(PriceData::getUpdateTime));
			if (lastPull != null) {
				pd.add(lastPull);
			}
		}
		return pd;
	}

	private void notifyMe(List<PriceData> allPrices, String ticker) {
		try {
			String msg = getNotificationString(allPrices, ticker);
			notificationService.notifyMe("NEW " + ScratchConstants.EXCHANGES[exchange] + " TICKER: "
							+ ticker, msg);
		} catch (Exception ex) {
			logger.error("Unable to notify me", ex);
		}
	}
}
