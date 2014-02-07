package thebombzen.mods.autoswitch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

/**
 * Represents a block or item with the given String namespace, name, and damage values.
 * @author thebombzen
 */
public class BlockItemIdentifier {

	public static final int ALL = 0;
	public static final int NAME = 1;
	public static final int CLASS = 2;
	public static final int MATERIAL = 3;
	
	/**
	 * Parse and identifier from a string.
	 */
	public static BlockItemIdentifier parseBlockItemIdentifier(String info){
		Scanner scanner = new Scanner(info);
		scanner.useDelimiter("(?<=[^\\+-])(?=[\\+-])");
		char type = info.charAt(0);
		
		if (!scanner.hasNext()){
			scanner.close();
			return new BlockItemIdentifier(BlockItemIdentifier.ALL);
		}
		
		String fullname = scanner.next();
		String namespace;
		String name;
		
		if (fullname.startsWith("@") || fullname.startsWith("&")){
			fullname = fullname.substring(1);
		}
		
		if (fullname.length() == 0){
			namespace = "";
			name = "";
		} else {
			int index = fullname.indexOf(':');
			if (index < 0){
				namespace = "minecraft";
				name = fullname;
			} else {
				namespace = fullname.substring(0, index);
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
		
		if (fullname.length() == 0){
			return new BlockItemIdentifier(BlockItemIdentifier.ALL, sets);
		}
		
		switch(type){
		case '@':
			return new BlockItemIdentifier(BlockItemIdentifier.CLASS, namespace, name, sets);
		case '&':
			return new BlockItemIdentifier(BlockItemIdentifier.MATERIAL, namespace, name, sets);
		default:
			return new BlockItemIdentifier(BlockItemIdentifier.NAME, namespace, name, sets);
		}
	}
	private String namespace;
	private String name;
	private ValueSet[] damageValues;

	private int type;
	
	/**
	 * Construct an identifier with the given String namespace, name, and damage value.
	 * @param namespace The String namespace such as "minecraft"
	 * @param name The String name such as "log"
	 * @param damageValue The Damage value
	 */
	public BlockItemIdentifier(int type, String namespace, String name, int damageValue){
		this(type, namespace, name, new ValueSet(damageValue, false));
	}
	
	/**
	 * Construct an identifier with the given String namespace, name, and damage values.
	 * @param namespace The String namespace such as "minecraft"
	 * @param name The String name such as "log"
	 * @param damageValues The Damage values
	 */
	public BlockItemIdentifier(int type, String namespace, String name, ValueSet... damageValues){
		if (type < 0 || type > 3){
			throw new IllegalArgumentException();
		}
		this.type = type;
		this.namespace = namespace;
		this.name = name;
		if (damageValues.length == 0){
			damageValues = new ValueSet[] { new ValueSet() } ;
		}
		this.damageValues = damageValues;
	}
	
	/**
	 * Construct a BlockItemIdentifier that matches the damages values on any item/block.
	 * @param damageValues
	 */
	public BlockItemIdentifier(int type, ValueSet... damageValues){
		this(type, "", "", damageValues);
	}

	public boolean contains(Block block, int metadata){
		UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(block);
		return contains(id.modId, id.name, metadata);
	}

	public boolean contains(ItemStack itemStack){
		if (itemStack == null){
			return false;
		}
		UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(itemStack.getItem());
		return contains(id.modId, id.name, itemStack.getItemDamage());
	}

	/**
	 * Gets whether this identifier refers to the given name and damage value.
	 * @param name The String name of this block/item
	 * @param damageValue The damage value of this block/item
	 */
	public boolean contains(String namespace, String name, int damageValue){
		
		switch (type){
		case ALL:
			break;
		case NAME:
			if (!getName().equals(name) || !getNamespace().equals(namespace)){
				return false;
			}
			break;
		case CLASS:
			Block block = GameRegistry.findBlock(namespace, name);
			if (block != null && getBlock() != null){
				if (!getBlock().getClass().isAssignableFrom(block.getClass())){
					return false;
				}
				break;
			}
			Item item = GameRegistry.findItem(namespace, name);
			if (item != null && getItem() != null){
				if (!getItem().getClass().isAssignableFrom(item.getClass())){
					return false;
				}
			} else {
				return false;
			}
			break;
		case MATERIAL:
			block = GameRegistry.findBlock(namespace, name);
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
				testDamageValue = GameRegistry.findItem(namespace, name).getMaxDamage() - damageValue;
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
		BlockItemIdentifier other = (BlockItemIdentifier) obj;
		if (!Arrays.equals(damageValues, other.damageValues))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
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
		return GameRegistry.findBlock(namespace, name);
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
		return GameRegistry.findItem(namespace, name);
	}

	/**
	 * Gets the String name of this item/block
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Gets the String namespace of this item/block
	 * @return
	 */
	public String getNamespace(){
		return namespace;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(damageValues);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((namespace == null) ? 0 : namespace.hashCode());
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
		case CLASS:
			builder.append('@');
			break;
		case MATERIAL:
			builder.append('&');
			break;
		}
		if (namespace.length() != 0){
			builder.append(namespace).append(":");
		}
		builder.append(name);
		for (int i = 0; i < damageValues.length; i++){
			builder.append(damageValues[i]);
		}
		return builder.toString();
	}

}
