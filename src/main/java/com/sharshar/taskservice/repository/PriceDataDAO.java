package com.sharshar.taskservice.repository;

import com.sharshar.taskservice.beans.PriceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Created by lsharshar on 3/6/2018.
 */
@Repository
public class PriceDataDAO {


	@Autowired
	JdbcTemplate jdbcTemplate;

	public Date getLatestUpdate() {
		return jdbcTemplate.queryForObject("SELECT MAX(update_time) FROM price_data", Date.class);
	}

	public List<String> getExistingTickers() {
		return jdbcTemplate.queryForList("SELECT DISTINCT ticker FROM price_data", String.class);
	}

	public List<PriceData> findTopByTicker(String ticker, int numberToGet) {
		return jdbcTemplate.query("SELECT * FROM price_data WHERE ticker=? LIMIT " + numberToGet, new Object[] { ticker },
				new PriceDataRowMapper());
	}

	public void save(PriceData data) {
		jdbcTemplate.update("INSERT INTO price_data (price, ticker, update_time) VALUES (?, ?, ?)",
				new Object[] { data.getPrice(), data.getTicker(), data.getUpdateTime()});
	}

	public List<PriceData> getTickerDateRange(String ticker, Date d1, Date d2) {
		List<PriceData> data = jdbcTemplate.query("SELECT * FROM price_data WHERE ticker = ? AND update_time >= ? AND update_time < ? LIMIT 500",
				new Object[] { ticker, d1, d2 }, new PriceDataRowMapper());
		return data;
	}
}
