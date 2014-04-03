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
	public SingleValueIdentifier(SingleValueIdentifier id){
		isItem = id.isItem;
		itemStack = id.itemStack == null ? null : id.itemStack.copy();
		block = id.block;
		metadata = id.metadata;
	}
	public SingleValueIdentifier(ItemStack stack){
		itemStack = stack.copy();
		isItem = true;
	}
	public SingleValueIdentifier(Block block, int metadata){
		this.block = block;
		this.metadata = metadata;
		isItem = false;
	}
	
	public boolean isItem(){
		return isItem;
	}
	
	public ItemStack getItemStack(){
		if (!isItem){
			return null;
		} else {
			return itemStack;
		}
	}
	
	public Item getItem(){
		return GameRegistry.findItem(getModId(), getName());
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
	
	public void setDamageValue(int damage){
		if (isItem){
			itemStack.setItemDamage(damage);
		} else {
			metadata = damage;
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
}
