package com.sharshar.taskservice.repository;

import com.sharshar.taskservice.beans.PriceData;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by lsharshar on 3/6/2018.
 */
public class PriceDataRowMapper implements RowMapper<PriceData> {

	@Override
	public PriceData mapRow(ResultSet rs, int line) throws SQLException {
		PriceDataResultSetExtractor extractor = new PriceDataResultSetExtractor();
		return extractor.extractData(rs);
	}
}
