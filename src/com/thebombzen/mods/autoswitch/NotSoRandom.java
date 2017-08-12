package com.thebombzen.mods.autoswitch;

import java.util.Random;

/**
 * NotSoRandom provides an implementation of Random that can be used to
 * obtain determinate results when calculating random occurrences.
 * E.g. insert it into the World object, call a random function, then replace. 
 * @author thebombzen
 */
public class NotSoRandom extends Random {

	private static final long serialVersionUID = 7668644027932430864L;
	
	private boolean useZero;

	public NotSoRandom(boolean useZero) {
		this.useZero = useZero;
	}
	
	@Override
	public double nextDouble() {
		if (useZero){
			return 0.0D;
		} else {
			return 1.0D;
		}
	}

	@Override
	public float nextFloat() {
		if (useZero) {
			return 0.0F;
		} else {
			return 1.0F;
		}
	}
	
	@Override
	public double nextGaussian(){
		return nextDouble();
	}

	@Override
	public int nextInt(int n) {
		if (useZero) {
			return 0;
		} else {
			return n - 1;
		}
	}

}
