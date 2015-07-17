package thebombzen.mods.autoswitch.configuration;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import thebombzen.mods.thebombzenapi.configuration.BooleanTester;
import thebombzen.mods.thebombzenapi.configuration.ConfigFormatException;

public class SingleEntityIdentifier implements BooleanTester<EntityLivingBase> {
	private int id;
	public SingleEntityIdentifier(int id){
		this.id = id;
	}
	
	public static SingleEntityIdentifier parseSingleEntityIdentifier(String info) throws ConfigFormatException {
		if (info.equals("@")){
			return new SingleEntityIdentifier(-1);
		}
		int id;
		try {
			id = Integer.parseInt(info);
			return new SingleEntityIdentifier(id);
		} catch (NumberFormatException nfe){
			throw new ConfigFormatException("Invalid number: " + info, nfe);
		}
	}
	
	@Override
	public boolean contains(EntityLivingBase c) {
		return id == -1 || EntityList.getEntityID(c) == id;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		SingleEntityIdentifier other = (SingleEntityIdentifier) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return id == -1 ? "@" : Integer.toString(id);
	}
}
