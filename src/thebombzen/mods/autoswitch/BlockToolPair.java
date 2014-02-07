package thebombzen.mods.autoswitch;


/**
 * This is a convenience class for creating pairings between blocks and tools.
 * The method names are self-explanatory.
 * @author thebombzen
 */
public class BlockToolPair {
	
	private BlockItemIdentifier block;
	private BlockItemIdentifier tool;

	public BlockToolPair(BlockItemIdentifier block, BlockItemIdentifier tool) {
		this.block = block;
		this.tool = tool;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlockToolPair other = (BlockToolPair) obj;
		if (block == null) {
			if (other.block != null)
				return false;
		} else if (!block.equals(other.block))
			return false;
		if (tool == null) {
			if (other.tool != null)
				return false;
		} else if (!tool.equals(other.tool))
			return false;
		return true;
	}

	public BlockItemIdentifier getBlock() {
		return block;
	}

	public BlockItemIdentifier getTool() {
		return tool;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((block == null) ? 0 : block.hashCode());
		result = prime * result + ((tool == null) ? 0 : tool.hashCode());
		return result;
	}

	public void setBlock(BlockItemIdentifier block) {
		this.block = block;
	}

	public void setTool(BlockItemIdentifier tool) {
		this.tool = tool;
	}

	@Override
	public String toString() {
		return "BlockToolPair [block=" + block + ", tool=" + tool + "]";
	}

}
