package com.sharshar.taskservice.controllers;

import com.sharshar.taskservice.repository.PriceDataDAO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created by lsharshar on 3/2/2018.
 */
@RestController
public class HealthController {
	@Resource
	private PriceDataDAO priceDataDAO;

	@RequestMapping("/dbConnect")
	public String dbConnect() {
		Date d = priceDataDAO.getLatestUpdate();
		if (d == null) {
			return "No data";
		}
		return "Last updated: " + d.toString();
	}
}
