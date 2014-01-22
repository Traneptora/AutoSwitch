package thebombzen.mods.autoswitch;

import java.util.Random;

public class NotSoRandom extends Random {

	private boolean useZero;

	public NotSoRandom(boolean useZero) {
		this.useZero = useZero;
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
	public int nextInt(int n) {
		if (useZero) {
			return 0;
		} else {
			return n - 1;
		}
	}

}
