package thebombzen.mods.autoswitch.configuration;

import java.util.Iterator;
import java.util.Scanner;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import thebombzen.mods.autoswitch.AutoSwitch;
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
		String firsts = null;
		int firstn = 0;
		String seconds = null;
		int secondn = 0;
		int thirdn = 0;
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
		boolean block = false;
		
		if (scanner.hasNext()){
			String num = scanner.next();
			if (Character.toUpperCase(num.charAt(0)) == 'E'){
				num = num.substring(1);
				enchant = true;
			}
			try {
				firstn = ThebombzenAPI.parseInteger(num);
			} catch (NumberFormatException nfe){
				block = true;
				if (enchant){
					num = 'E' + num;
					enchant = false;
				}
				firsts = num;
			}
			if (enchant && Enchantment.getEnchantmentByID(firstn) == null){
				scanner.close();
				throw new ConfigFormatException("Invalid Enchantment ID: " + firstn);
			}
		} else {
			scanner.close();
			return new ValueSet();
		}
		
		if (scanner.hasNext()){
			String num = scanner.next();
			if (block){
				seconds = num;
			} else {
				try {
					secondn = ThebombzenAPI.parseInteger(num);
				} catch (NumberFormatException nfe){
					scanner.close();
					throw new ConfigFormatException("Invalid number: " + num);
				}
			}
		} else {
			scanner.close();
			if (block){
				throw new ConfigFormatException("No value for property: " + firsts);
			}
			if (enchant){
				return new ValueSet(Enchantment.getEnchantmentByID(firstn), 1, Integer.MAX_VALUE, subtract);
			} else {
				return new ValueSet(firstn, Integer.MAX_VALUE, subtract);
			}
		}
		
		if (enchant){
			if (scanner.hasNext()){
				String num = scanner.next();
				scanner.close();
				try {
					thirdn = ThebombzenAPI.parseInteger(num);
				} catch (NumberFormatException e){
					throw new ConfigFormatException("Invalid number: " + num);
				}
				return new ValueSet(Enchantment.getEnchantmentByID(firstn), secondn, thirdn, subtract);
			} else {
				scanner.close();
				return new ValueSet(Enchantment.getEnchantmentByID(firstn), secondn, Integer.MAX_VALUE, subtract);
			}
		} else {
			scanner.close();
			if (block){
				return new ValueSet(firsts, seconds, subtract);
			} else {
				return new ValueSet(firstn, secondn, subtract);
			}
		}
	}
	
	/**
	 * If true, this is meant to subtract rather than add.
	 */
	private final boolean subtract;
	private final int type;
	
	private int data = -1;
	private int mask = -1;
	
	private String state = null;
	private String value = null;
	
	
	private int min = -1;
	private int max = -1;
	private Enchantment enchantment = null;
	
	public static final int UNIVERSAL = 0;
	public static final int ITEM_DAMAGE = 1;
	public static final int ITEM_ENCHANTMENT = 2;
	public static final int BLOCK_STATE = 3;
	
	/**
	 * Construct a universal set
	 */
	public ValueSet(){
		this.type = ValueSet.UNIVERSAL;
		this.subtract = false;
	}
	
	/**
	 * Construct a set using enchantments
	 * @param enchantment The enchantment for which to check
	 * @param min The minimum accepted power of the enchantment
	 * @param max The maximum accepted power of the enchantment
	 * @param subtract whether to subtract (true) or add (false) the value.
	 */
	public ValueSet(Enchantment enchantment, int min, int max, boolean subtract){
		this.type = ValueSet.ITEM_ENCHANTMENT;
		this.subtract = subtract;
		this.min = min;
		this.max = max;
		this.enchantment = enchantment;
	}
	
	/**
	 * Construct a set using block states
	 * @param state The name of the block state, e.g. axis (for axis:x)
	 * @param value The value of the block state, e.g. x (for axis:x)
	 * @param subtract whether to subtract (true) or add (false) the value.
	 */
	public ValueSet(String state, String value, boolean subtract){
		this.type = ValueSet.BLOCK_STATE;
		this.subtract = subtract;
		this.state = state;
		this.value = value;
	}
	
	/**
	 * Construct a set that matches all values whose mask matches the data
	 * @param data The data
	 * @param mask The mask to apply to potential values
	 * @param subtract whether to subtract (true) or add (false) the value.
	 */
	public ValueSet(int data, int mask, boolean subtract){
		this.type = ValueSet.ITEM_DAMAGE;
		this.data = data;
		this.mask = mask;
		this.subtract = subtract;
	}
	
	private boolean contains(IBlockState state){
		if (state == null){
			return false;
		}
		ImmutableMap<IProperty<?>, Comparable<?>> properties = state.getProperties();
		Iterator<IProperty<?>> iterator = properties.keySet().iterator();
		while (iterator.hasNext()){
			IProperty<?> prop = iterator.next();
			if (prop.getName().equalsIgnoreCase(this.state) && properties.get(prop).toString().equalsIgnoreCase(this.value)){
				return true;
			}
			
		}
		return false;
	}
	
	/**
	 * Returns true if this set contains the value
	 * @param value the value to check
	 */
	private boolean contains(ItemStack stack){
		if (stack == null){
			return false;
		}
		if (type == ValueSet.ITEM_ENCHANTMENT){
			NBTTagList list = stack.getEnchantmentTagList();
			if (list == null){
				return false;
			}
			for (int i = 0; i < list.tagCount(); i++){
				short id = list.getCompoundTagAt(i).getShort("id");
				short lvl = list.getCompoundTagAt(i).getShort("lvl");
				if (id == Enchantment.getEnchantmentID(enchantment)){
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
	
	@Override
	public boolean contains(SingleValueIdentifier id){
		if (type == ValueSet.UNIVERSAL){
			return true;
		}
		if (id.isItem()){
			if (type == ValueSet.BLOCK_STATE){
				AutoSwitch.instance.throwException("Trying to check the block state of an item!", new UnsupportedOperationException(), false);
				return false;
			}
			return contains(id.getItemStack());
		} else {
			if (type != ValueSet.BLOCK_STATE){
				AutoSwitch.instance.throwException("Trying to check the item state of a block!", new UnsupportedOperationException(), false);
				return false;
			}
			return contains(id.getBlockState());
		}
	}

	/**
	 * Returns whether this set subtracts rather than adds values to the total.
	 */
	public boolean doesSubtract(){
		return subtract;
	}

	public int getData() {
		return data;
	}
	
	public int getMask() {
		return mask;
	}

	/**
	 * Returns a String representation, as seen in the config.
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(subtract ? '-' : '+');
		switch (type){
		case UNIVERSAL:
			b.append('U');
			break;
		case ITEM_ENCHANTMENT:
			b.append('E').append("0x").append(Integer.toHexString(Enchantment.getEnchantmentID(enchantment))).append(":0x").append(Integer.toHexString(min)).append(":0x").append(Integer.toHexString(max));
			break;
		case ITEM_DAMAGE:
			b.append("0x").append(Integer.toHexString(data)).append(":0x").append(Integer.toHexString(mask));
			break;
		case BLOCK_STATE:
			b.append(state).append(':').append(value);
			break;
		}
		return b.toString();
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
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + (subtract ? 1231 : 1237);
		result = prime * result + type;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (subtract != other.subtract)
			return false;
		if (type != other.type)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
}
