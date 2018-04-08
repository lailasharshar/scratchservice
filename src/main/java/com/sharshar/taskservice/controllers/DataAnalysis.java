package com.sharshar.taskservice.controllers;

import com.sharshar.taskservice.algorithms.SignalSearcher;
import com.sharshar.taskservice.beans.PriceData;
import com.sharshar.taskservice.repository.PriceDataES;
import com.sharshar.taskservice.utils.GenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Used to return data that can be analyzed. This is data from the database
 *
 * Created by lsharshar on 3/16/2018.
 */
@RestController
public class DataAnalysis {

	@Autowired
	SignalSearcher signalSearcher;

	public class PriceDataLight {
		private String ticker;
		private double price;
		private Date updateDate;

		private PriceDataLight(String ticker, double price, Date updateDate) {
			this.ticker = ticker;
			this.price = price;
			this.updateDate = updateDate;
		}

		public String getTicker() {
			return ticker;
		}

		public double getPrice() {
			return price;
		}

		public Date getUpdateDate() {
			return updateDate;
		}

	}

	@Resource
	private PriceDataES priceDataEs;

	private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

	@GetMapping("/data/history/{ticker}/first")
	public List<PriceDataLight> getPriceData(@PathVariable String ticker, short exchange) {
		List<PriceData> data = priceDataEs.findByTicker(ticker, exchange);
		if (data != null) {
			return data.stream().map(d -> new PriceDataLight(d.getTicker(), d.getPrice(), d.getUpdateTime()))
					.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	@GetMapping("/data/history/{ticker}/{exchange}/firstText")
	public String getPriceDataText(@PathVariable String ticker, @PathVariable short exchange) {
		List<PriceData> data = priceDataEs.findByTicker(ticker, exchange);
		StringBuilder result = new StringBuilder();
		result.append(ticker).append("\n\n");
		if (data != null) {
			for (PriceData pd : data) {
				result.append("   ").append(sdf2.format(pd.getUpdateTime()))
						.append(": ").append(String.format("%2.10f", pd.getPrice())).append("\n");
			}
			return result.toString();
		}
		return null;
	}

	@GetMapping("/data/history/{ticker}/{exchange}")
	public List<PriceDataLight> getPriceData(@PathVariable String ticker, @RequestParam String startDate,
										@RequestParam String endDate, @PathVariable short exchange) throws Exception {
		Date startDateVal = GenUtils.parseDate(startDate, sdf);
		Date endDateVal = GenUtils.parseDate(endDate, sdf);
		if (startDateVal == null || endDateVal == null) {
			return new ArrayList<>();
		}
		List<PriceData> data = priceDataEs.findByTimeRange(ticker, startDateVal, endDateVal, exchange);
		if (data != null) {
			data.stream().sorted(Comparator.comparing(PriceData::getUpdateTime)).collect(Collectors.toList());
			return data.stream().map(d -> new PriceDataLight(d.getTicker(), d.getPrice(), d.getUpdateTime()))
					.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}
}
