package thebombzen.mods.autoswitch.configuration;

import com.thebombzen.mods.thebombzenapi.ThebombzenAPI;
import com.thebombzen.mods.thebombzenapi.configuration.CompoundExpression;
import com.thebombzen.mods.thebombzenapi.configuration.ConfigFormatException;

import net.minecraft.entity.EntityLivingBase;

public class EntityIdentifier extends CompoundExpression<EntityLivingBase> {
	
	public static EntityIdentifier parseEntityIdentifier(String info) throws ConfigFormatException {
		
		if (info.length() == 0){
			throw new ConfigFormatException("Empty entity identifier!");
		}
		
		if (!info.contains("&") && !info.contains("|") && !info.contains("^") && !info.contains("!")){
			if (info.startsWith("(") && info.endsWith(")")){
				return parseEntityIdentifier(info.substring(1, info.length() - 1));
			} else {
				return new EntityIdentifier(SingleEntityIdentifier.parseSingleEntityIdentifier(info));
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
				return new EntityIdentifier(XOR, EntityIdentifier.parseEntityIdentifier(before), EntityIdentifier.parseEntityIdentifier(after));
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
				return new EntityIdentifier(OR, EntityIdentifier.parseEntityIdentifier(before), EntityIdentifier.parseEntityIdentifier(after));
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
				return new EntityIdentifier(AND, EntityIdentifier.parseEntityIdentifier(before), EntityIdentifier.parseEntityIdentifier(after));
			} else {
				index = info.indexOf('&', index + 1);
			}
		}
		
		if (info.startsWith("!")){
			return new EntityIdentifier(NOT, EntityIdentifier.parseEntityIdentifier(info.substring(1)), null);
		}
		
		if (info.startsWith("(") && info.endsWith(")")){
			return EntityIdentifier.parseEntityIdentifier(info.substring(1, info.length() - 1));
		}
		
		throw new ConfigFormatException("Malformed entity identifier: " + info);
		
	}
	
	public EntityIdentifier(int type, EntityIdentifier first, EntityIdentifier second){
		super(type, first, second);
	}

	public EntityIdentifier(SingleEntityIdentifier singleID){
		super(singleID);
	}

}
