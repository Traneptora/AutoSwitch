package thebombzen.mods.autoswitch.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import thebombzen.mods.thebombzenapi.configuration.BooleanTester;
import thebombzen.mods.thebombzenapi.configuration.ConfigFormatException;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Represents a block or item with the given String namespace, name, and damage values.
 * @author thebombzen
 */
public class SingleBlockItemIdentifier implements BooleanTester<SingleValueIdentifier> {

	public static final int ALL = 0;
	public static final int NAME = 1;
	public static final int CLASS = 2;
	public static final int MATERIAL = 3;
	
	/**
	 * Parse an identifier from a string.
	 */
	public static SingleBlockItemIdentifier parseSingleBlockItemIdentifier(String info) throws ConfigFormatException {
		if (info.length() == 0){
			throw new ConfigFormatException();
		}
		char type = info.charAt(0);
		int superNum = 0;
		if (type == '@' || type == '$'){
			info = info.substring(1);
		} else if (type == '['){
			int lastIndex = info.indexOf(']');
			if (lastIndex == -1){
				throw new ConfigFormatException();
			}
			String superNumS = info.substring(1, lastIndex);
			try {
				superNum = ThebombzenAPI.parseInteger(superNumS);
			} catch (NumberFormatException e){
				throw new ConfigFormatException(e);
			}
			info = info.substring(lastIndex + 1);
		}
		
		boolean all = info.startsWith("+") || info.startsWith("-");
		
		if (all){
			info = "false_prefix" + info;
		}
		
		Scanner scanner = new Scanner(info);
		scanner.useDelimiter("(?<=[^\\+-])(?=[\\+-])");
		
		if (!scanner.hasNext()){
			scanner.close();
			return new SingleBlockItemIdentifier(SingleBlockItemIdentifier.ALL);
		}
		
		String fullname = scanner.next();
		String modid;
		String name;
		
		//System.out.println(fullname);
		
		if (all){
			modid = "";
			name = "";
		} else {
			int index = fullname.indexOf(':');
			if (index < 0){
				modid = "minecraft";
				name = fullname;
			} else {
				modid = fullname.substring(0, index);
				name = fullname.substring(index + 1);
			}
		}

		List<ValueSet> valueSets = new ArrayList<ValueSet>();
		
		while (scanner.hasNext()){
			String s = scanner.next();
			ValueSet valueSet = ValueSet.parseValueSet(s);
			valueSets.add(valueSet);
		}
		
		scanner.close();
		
		ValueSet[] sets = valueSets.toArray(new ValueSet[valueSets.size()]);
		
		if (all){
			return new SingleBlockItemIdentifier(SingleBlockItemIdentifier.ALL, sets);
		}
		
		switch(type){
		case '@':
		case '[':
			return new SingleBlockItemIdentifier(SingleBlockItemIdentifier.CLASS, modid, name, superNum, sets);
		case '$':
			return new SingleBlockItemIdentifier(SingleBlockItemIdentifier.MATERIAL, modid, name, 0, sets);
		default:
			return new SingleBlockItemIdentifier(SingleBlockItemIdentifier.NAME, modid, name, 0, sets);
		}
	}
	private String modid;
	private String name;
	private ValueSet[] damageValues;

	private int type;
	private int superNum;
	
	/**
	 * Construct an identifier with the given String namespace, name, and damage value.
	 * @param namespace The String namespace such as "minecraft"
	 * @param name The String name such as "log"
	 * @param damageValue The Damage value
	 */
	public SingleBlockItemIdentifier(int type, String namespace, String name, int damageValue){
		this(type, namespace, name, 0, new ValueSet(damageValue, false));
	}
	
	/**
	 * Construct an identifier with the given String namespace, name, and damage values.
	 * @param namespace The String namespace such as "minecraft"
	 * @param name The String name such as "log"
	 * @param damageValues The Damage values
	 */
	public SingleBlockItemIdentifier(int type, String namespace, String name, int superNum, ValueSet... damageValues){
		
		if (type < 0 || type > 3){
			throw new IllegalArgumentException();
		}
		this.superNum = superNum;
		this.type = type;
		this.modid = namespace;
		this.name = name;
		if (damageValues.length == 0 || damageValues.length > 0 && damageValues[0].doesSubtract()){
			ValueSet[] temp = new ValueSet[damageValues.length + 1];
			System.arraycopy(damageValues, 0, temp, 1, damageValues.length);
			temp[0] = new ValueSet();
			damageValues = temp;
		}
		this.damageValues = damageValues;
	}
	
	/**
	 * Construct a BlockItemIdentifier that matches the damages values on any item/block.
	 * @param damageValues
	 */
	public SingleBlockItemIdentifier(int type, ValueSet... damageValues){
		this(type, "", "", 0, damageValues);
	}

	/**
	 * Gets whether this identifier refers to the given name and damage value.
	 * @param name The String name of this block/item
	 * @param damageValue The damage value of this block/item
	 */
	@Override
	public boolean contains(SingleValueIdentifier identifier){
		String modid = identifier.getModid();
		String name = identifier.getName();
		int damageValue = identifier.getDamageValue();
		switch (type){
		case ALL:
			break;
		case NAME:
			if (!getName().equals(name) || !getModId().equals(modid)){
				return false;
			}
			break;
		case CLASS:
			Block block = GameRegistry.findBlock(modid, name);
			if (block != null && getBlock() != null){
				Class<?> clazz = getBlock().getClass();
				for (int i = 0; i < superNum; i++){
					if (clazz.getSuperclass() != null){
						clazz = clazz.getSuperclass();
					} else {
						break;
					}
				}
				if (!clazz.isAssignableFrom(block.getClass())){
					return false;
				}
				break;
			}
			Item item = GameRegistry.findItem(modid, name);
			if (item != null && getItem() != null){
				Class<?> clazz = getItem().getClass();
				for (int i = 0; i < superNum; i++){
					if (clazz.getSuperclass() != null){
						clazz = clazz.getSuperclass();
					} else {
						break;
					}
				}
				if (!clazz.isAssignableFrom(item.getClass())){
					return false;
				}
			} else {
				return false;
			}
			break;
		case MATERIAL:
			block = GameRegistry.findBlock(modid, name);
			if (block == null || getBlock() == null){
				return false;
			}
			if (!getBlock().getMaterial().equals(block.getMaterial())){
				return false;
			}
			break;
		}
		for (int i = damageValues.length - 1; i >= 0; i--){
			int testDamageValue = damageValue;
			if (damageValues[i].getMask() < 0){
				testDamageValue = GameRegistry.findItem(modid, name).getMaxDamage() - damageValue;
			}
			if (damageValues[i].contains(testDamageValue)){
				return !damageValues[i].doesSubtract();
			}
		}
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SingleBlockItemIdentifier other = (SingleBlockItemIdentifier) obj;
		if (!Arrays.equals(damageValues, other.damageValues))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (modid == null) {
			if (other.modid != null)
				return false;
		} else if (!modid.equals(other.modid))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	
	/**
	 * Gets the block associated with this identifier.
	 * null if this is not a block.
	 */
	public Block getBlock() {
		return GameRegistry.findBlock(modid, name);
	}

	/**
	 * Return the array of ValueSets
	 */
	public ValueSet[] getDamageValues() {
		return damageValues;
	}

	/**
	 * Gets the item associated with this identifier. If it's a block it
	 * will return the corresponding ItemBlock.
	 */
	public Item getItem() {
		return GameRegistry.findItem(modid, name);
	}

	/**
	 * Gets the String namespace of this item/block
	 * @return
	 */
	public String getModId(){
		return modid;
	}
	
	/**
	 * Gets the String name of this item/block
	 */
	public String getName(){
		return name;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(damageValues);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((modid == null) ? 0 : modid.hashCode());
		result = prime * result + type;
		return result;
	}
	
	/**
	 * Determines whether this IDMetadataPair is a valid (non-null) block. Air
	 * is not a valid block.
	 * 
	 * @return Whether this is a valid Block object.
	 */
	public boolean isBlock() {
		return getBlock() != null;
	}

	/**
	 * Determines whether this IDMetadataPair is a valid (non-null) item. All
	 * valid registered blocks are also valid items, see
	 * {net.minecraft.item.ItemBlock}
	 * 
	 * @return Whether this is a valid Item object.
	 */
	public boolean isItem() {
		return getItem() != null;
	}

	/**
	 * Returns a String representation, as seen in the config.
	 */
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		switch (type){
		case ALL:
			builder.append('@');
			break;
		case CLASS:
			builder.append("[").append(superNum).append("]");
			break;
		case MATERIAL:
			builder.append('&');
			break;
		}
		if (modid.length() != 0){
			builder.append(modid).append(":");
		}
		builder.append(name);
		for (int i = 0; i < damageValues.length; i++){
			builder.append(damageValues[i]);
		}
		return builder.toString();
	}

}
