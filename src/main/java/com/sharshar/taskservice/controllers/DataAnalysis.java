package com.sharshar.taskservice.controllers;

import com.sharshar.taskservice.algorithms.SignalSearcher;
import com.sharshar.taskservice.beans.PriceData;
import com.sharshar.taskservice.services.PriceDataPuller;
import com.sharshar.taskservice.utils.GenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

	public class TimeGraph {
		private Date t;
		private double y;

		public TimeGraph(Date t, double y) {
			this.t = t;
			this.y = y;
		}

		public Date getT() {
			return t;
		}

		public void setT(Date t) {
			this.t = t;
		}

		public double getY() {
			return y;
		}

		public void setY(double y) {
			this.y = y;
		}
	}

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

	@Autowired
	private PriceDataPuller priceDataPuller;

	private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

	@GetMapping("/data/history/{ticker}/first")
	public List<PriceDataLight> getPriceData(@PathVariable String ticker, short exchange) {
		List<PriceData> data = priceDataPuller.findByTicker(ticker, exchange);
		if (data != null) {
			return data.stream().map(d -> new PriceDataLight(d.getTicker(), d.getPrice(), d.getUpdateTime()))
					.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	@GetMapping("/data/history/{ticker}/{exchange}/firstText")
	public String getPriceDataText(@PathVariable String ticker, @PathVariable short exchange) {
		List<PriceData> data = priceDataPuller.findByTicker(ticker, exchange);
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

	@CrossOrigin
	@GetMapping("/data/history/{ticker}/{exchange}")
	public List<PriceDataLight> getPriceData(@PathVariable String ticker, @RequestParam String startDate,
											 @RequestParam String endDate, @PathVariable short exchange) throws Exception {
		Date startDateVal = GenUtils.parseDate(startDate, sdf);
		Date endDateVal = GenUtils.parseDate(endDate, sdf);
		if (startDateVal == null || endDateVal == null) {
			return new ArrayList<>();
		}
		List<PriceData> data = priceDataPuller.getPriceData(ticker, exchange, startDateVal, endDateVal);
		if (data != null) {
			data = data.stream().sorted(Comparator.comparing(PriceData::getUpdateTime)).collect(Collectors.toList());
			return data.stream().map(d -> new PriceDataLight(d.getTicker(), d.getPrice(), d.getUpdateTime()))
					.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	@CrossOrigin
	@GetMapping("/data/history/{ticker}/{exchange}/graph")
	public List<TimeGraph> getPriceDataForGraph(@PathVariable String ticker, @RequestParam String startDate,
											 @RequestParam String endDate, @PathVariable short exchange) throws Exception {
		Date startDateVal = GenUtils.parseDate(startDate, sdf);
		Date endDateVal = GenUtils.parseDate(endDate, sdf);
		if (startDateVal == null || endDateVal == null) {
			return new ArrayList<>();
		}
		List<PriceData> data = priceDataPuller.getPriceData(ticker, exchange, startDateVal, endDateVal);
		if (data != null) {
			data = data.stream().sorted(Comparator.comparing(PriceData::getUpdateTime)).collect(Collectors.toList());
			return data.stream().map(d -> new TimeGraph(d.getUpdateTime(), d.getPrice()))
					.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	@CrossOrigin
	@GetMapping("/data/history/{exchange}/ratio/graph")
	public List<TimeGraph> getPriceDataForGraph(@RequestParam String coin1, @RequestParam String coin2,
				@RequestParam String startDate, @RequestParam String endDate, @PathVariable short exchange) throws Exception {

		Date startDateVal = GenUtils.parseDate(startDate, sdf);
		Date endDateVal = GenUtils.parseDate(endDate, sdf);
		if (startDateVal == null || endDateVal == null) {
			System.out.println("Either " + startDate + " + or " + endDate + " is invalid");
			return new ArrayList<>();
		}
		List<PriceData> coin1Prices = priceDataPuller.getPriceData(coin1, exchange, startDateVal, endDateVal);
		List<PriceData> coin2Prices = priceDataPuller.getPriceData(coin2, exchange, startDateVal, endDateVal);
		if (coin2Prices == null || coin1Prices == null) {
			return new ArrayList<>();
		}
		if (coin2Prices.size() != coin1Prices.size()) {
			System.out.println("Mismatched data: " + coin1Prices.size() + "/" + coin2Prices.size());
		}
		coin2Prices = coin2Prices.stream().sorted(Comparator.comparing(PriceData::getUpdateTime)).collect(Collectors.toList());
		coin1Prices = coin1Prices.stream().sorted(Comparator.comparing(PriceData::getUpdateTime)).collect(Collectors.toList());
		List<TimeGraph> ratios = new ArrayList<>();
		for (int i=0; i<coin2Prices.size(); i++) {
			double priceCoin2 = coin2Prices.get(i).getPrice();
			if (i < coin1Prices.size()) {
				double priceCoin1 = coin1Prices.get(i).getPrice();
				ratios.add(new TimeGraph(coin2Prices.get(i).getUpdateTime(), priceCoin1 / priceCoin2));
			}
		}
		return ratios;
	}
}
