package com.sharshar.taskservice.beans;

import java.util.Date;

/**
 * Created by lsharshar on 3/6/2018.
 */
public class PriceData {
	public PriceData() {}

	public PriceData(int id, String ticker, Double price) {
		this.id = id;
		this.ticker = ticker;
		this.price = price;
	}

	private Integer id;

	private String ticker;

	private Double price;

	private Date updateTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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
}
