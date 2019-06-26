package com.sharshar.taskservice.services;

import com.sharshar.taskservice.beans.PriceData;
import com.sharshar.taskservice.beans.PriceDataSql;
import com.sharshar.taskservice.repository.PriceDataES;
import com.sharshar.taskservice.repository.PriceDataSQLRepository;
import com.sharshar.taskservice.utils.ScratchConstants;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Used to determine the correct db mechanism to pull the data from.
 *
 * Created by lsharshar on 5/14/2018.
 */
@Service
public class PriceDataPuller {
	@Resource
	private PriceDataES priceDataEs;

	@Resource
	private PriceDataSQLRepository priceDataSQLRepository;

	public List<PriceData> getPriceData(String ticker, short exchange, Date startDate, Date endDate) throws Exception {
		if (ScratchConstants.EXCHANGES_SEARCHES[exchange] == ScratchConstants.ELASTIC_SEARCH) {
			return priceDataEs.findByTimeRange(ticker, startDate, endDate, exchange);
		}
		if (ScratchConstants.EXCHANGES_SEARCHES[exchange] == ScratchConstants.SQL_SEARCH) {
			List<PriceDataSql> data = priceDataSQLRepository.findByTickerAndUpdateTimeGreaterThanAndUpdateTimeLessThan(ticker, startDate, endDate);
			return convertToPriceData(data, exchange);
		}
		return new ArrayList<>();
	}

	public List<PriceData> findByTicker(String ticker, short exchange) {
		if (ScratchConstants.EXCHANGES_SEARCHES[exchange] == ScratchConstants.ELASTIC_SEARCH) {
			return priceDataEs.findByTicker(ticker, exchange);
		}
		if (ScratchConstants.EXCHANGES_SEARCHES[exchange] == ScratchConstants.SQL_SEARCH) {
			List<PriceDataSql> data = priceDataSQLRepository.findTop500ByTicker(ticker);
			return convertToPriceData(data, exchange);
		}
		return new ArrayList<>();
	}

	private static List<PriceData> convertToPriceData(List<PriceDataSql> data, short exchange) {
		if (data == null) {
			return new ArrayList<>();
		}
		return data.stream().map(
				d -> new PriceData()
						.setExchange(exchange).setPrice(d.getPrice()).setTicker(d.getTicker()).setUpdateTime(d.getUpdateTime()))
				.collect(Collectors.toList());
	}
}
