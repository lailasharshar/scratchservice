package com.sharshar.taskservice.controllers;

import com.sharshar.taskservice.repository.PriceDataDAO;
import com.sharshar.taskservice.services.AnalyzePriceHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lsharshar on 1/21/2018.
 */
@RestController
public class PriceAnalysisController {
	@Resource
	private PriceDataDAO priceDataDAO;

	@Autowired
	private AnalyzePriceHistory analyzePriceHistory;

	@RequestMapping("/analysis/{ticker}")
	public AnalyzePriceHistory.Analysis getAnalysis(@PathVariable String ticker, @RequestParam String steps) {
		List<Pair<Double, Double>> pairs = parseSteps(Arrays.asList(steps.split(" ")));
		try {
			AnalyzePriceHistory.Analysis analysis = analyzePriceHistory.analyzeSellPrice(ticker, pairs);
			return analysis;
		} catch (Exception ex) {

			return null;
		}
	}

	public static List<Pair<Double, Double>> parseSteps(List<String> paramList) {
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
}
