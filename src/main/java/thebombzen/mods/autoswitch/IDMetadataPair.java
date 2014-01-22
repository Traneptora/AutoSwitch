package thebombzen.mods.autoswitch;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

/**
 * Represents either a block or an item, with the given Shifted Index and Damage
 * Value.
 */

public class IDMetadataPair {

	private int damageValue;
	private int shiftedIndex;

	/**
	 * Create an IDMetadataPair
	 * 
	 * @param shiftedIndex
	 *            The blockID or shiftedIndex of the block/item.
	 * @param damageValue
	 *            The metadata or damageValue of the block/item.
	 */
	public IDMetadataPair(int shiftedIndex, int damageValue) {
		this.shiftedIndex = shiftedIndex;
		this.damageValue = damageValue;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IDMetadataPair other = (IDMetadataPair) obj;
		if (damageValue != other.damageValue)
			return false;
		if (shiftedIndex != other.shiftedIndex)
			return false;
		return true;
	}

	/**
	 * Gets the block associated with this IDMetadataPair. If it's just an item
	 * throw {UnsupportedOperationException}.
	 */
	public Block getBlock() throws UnsupportedOperationException {
		if (!this.isBlock()) {
			throw new UnsupportedOperationException("This is not a block.");
		}
		return Block.blocksList[shiftedIndex];
	}

	public int getDamageValue() {
		return damageValue;
	}

	/**
	 * Gets the item associated with this IDMetadataPair. If it's a block it
	 * will return the corresponding ItemBlock.
	 */
	public Item getItem() throws UnsupportedOperationException {
		if (!this.isItem()) {
			throw new UnsupportedOperationException("This is not an item.");
		}
		return Item.itemsList[shiftedIndex];
	}

	public int getShiftedIndex() {
		return shiftedIndex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + damageValue;
		result = prime * result + shiftedIndex;
		return result;
	}

	public boolean includes(IDMetadataPair pair) {
		if (pair == null) {
			return false;
		}
		if (this.shiftedIndex != pair.shiftedIndex) {
			return false;
		}
		if (this.damageValue == -1 || this.damageValue == pair.damageValue) {
			return true;
		}
		return false;
	}

	/**
	 * Determines whether this IDMetadataPair is a valid (non-null) block. Air
	 * is not a valid block.
	 * 
	 * @return Whether this is a valid Block object.
	 */
	public boolean isBlock() {
		return this.shiftedIndex > 0
				&& this.shiftedIndex < Block.blocksList.length
				&& Block.blocksList[this.shiftedIndex] != null;
	}

	/**
	 * Determines whether this IDMetadataPair is a valid (non-null) item. All
	 * valid registered blocks are also valid items, see
	 * {net.minecraft.item.ItemBlock}
	 * 
	 * @return Whether this is a valid Item object.
	 */
	public boolean isItem() {
		return this.shiftedIndex > 0
				&& this.shiftedIndex < Item.itemsList.length
				&& Item.itemsList[this.shiftedIndex] != null;
	}

	public void setDamageValue(int damageValue) {
		this.damageValue = damageValue;
	}

	public void setShiftedIndex(int shiftedIndex) {
		this.shiftedIndex = shiftedIndex;
	}

	@Override
	public String toString() {
		if (this.damageValue == -1) {
			return String.format("%d", shiftedIndex);
		} else {
			return String.format("%d:%d", shiftedIndex, damageValue);
		}
	}

}
