package com.sharshar.taskservice.services;

import com.sharshar.taskservice.utils.ScratchException;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Used to analyze price data
 * Created by lsharshar on 1/20/2018.
 */
public class PriceStepper {
	private List<Pair<Double, Double>> pairs;
	private double purchasePrice = 0.0;
	private double rollingMax = 0.0;

	public PriceStepper(List<Pair<Double, Double>> pairs) {
		if (pairs == null) {
			return;
		}
		// Sort them in order of multiple of original price
		this.pairs = pairs.stream().sorted((e1, e2) -> Double.compare(e1.getSecond(),
				e2.getSecond())).collect(Collectors.toList());
	}

	public void setPurchasePrice(double purchasePrice) {
		this.purchasePrice = purchasePrice;
		// This should be considered the first rolling max
		if (this.rollingMax < this.purchasePrice) {
			this.rollingMax = purchasePrice;
		}
	}

	/**
	 * Determines, based on the rolling max and the step we're currently in if we should sell
	 * the currency
	 *
	 * @param currentPrice - the current price to compare against the max
	 * @return true if we've dropped below the percentage threshold for the relevant step
	 * @throws ScratchException if there is some important data not defined (like the steps or purchase price)
	 */
	boolean shouldSell(double currentPrice) throws ScratchException {
		// Throw exception if the purchase was never defined
		if (purchasePrice == 0) {
			throw new ScratchException("Purchase price is not specified");
		}

		// If we don't have any steps defined, throw exception
		if (pairs == null) {
			throw new ScratchException("No price steps specified");
		}

		// If the current price is over the rolling max, set it to the current price and return as false;
		if (currentPrice > rollingMax) {
			rollingMax = currentPrice;
			return false;
		}
		// Determine the multiple of the original price
		double multiple = currentPrice / purchasePrice;

		// Determine the correct step
		double percentageToCheck = getAppropriatePercent(multiple);

		// Determine the amount from the top we can go down (convert to value instead of percent)
		double appliedPerc = rollingMax * (percentageToCheck/100.0);

		// Determine the minimum the currentPrice should be before we should sell
		double minPriceToHold = rollingMax - appliedPerc;

		// If the current price is less than the minimum price, sell
		if (currentPrice < minPriceToHold) {
			return true;
		}

		// It's down, but not by the percentage required to bail
		return false;
	}

	/**
	 * Determine the appropriate step pair. If we are below the first
	 * multiple value, return the first one.
	 *
	 * @param multiple - the multiple of the current price compared to the original price.
	 *                 For example, if the original price was 10 and it's at 20, the multiple is 2.0.
	 *                 If the current price is 9, the multiple is 0.9.
	 * @return - the appropiate percentage drop to look for
	 */
	private Double getAppropriatePercent(double multiple) {
		Pair<Double, Double> previousPair = pairs.get(0);
		for (Pair<Double, Double> pair : pairs) {
			// Iterate until we find the one with a value larger
			// than the multiple, then return the previous one. We're
			// assuming the pairs are sorted incrementally by the multiple
			if (pair.getSecond() < multiple) {
				previousPair = pair;
			} else {
				// We've find the step that's appropriate, bail
				break;
			}
		}
		return previousPair.getFirst();
	}

	public List<Pair<Double, Double>> getSteps() {
		return pairs;
	}

	public double getPurchasePrice() {
		return purchasePrice;
	}
}
