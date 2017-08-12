package thebombzen.mods.autoswitch.configuration;

import com.thebombzen.mods.thebombzenapi.ThebombzenAPI;
import com.thebombzen.mods.thebombzenapi.configuration.CompoundExpression;
import com.thebombzen.mods.thebombzenapi.configuration.ConfigFormatException;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

public class BlockItemIdentifier extends CompoundExpression<SingleValueIdentifier> {

	public static BlockItemIdentifier parseBlockItemIdentifier(String info) throws ConfigFormatException {
		
		if (info.length() == 0){
			throw new ConfigFormatException("Empty block/item identifier");
		}
		
		if (!info.contains("&") && !info.contains("|") && !info.contains("^") && !info.contains("!")){
			if (info.startsWith("(") && info.endsWith(")")){
				return parseBlockItemIdentifier(info.substring(1, info.length() - 1));
			} else {
				return new BlockItemIdentifier(SingleBlockItemIdentifier.parseSingleBlockItemIdentifier(info));
			}
		}
		
		int index = info.indexOf('^');
		while (index >= 0){
			if (index == 0){
				throw new ConfigFormatException("^ requires something on both sides: " + info);
			}
			if (ThebombzenAPI.isSeparatorAtTopLevel(info, index)){
				String before = info.substring(0, index);
				String after = info.substring(index + 1);
				return new BlockItemIdentifier(XOR, BlockItemIdentifier.parseBlockItemIdentifier(before), BlockItemIdentifier.parseBlockItemIdentifier(after));
			} else {
				index = info.indexOf('^', index + 1);
			}
		}
		
		index = info.indexOf('|');
		while (index >= 0){
			if (index == 0){
				throw new ConfigFormatException("| requires something on both sides: " + info);
			}
			if (ThebombzenAPI.isSeparatorAtTopLevel(info, index)){
				String before = info.substring(0, index);
				String after = info.substring(index + 1);
				return new BlockItemIdentifier(OR, BlockItemIdentifier.parseBlockItemIdentifier(before), BlockItemIdentifier.parseBlockItemIdentifier(after));
			} else {
				index = info.indexOf('|', index + 1);
			}
		}
		
		index = info.indexOf('&');
		while (index >= 0){
			if (index == 0){
				throw new ConfigFormatException("& requires something on both sides: " + info);
			}
			if (ThebombzenAPI.isSeparatorAtTopLevel(info, index)){
				String before = info.substring(0, index);
				String after = info.substring(index + 1);
				return new BlockItemIdentifier(AND, BlockItemIdentifier.parseBlockItemIdentifier(before), BlockItemIdentifier.parseBlockItemIdentifier(after));
			} else {
				index = info.indexOf('&', index + 1);
			}
		}
		
		if (info.startsWith("!")){
			return new BlockItemIdentifier(NOT, BlockItemIdentifier.parseBlockItemIdentifier(info.substring(1)), null);
		}
		
		if (info.startsWith("(") && info.endsWith(")")){
			return BlockItemIdentifier.parseBlockItemIdentifier(info.substring(1, info.length() - 1));
		}
		
		throw new ConfigFormatException("Malformed block/item identifier: " + info);
		
	}
	
	public BlockItemIdentifier(int type, BlockItemIdentifier first, BlockItemIdentifier second){
		super(type, first, second);
	}

	public BlockItemIdentifier(SingleBlockItemIdentifier singleID){
		super(singleID);
	}

	public boolean contains(IBlockState state){
		return contains(new SingleValueIdentifier(state));
	}
	
	public boolean contains(ItemStack itemStack){
		if (itemStack == null){
			return false;
		}
		return contains(new SingleValueIdentifier(itemStack));
	}
	
}
