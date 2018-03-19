package com.sharshar.taskservice.controllers;

import com.sharshar.taskservice.beans.PriceData;
import com.sharshar.taskservice.controllers.utils.GenUtils;
import com.sharshar.taskservice.repository.PriceDataDAO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lsharshar on 3/16/2018.
 */
@RestController
public class DataAnalysis {
	public class PriceDataLight {
		private String ticker;
		private double price;
		private Date updateDate;

		public PriceDataLight(String ticker, double price, Date updateDate) {
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
	private PriceDataDAO priceDataDAO;

	private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

	@GetMapping("/data/history/{ticker}/first")
	public List<PriceDataLight> getPriceData(@PathVariable String ticker) {
		List<PriceData> data = priceDataDAO.findTopByTicker(ticker, 100);
		if (data != null) {
			List<PriceDataLight> dataLight = data.stream()
					.map(d -> new PriceDataLight(d.getTicker(), d.getPrice(), d.getUpdateTime()))
					.collect(Collectors.toList());
			return dataLight;
		}
		return null;
	}

	@GetMapping("/data/history/{ticker}/firstText")
	public String getPriceDataText(@PathVariable String ticker) {
		List<PriceData> data = priceDataDAO.findTopByTicker(ticker, 100);
		String result = ticker + "\n\n";
		if (data != null) {
			for (PriceData pd : data) {
				result += "   " + sdf2.format(pd.getUpdateTime()) + ": " + String.format("%2.10f", pd.getPrice()) + "\n";
			}
			return result;
		}
		return null;
	}

	@GetMapping("/data/history/{ticker}")
	public List<PriceDataLight> getPriceData(@PathVariable String ticker, @RequestParam String startDate,
										@RequestParam String endDate) {
		Date startDateVal = GenUtils.parseDate(startDate, sdf);
		Date endDateVal = GenUtils.parseDate(endDate, sdf);
		if (startDateVal == null || endDateVal == null) {
			return null;
		}
		List<PriceData> data = priceDataDAO.getTickerDateRange(ticker, startDateVal, endDateVal);
		if (data != null) {
			List<PriceDataLight> dataLight = data.stream()
					.map(d -> new PriceDataLight(d.getTicker(), d.getPrice(), d.getUpdateTime()))
					.collect(Collectors.toList());
			return dataLight;
		}
		return null;
	}
}
