package thebombzen.mods.autoswitch;

public class BlockToolPair {
	private IDMetadataPair block;

	private int tool;

	public BlockToolPair(IDMetadataPair block, int tool) {
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
		if (tool != other.tool)
			return false;
		return true;
	}

	public IDMetadataPair getBlock() {
		return block;
	}

	public int getTool() {
		return tool;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((block == null) ? 0 : block.hashCode());
		result = prime * result + tool;
		return result;
	}

	public void setBlock(IDMetadataPair block) {
		this.block = block;
	}

	public void setTool(int tool) {
		this.tool = tool;
	}

}
