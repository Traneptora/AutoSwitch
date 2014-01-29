package thebombzen.mods.autoswitch;


/**
 * This class represents a set of values. It could be a universal set,
 * a one-item set, or a set based off a mask.
 * @author thebombzen
 */
public class ValueSet {
	
	
	private int data;
	private int mask;
	
	/**
	 * If true, this is meant to subtract rather than add.
	 */
	private boolean subtract;
	
	/**
	 * Construct a universal set
	 */
	public ValueSet(){
		this(0, 0, false);
	}
	
	/**
	 * Construct a set that only matches the specified value
	 * @param value the value
	 * @param subtract whether to subtract (true) or add (false) the value.
	 */
	public ValueSet(int value, boolean subtract){
		this(value, ~0, subtract);
	}
	
	/**
	 * Construct a set that matches all values whose mask matches the data
	 * @param data The data
	 * @param mask The mask to apply to potential values
	 * @param subtract whether to subtract (true) or add (false) the value.
	 */
	public ValueSet(int data, int mask, boolean subtract){
		this.data = data;
		this.mask = mask;
		this.subtract = subtract;
	}
	
	/**
	 * Returns true if this set contains the value
	 * @param value the value to check
	 */
	public boolean contains(int value){
		return (value & mask) == data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + data;
		result = prime * result + (subtract ? 1231 : 1237);
		result = prime * result + mask;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValueSet other = (ValueSet) obj;
		if (data != other.data)
			return false;
		if (subtract != other.subtract)
			return false;
		if (mask != other.mask)
			return false;
		return true;
	}

	/**
	 * Returns a String representation, as seen in the config.
	 */
	@Override
	public String toString() {
		return (subtract ? '-' : '+') + "0x" + Integer.toHexString(data) + (mask != ~0 ? ":0x" + Integer.toHexString(mask) : "");
	}
	
	/**
	 * Returns whether this set subtracts rather than adds values to the total.
	 */
	public boolean doesSubtract(){
		return subtract;
	}
	
}
