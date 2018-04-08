package com.sharshar.taskservice.repository;

import com.sharshar.taskservice.beans.PriceData;
import com.sharshar.taskservice.configuration.EsConfig;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lsharshar on 3/24/2018.
 */
@Service
public class PriceDataES {

	@Autowired
	private PriceDataRepository priceDataRepository;

	@Autowired
	private EsConfig esConfig;

	public PriceData save(PriceData priceData) {
		return priceDataRepository.save(priceData);
	}

	public Iterable<PriceData> save(List<PriceData> priceData) {
		return priceDataRepository.saveAll(priceData);
	}

	public void delete(PriceData priceData) {
		priceDataRepository.delete(priceData);
	}

	public List<PriceData> findByTicker(String ticker, int exchange) {
		Page<PriceData> data = priceDataRepository.
				findTop500ByTickerAndExchangeOrderByUpdateTimeAsc(ticker, exchange, PageRequest.of(0, 500));
		if (data == null) {
			return new ArrayList<>();
		}

		return data.getContent();
	}

	public List<PriceData> findByTimeRange(String ticker, Date startDate, Date endDate, int exchange) throws Exception{
		DateTime dt1 = new DateTime(startDate);
		DateTime dt2 = new DateTime(endDate);
		Page<PriceData> data = priceDataRepository.findByUpdateTimeBetweenAndExchangeAndTicker(
				dt1, dt2, exchange, ticker, PageRequest.of(0, 10000));
		return data.getContent();
	}
}
