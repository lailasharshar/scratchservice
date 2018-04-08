package com.sharshar.taskservice;

import com.sharshar.taskservice.algorithms.SignalSearcher;
import com.sharshar.taskservice.beans.PriceData;
import com.sharshar.taskservice.utils.ScratchConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TaskserviceApplicationTests {

	private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	private static final long MINUTE = 60 * 1000;

	@Test
	public void testSignalSearcher() {
		List<PriceData> priceDataList = new ArrayList<>();
		priceDataList.add(createPriceData("A", 1.0, "1/1/2018 10:00:00"));
		priceDataList.add(createPriceData("A", 1.2, "1/1/2018 10:01:01"));
		priceDataList.add(createPriceData("A", 1.4, "1/1/2018 10:02:02"));
		priceDataList.add(createPriceData("A", 1.6, "1/1/2018 10:03:03"));
		priceDataList.add(createPriceData("A", 1.8, "1/1/2018 10:04:04"));
		priceDataList.add(createPriceData("A", 2.0, "1/1/2018 10:05:05"));
		priceDataList.add(createPriceData("A", 2.2, "1/1/2018 10:06:06"));
		priceDataList.add(createPriceData("A", 2.4, "1/1/2018 10:07:07"));
		priceDataList.add(createPriceData("A", 2.6, "1/1/2018 10:08:08"));
		priceDataList.add(createPriceData("A", 2.8, "1/1/2018 10:09:09"));
		priceDataList.add(createPriceData("A", 3.0, "1/1/2018 10:10:10"));

		SignalSearcher searcher = new SignalSearcher();
		assertEquals(3, searcher.getDataWithinInterval(priceDataList, (3 * MINUTE)).size());
		assertEquals(4, searcher.getDataWithinInterval(priceDataList, (3 * MINUTE) + 5000).size());

		assertEquals(0, searcher.getSignals(
				priceDataList, 2, -1).size());
		List<SignalSearcher.SignalIdentifier> identifiers = searcher.getSignals(
				priceDataList, .5, -1);
		assertEquals(1, identifiers.size());
		SignalSearcher.SignalIdentifier id = identifiers.get(0);
		assertEquals(id.getStartDate().getTime(), priceDataList.get(0).getUpdateTime().getTime());
		assertEquals(id.getEndDate().getTime(), priceDataList.get(4).getUpdateTime().getTime());

		List<PriceData> priceDataList2 = new ArrayList<>();
		priceDataList2.add(createPriceData("A", 3.0, "1/1/2018 10:00:00"));
		priceDataList2.add(createPriceData("A", 2.2, "1/1/2018 10:01:01"));
		priceDataList2.add(createPriceData("A", 5.4, "1/1/2018 10:02:02"));
		priceDataList2.add(createPriceData("A", 1.6, "1/1/2018 10:03:03"));
		List<SignalSearcher.SignalIdentifier> identifiers2 = searcher.getSignals(
				priceDataList2, .5, -0.3);
		assertEquals(1, identifiers2.size());
		SignalSearcher.SignalIdentifier id2 = identifiers2.get(0);
		assertTrue(id2.getMultiplier() < 1);
	}

	private PriceData createPriceData(String ticker, double price, String date) {
		PriceData d = new PriceData();
		d.setTicker(ticker);
		d.setPrice(price);
		try {
			d.setUpdateTime(sdf.parse(date));
		} catch (Exception ex) {
			fail("Cannot parse date");
		}
		d.setExchange(ScratchConstants.BINANCE);
		return d;
	}
}
