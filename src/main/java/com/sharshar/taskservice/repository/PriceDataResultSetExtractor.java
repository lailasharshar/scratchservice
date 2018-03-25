package com.sharshar.taskservice.repository;

import com.sharshar.taskservice.beans.PriceData;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by lsharshar on 3/6/2018.
 */
public class PriceDataResultSetExtractor implements ResultSetExtractor <PriceData> {

	@Override
	public PriceData extractData(ResultSet rs) throws SQLException {
		PriceData priceData = new PriceData();
		priceData.setPrice(rs.getDouble("price"));
		priceData.setTicker(rs.getString("ticker"));
		priceData.setUpdateTime(rs.getTimestamp("update_time"));
		priceData.setExchange(rs.getShort("exchange"));
		return priceData;
	}
}
