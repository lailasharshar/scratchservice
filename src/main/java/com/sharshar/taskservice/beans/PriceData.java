package com.sharshar.taskservice.beans;

import java.security.SecureRandom;
import java.util.Date;

/**
 * Bean for price data
 *
 * Created by lsharshar on 3/6/2018.
 */
public class PriceData {
	private Long _id;
	private String ticker;
	private Double price;
	private Date updateTime;
	private short exchange;

	public PriceData() {
		long unsignedValue;
		try {
			SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
			int val = prng.nextInt();
			unsignedValue = val;
			if (val < 0) {
				unsignedValue = val & 0xffffffffl;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			int val = (int) (Math.random() * Integer.MAX_VALUE);
			unsignedValue = val;
			if (val < 0) {
				unsignedValue = val & 0xffffffffl;
			}
		}
		_id = unsignedValue;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public short getExchange() {
		return exchange;
	}

	public void setExchange(short exchange) {
		this.exchange = exchange;
	}
}
