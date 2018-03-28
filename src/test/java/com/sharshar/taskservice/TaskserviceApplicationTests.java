package com.sharshar.taskservice;

import com.sharshar.taskservice.algorithms.NewTickerAlgorithm;
import com.sharshar.taskservice.beans.PriceData;
import com.sharshar.taskservice.services.ExchangeCache;
import com.sharshar.taskservice.utils.ScratchConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TaskserviceApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SSS");

	@Value( "${cacheSize}" )
	private int cacheSize;

	@Test
	public void testCache() {
		ExchangeCache cache = applicationContext.getBean(ExchangeCache.class);
		cache.setCacheSize(4);
		cache.setTickers(new ArrayList<>(Arrays.asList(new String[] {"A", "B", "C"})));
		assertEquals(3, cache.getTickers().size());
		assertEquals(0, cache.getPriceData("A").size());
		assertEquals(0, cache.getPriceData("Z").size());
		cache.addPriceData(new ArrayList<>(Arrays.asList(
				new PriceData[] { createPriceData("A", 1.1),
						createPriceData("A", 1.2),
						createPriceData("B", 9.1) }
		)));
		assertEquals(3, cache.getTickers().size());
		assertEquals(2, cache.getPriceData("A").size());
		assertEquals(1, cache.getPriceData("B").size());

		// Sleep for a bit so the time on trackers won't be the same
		try {
			Thread.sleep(700);
		} catch (Exception ex) { ex.printStackTrace(); }

		PriceData latest = createPriceData("C", 5.5);
		List<PriceData> data = new ArrayList<>(Arrays.asList(
				new PriceData[] {
						createPriceData("A", 3.2),
						createPriceData("Z", 3.2),
						latest}
		));

		cache.addPriceData(data);
		assertEquals(4, cache.getTickers().size());
		assertEquals(3, cache.getPriceData("A").size());
		assertEquals(1, cache.getPriceData("B").size());
		assertEquals(1, cache.getPriceData("Z").size());

		System.out.println(cache.getPriceCache("A"));
		System.out.println(cache.getPriceCache("B"));
		System.out.println(cache.getPriceCache("C"));
		System.out.println(cache.getPriceCache("Z"));
		System.out.println(cache.getPriceCache("NOTDEF"));

		Date d = cache.getLatestUpdate();
		System.out.println("Latest: " + sdf.format(d));
		assertEquals(latest.getUpdateTime().getTime(), d.getTime());

		cache.addTicker("NEWONE");
		assertEquals(5, cache.getTickers().size());
		cache.addTicker("NEWONE");
		assertEquals(5, cache.getTickers().size());
		cache.addAllTickers(Arrays.asList("Z", "K"));
		assertEquals(6, cache.getTickers().size());
		assertTrue(cache.tickerExists("Z"));
		assertFalse(cache.tickerExists("BOGUS"));

		cache.clear();
		assertEquals(0, cache.getTickers().size());
		assertEquals(0, cache.getPriceData("A").size());
	}

	private PriceData createPriceData(String ticker, Double price) {
		PriceData pd = new PriceData();
		pd.setTicker(ticker);
		pd.setUpdateTime(new Date(System.currentTimeMillis()));
		pd.setExchange(ScratchConstants.BINANCE);
		pd.setPrice(price);
		return pd;
	}

	@Test
	public void testNewTickerAlgorithm() {
		NewTickerAlgorithm newTickerAlgorithm = applicationContext.getBean(
				NewTickerAlgorithm.class, ScratchConstants.BINANCE,
				new ArrayList<>(Arrays.asList(new String[]{"DDDDD", "DDDDD", "EEEEE"})));
		Date d = newTickerAlgorithm.getDateForAlert();
		long timeFromNow = (d.getTime() - new Date().getTime())/1000;
		System.out.println("Cache size: " + cacheSize);
		System.out.println("Interval Time: " +  (ScratchConstants.PULL_INTERVAL/1000) + " sec.");
		System.out.println("Date for alert: " + timeFromNow + " sec, " + (timeFromNow/60) + " min");
		System.out.println("Current Date:   " + new Date());
		System.out.println("Date for alert: " + d);
	}

	@Test
	public void testDataDump() {

	}
}
