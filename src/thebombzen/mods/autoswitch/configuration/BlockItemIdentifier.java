package thebombzen.mods.autoswitch.configuration;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import thebombzen.mods.thebombzenapi.configuration.CompoundExpression;
import thebombzen.mods.thebombzenapi.configuration.ConfigFormatException;

public class BlockItemIdentifier extends CompoundExpression<SingleValueIdentifier> {

	public static BlockItemIdentifier parseBlockItemIdentifier(String info) throws ConfigFormatException {
		
		if (info.startsWith("(") && info.endsWith(")")){
			return parseBlockItemIdentifier(info.substring(1, info.length() - 1));
		}
		
		int indexOfX = info.indexOf('^');
		int indexOfO = info.indexOf('|');
		int indexOfA = info.indexOf('&');
		
		
		if (indexOfA < 0 && indexOfO < 0 && indexOfX < 0 && info.indexOf('!') < 0){
			return new BlockItemIdentifier(SingleBlockItemIdentifier.parseSingleBlockItemIdentifier(info));
		}
		
		
		if (indexOfX >= 0){
			String before = info.substring(0, indexOfX);
			String after = info.substring(indexOfX + 1);
			int beforeLeftCount = ThebombzenAPI.countOccurrences(before, '(');
			int beforeRightCount = ThebombzenAPI.countOccurrences(before, ')');
			int afterLeftCount = ThebombzenAPI.countOccurrences(after, '(');
			int afterRightCount = ThebombzenAPI.countOccurrences(after, ')');
			if (beforeLeftCount == beforeRightCount && afterLeftCount == afterRightCount){
				return new BlockItemIdentifier(XOR, BlockItemIdentifier.parseBlockItemIdentifier(before), BlockItemIdentifier.parseBlockItemIdentifier(after));
			}
		}
		
		
		if (indexOfO >= 0){
			String before = info.substring(0, indexOfO);
			String after = info.substring(indexOfO + 1);
			int beforeLeftCount = ThebombzenAPI.countOccurrences(before, '(');
			int beforeRightCount = ThebombzenAPI.countOccurrences(before, ')');
			int afterLeftCount = ThebombzenAPI.countOccurrences(after, '(');
			int afterRightCount = ThebombzenAPI.countOccurrences(after, ')');
			if (beforeLeftCount == beforeRightCount && afterLeftCount == afterRightCount){
				return new BlockItemIdentifier(OR, BlockItemIdentifier.parseBlockItemIdentifier(before), BlockItemIdentifier.parseBlockItemIdentifier(after));
			}
		}
		
		
		if (indexOfA >= 0){
			String before = info.substring(0, indexOfA);
			String after = info.substring(indexOfA + 1);
			int beforeLeftCount = ThebombzenAPI.countOccurrences(before, '(');
			int beforeRightCount = ThebombzenAPI.countOccurrences(before, ')');
			int afterLeftCount = ThebombzenAPI.countOccurrences(after, '(');
			int afterRightCount = ThebombzenAPI.countOccurrences(after, ')');
			if (beforeLeftCount == beforeRightCount && afterLeftCount == afterRightCount){
				return new BlockItemIdentifier(AND, BlockItemIdentifier.parseBlockItemIdentifier(before), BlockItemIdentifier.parseBlockItemIdentifier(after));
			}
		}
		
		if (info.startsWith("!")){
			return new BlockItemIdentifier(NOT, BlockItemIdentifier.parseBlockItemIdentifier(info.substring(1)), null);
		}
		
		throw new ConfigFormatException("Error parsing identifier!");
		
	}
	
	public BlockItemIdentifier(int type, BlockItemIdentifier first, BlockItemIdentifier second){
		super(type, first, second);
	}

	public BlockItemIdentifier(SingleBlockItemIdentifier singleID){
		super(singleID);
	}

	public boolean contains(Block block, int metadata){
		return contains(new SingleValueIdentifier(block, metadata));
	}
	
	public boolean contains(ItemStack itemStack){
		if (itemStack == null){
			return false;
		}
		return contains(new SingleValueIdentifier(itemStack));
	}
	
}
