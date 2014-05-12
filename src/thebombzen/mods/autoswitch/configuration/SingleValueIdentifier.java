package thebombzen.mods.autoswitch.configuration;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

public class SingleValueIdentifier {
	private ItemStack itemStack = null;
	private Block block = null;
	private int metadata = 0;
	private boolean isItem;
	public SingleValueIdentifier(Block block, int metadata){
		this.block = block;
		this.metadata = metadata;
		isItem = false;
	}
	public SingleValueIdentifier(ItemStack stack){
		itemStack = stack.copy();
		isItem = true;
	}
	public SingleValueIdentifier(SingleValueIdentifier id){
		isItem = id.isItem;
		itemStack = id.itemStack == null ? null : id.itemStack.copy();
		block = id.block;
		metadata = id.metadata;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SingleValueIdentifier other = (SingleValueIdentifier) obj;
		if (block == null) {
			if (other.block != null)
				return false;
		} else if (!block.equals(other.block))
			return false;
		if (isItem != other.isItem)
			return false;
		if (itemStack == null) {
			if (other.itemStack != null)
				return false;
		} else if (!itemStack.equals(other.itemStack))
			return false;
		if (metadata != other.metadata)
			return false;
		return true;
	}
	
	public Block getBlock(){
		return GameRegistry.findBlock(getModId(), getName());
	}
	
	public int getDamageValue(){
		if (isItem){
			return itemStack.getItemDamage();
		} else {
			return metadata;
		}
	}
	
	public Item getItem(){
		return GameRegistry.findItem(getModId(), getName());
	}
	
	public ItemStack getItemStack(){
		if (!isItem){
			return null;
		} else {
			return itemStack;
		}
	}
	
	public String getModId(){
		return getUniqueIdentifier().modId;
	}
	
	public String getName(){
		return getUniqueIdentifier().name;
	}
	
	public UniqueIdentifier getUniqueIdentifier(){
		if (isItem){
			return GameRegistry.findUniqueIdentifierFor(itemStack.getItem());
		} else {
			return GameRegistry.findUniqueIdentifierFor(block);
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((block == null) ? 0 : block.hashCode());
		result = prime * result + (isItem ? 1231 : 1237);
		result = prime * result
				+ ((itemStack == null) ? 0 : itemStack.hashCode());
		result = prime * result + metadata;
		return result;
	}
	public boolean isItem(){
		return isItem;
	}
	public void setDamageValue(int damage){
		if (isItem){
			itemStack.setItemDamage(damage);
		} else {
			metadata = damage;
		}
	}	
}
