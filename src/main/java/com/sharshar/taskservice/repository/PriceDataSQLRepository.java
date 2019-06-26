package com.sharshar.taskservice.repository;

import com.sharshar.taskservice.beans.PriceDataSql;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by lsharshar on 5/14/2018.
 */
@Transactional
public interface PriceDataSQLRepository extends CrudRepository<PriceDataSql, Long> {
	public List<PriceDataSql> findByTickerAndUpdateTimeGreaterThanAndUpdateTimeLessThan(String ticker, Date d1, Date d2);
	public List<PriceDataSql> findTop500ByTicker(String ticker);
}
