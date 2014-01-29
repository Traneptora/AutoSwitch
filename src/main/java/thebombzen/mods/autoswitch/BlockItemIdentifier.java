package thebombzen.mods.autoswitch;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import cpw.mods.fml.common.registry.GameData;

/**
 * Represents a block or item with the given String name and damage values.
 * @author thebombzen
 */
public class BlockItemIdentifier {

	private String name;
	private ValueSet[] damageValues;

	/**
	 * Construct an identifier with the given String id
	 * @param name The String name such as "minecraft:log"
	 */
	public BlockItemIdentifier(String name){
		this(name, new ValueSet());
	}
	
	public BlockItemIdentifier(String name, int damageValue){
		this(name, new ValueSet(damageValue, false));
	}
	
	/**
	 * Construct an identifier with the given String id and data value.
	 * @param name The String id such as "minecraft:log"
	 * @param damageValues The Damage values
	 */
	public BlockItemIdentifier(String name, ValueSet... damageValues){
		this.name = name;
		this.damageValues = damageValues;
	}

	/**
	 * Gets the block associated with this identifier.
	 * null if this is not a block.
	 */
	public Block getBlock() {
		return GameData.blockRegistry.get(name);
	}

	public ValueSet[] getDamageValues() {
		return damageValues;
	}

	/**
	 * Gets the item associated with this identifier. If it's a block it
	 * will return the corresponding ItemBlock.
	 */
	public Item getItem() {
		return GameData.itemRegistry.get(name);
	}
	
	/**
	 * Gets the String name of this item
	 */
	public String getName(){
		return name;
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
		builder.append(name);
		for (int i = 0; i < damageValues.length; i++){
			builder.append(damageValues[i]);
		}
		return builder.toString();
	}
	
	/**
	 * Gets whether this identifier refers to the given name and damage value.
	 * @param name The String name of this block/item
	 * @param damageValue The damage value of this block/item
	 */
	public boolean contains(String name, int damageValue){
		if (!getName().equals(name)){
			return false;
		}
		for (int i = damageValues.length - 1; i >= 0; i--){
			if (damageValues[i].contains(damageValue)){
				return !damageValues[i].doesSubtract();
			}
		}
		return false;
	}

}
