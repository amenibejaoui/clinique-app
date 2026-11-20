package com.sip.clm.util;

public final class MoneyUtil {

	public static final double EPS = 0.0005;

	private MoneyUtil() {
	}

	/** Arrondi a 3 decimales (millimes). */
	public static double round(double value) {
		return Math.round(value * 1000.0) / 1000.0;
	}
}
