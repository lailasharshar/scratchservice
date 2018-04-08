package com.sharshar.taskservice.controllers;

import com.sharshar.taskservice.algorithms.SignalSearcher;
import com.sharshar.taskservice.beans.PriceData;
import com.sharshar.taskservice.beans.RepositoryDescriptor;
import com.sharshar.taskservice.repository.PriceDataES;
import com.sharshar.taskservice.services.AnalyzePriceHistory;
import com.sharshar.taskservice.services.GlobalRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Controller to serve analysis of the data
 *
 * Created by lsharshar on 1/21/2018.
 */
@RestController
public class PriceAnalysisController {
	private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm");

	@Resource
	private PriceDataES priceDataEs;

	@Autowired
	private GlobalRepositories globalRepositories;

	@Autowired
	private AnalyzePriceHistory analyzePriceHistory;

	@RequestMapping("/analysis/{ticker}")
	public AnalyzePriceHistory.Analysis getAnalysis(@PathVariable String ticker, @RequestParam String steps, @RequestParam short exchange) {
		List<Pair<Double, Double>> pairs = parseSteps(Arrays.asList(steps.split(" ")));
		try {
			return analyzePriceHistory.analyzeSellPrice(ticker, pairs, exchange);
		} catch (Exception ex) {
			return null;
		}
	}

	private static List<Pair<Double, Double>> parseSteps(List<String> paramList) {
		List<Pair<Double, Double>> pairs = new ArrayList<>();
		// Must be in pairs
		if (paramList.size() % 2 != 0) {
			return pairs;
		}
		for (int i = 0; i < paramList.size(); i++) {
			Double percentage = Double.parseDouble(paramList.get(i));
			Double step = Double.parseDouble(paramList.get(i + 1));
			pairs.add(Pair.of(percentage, step));
			i++;
		}
		return pairs;
	}

	@RequestMapping("/signal/{exchange}/{ticker}")
	public List<SignalSearcher.SignalIdentifier> getSignals(@PathVariable String ticker, @PathVariable short exchange,
				@RequestParam String startDate, @RequestParam String endDate) throws Exception {
		System.out.println(startDate + " - " + endDate);
		SignalSearcher searcher = new SignalSearcher();
		List<PriceData> pd = priceDataEs.findByTimeRange(ticker, sdf.parse(startDate), sdf.parse(endDate), exchange);
		return searcher.getSignals(pd, 2, 0.2);
	}

	@RequestMapping("/signal/{exchange}")
	public List<SignalSearcher.SignalIdentifier> getSignals(@PathVariable short exchange,
				@RequestParam String startDate, @RequestParam String endDate, @RequestParam double up, @RequestParam double down) throws ParseException {
		SignalSearcher searcher = new SignalSearcher();
		System.out.println(startDate + " - " + endDate);
		long interval = 1000 * 60 * 5;

		Optional<RepositoryDescriptor> descriptor = globalRepositories.getTrackerList().stream()
				.filter(c -> c.getExchangeId() == exchange).findFirst();
		List<SignalSearcher.SignalIdentifier> identifiers = new ArrayList<>();
		if (descriptor.isPresent()) {
			List<String> tickers = descriptor.get().getCache().getTickers();
			for (String ticker : tickers) {
				System.out.println("Doing " + ticker);
				try {
					List<PriceData> pd = priceDataEs.findByTimeRange(ticker, sdf.parse(startDate), sdf.parse(endDate), exchange);
					List<List<PriceData>> splitList = searcher.splitUpData(pd, interval);
					for (List<PriceData> subList : splitList) {
						identifiers.addAll(searcher.getSignals(subList, up, down));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return identifiers;
	}
}
