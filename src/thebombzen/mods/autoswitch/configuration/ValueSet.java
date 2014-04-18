package thebombzen.mods.autoswitch.configuration;

import java.util.Scanner;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import thebombzen.mods.thebombzenapi.configuration.BooleanTester;
import thebombzen.mods.thebombzenapi.configuration.ConfigFormatException;


/**
 * This class represents a set of values. It could be a universal set,
 * a one-item set, or a set based off a mask.
 * @author thebombzen
 */
public class ValueSet implements BooleanTester<SingleValueIdentifier> {
	
	public static ValueSet parseValueSet(String s) throws ConfigFormatException {
		int first;
		int second;
		int third;
		boolean subtract;
		
		switch (s.charAt(0)){
		case '+':
			subtract = false;
			break;
		case '-':
			subtract = true;
			break;
		default:
				throw new ConfigFormatException("Value Set doesn't start with + or -: " + s);
		}
		
		Scanner scanner = new Scanner(s.substring(1));
		scanner.useDelimiter(":");
		
		boolean enchant = false;
		
		if (scanner.hasNext()){
			String num = scanner.next();
			if (Character.toUpperCase(num.charAt(0)) == 'E'){
				num = num.substring(1);
				enchant = true;
			}
			try {
				first = ThebombzenAPI.parseInteger(num);
			} catch (NumberFormatException nfe){
				scanner.close();
				throw new ConfigFormatException("Invalid number: " + num);
			}
			if (enchant && (first < 0 || first >= Enchantment.enchantmentsList.length || Enchantment.enchantmentsList[first] == null)){
				scanner.close();
				throw new ConfigFormatException("Invalid Enchantment ID: " + first);
			}
		} else {
			scanner.close();
			return new ValueSet(0, 0, subtract);
		}
		
		if (scanner.hasNext()){
			String num = scanner.next();
			try {
				second = ThebombzenAPI.parseInteger(num);
			} catch (NumberFormatException nfe){
				scanner.close();
				throw new ConfigFormatException("Invalid number: " + num);
			}
		} else {
			scanner.close();
			if (enchant){
				return new ValueSet(Enchantment.enchantmentsList[first], 1, Integer.MAX_VALUE, subtract);
			} else {
				return new ValueSet(first, Integer.MAX_VALUE, subtract);
			}
		}
		
		if (enchant){
			if (scanner.hasNext()){
				String num = scanner.next();
				scanner.close();
				try {
					third = ThebombzenAPI.parseInteger(num);
				} catch (NumberFormatException e){
					throw new ConfigFormatException("Invalid number: " + num);
				}
				return new ValueSet(Enchantment.enchantmentsList[first], second, third, subtract);
			} else {
				scanner.close();
				return new ValueSet(Enchantment.enchantmentsList[first], second, Integer.MAX_VALUE, subtract);
			}
		} else {
			scanner.close();
			return new ValueSet(first, second, subtract);
		}
	}
	
	private int data;
	private int mask;
	
	/**
	 * If true, this is meant to subtract rather than add.
	 */
	private boolean subtract;
	
	private int min;
	private int max;
	private Enchantment enchantment;
	
	/**
	 * Construct a universal set
	 */
	public ValueSet(){
		this(0, 0, false);
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
		this.enchantment = null;
	}
	
	public ValueSet(Enchantment enchantment, int min, int max, boolean subtract){
		this.subtract = subtract;
		this.min = min;
		this.max = max;
		this.enchantment = enchantment;
	}
	
	public boolean contains(SingleValueIdentifier id){
		if (id.isItem()){
			return contains(id.getItemStack());
		} else {
			return contains(id.getDamageValue());
		}
	}
	
	private boolean contains(int value){
		return (value & mask) == data;
	}
	
	/**
	 * Returns true if this set contains the value
	 * @param value the value to check
	 */
	private boolean contains(ItemStack stack){
		if (stack == null){
			return false;
		}
		if (enchantment != null){
			NBTTagList list = stack.getEnchantmentTagList();
			if (list == null){
				return false;
			}
			for (int i = 0; i < list.tagCount(); i++){
				short id = list.getCompoundTagAt(i).getShort("id");
				short lvl = list.getCompoundTagAt(i).getShort("lvl");
				if (id == enchantment.effectId){
					if (lvl >= min && lvl <= max){
						return true;
					} else {
						return false;
					}
				} else {
					continue;
				}
			}
			return false;
		} else {
			return (stack.getItemDamage() & mask) == data;
		}
	}

	/**
	 * Returns whether this set subtracts rather than adds values to the total.
	 */
	public boolean doesSubtract(){
		return subtract;
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
		if (enchantment == null) {
			if (other.enchantment != null)
				return false;
		} else if (!enchantment.equals(other.enchantment))
			return false;
		if (mask != other.mask)
			return false;
		if (max != other.max)
			return false;
		if (min != other.min)
			return false;
		if (subtract != other.subtract)
			return false;
		return true;
	}

	public int getData() {
		return data;
	}
	
	public int getMask() {
		return mask;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + data;
		result = prime * result
				+ ((enchantment == null) ? 0 : enchantment.hashCode());
		result = prime * result + mask;
		result = prime * result + max;
		result = prime * result + min;
		result = prime * result + (subtract ? 1231 : 1237);
		return result;
	}

	/**
	 * Returns a String representation, as seen in the config.
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(subtract ? '-' : '+');
		if (enchantment != null){
			b.append('E').append("0x").append(Integer.toHexString(enchantment.effectId)).append(":0x").append(Integer.toHexString(min)).append(":0x").append(Integer.toHexString(max));
		} else {
			b.append("0x").append(Integer.toHexString(data)).append(":0x").append(Integer.toHexString(mask));
		}
		return b.toString();
	}
	
}
