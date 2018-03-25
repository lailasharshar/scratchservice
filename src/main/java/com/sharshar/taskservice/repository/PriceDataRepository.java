package com.sharshar.taskservice.repository;

import com.sharshar.taskservice.beans.PriceData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.Date;

/**
 * Created by lsharshar on 3/24/2018.
 */
public interface PriceDataRepository extends ElasticsearchRepository<PriceData, Long> {
	Page<PriceData> findTop500ByTickerAndExchangeOrderByUpdateTimeAsc(String ticker, int exchange, Pageable pageable);
	Page<PriceData> findByTickerAndExchangeAndUpdateTimeBetweenOrderByUpdateTime(String ticker, int exchange, Date d1, Date d2, Pageable pageable);
}
