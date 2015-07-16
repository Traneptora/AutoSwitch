package thebombzen.mods.autoswitch.configuration;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.UniqueIdentifier;
import thebombzen.mods.autoswitch.AutoSwitch;

public class SingleValueIdentifier {
	private ItemStack itemStack = null;
	private IBlockState block = null;
	private boolean isItem;
	public SingleValueIdentifier(IBlockState block){
		this.block = block;
		isItem = false;
	}
	public SingleValueIdentifier(ItemStack stack){
		itemStack = stack == null ? null : stack.copy();
		isItem = true;
	}
	public SingleValueIdentifier(SingleValueIdentifier id){
		isItem = id.isItem;
		itemStack = id.itemStack == null ? null : id.itemStack.copy();
		block = id.block;
	}
	
	public ItemStack getItemStack(){
		if (!isItem){
			AutoSwitch.instance.throwException("Getting ItemStack of block SVI", new UnsupportedOperationException(), false);
			return null;
		} else {
			return itemStack;
		}
	}
	
	
	public IBlockState getBlockState(){
		if (isItem){
			AutoSwitch.instance.throwException("Getting IBlockState of item SVI", new UnsupportedOperationException(), false);
			return null;
		} else {
			return block;
		}
	}
	
	public String getModId(){
		if (isItem && itemStack == null){
			return null;
		}
		return getUniqueIdentifier().modId;
	}
	
	public String getName(){
		if (isItem && itemStack == null){
			return null;
		}
		return getUniqueIdentifier().name;
	}
	
	public UniqueIdentifier getUniqueIdentifier(){
		if (isItem){
			if (itemStack == null){
				return null;
			}
			return GameRegistry.findUniqueIdentifierFor(itemStack.getItem());
		} else {
			return GameRegistry.findUniqueIdentifierFor(block.getBlock());
		}
	}
	
	public boolean isItem(){
		return isItem;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((block == null) ? 0 : block.hashCode());
		result = prime * result + (isItem ? 1231 : 1237);
		result = prime * result
				+ ((itemStack == null) ? 0 : itemStack.hashCode());
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
		return true;
	}
	@Override
	public String toString() {
		return "SingleValueIdentifier [itemStack=" + itemStack + ", block="
				+ block + ", isItem=" + isItem + "]";
	}
}
