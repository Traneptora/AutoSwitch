package thebombzen.mods.autoswitch.configuration;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import thebombzen.mods.thebombzenapi.configuration.BooleanTester;
import thebombzen.mods.thebombzenapi.configuration.ConfigFormatException;

public class SingleEntityIdentifier implements BooleanTester<EntityLivingBase> {
	private int id;
	private String name;
	
	public SingleEntityIdentifier(){
		this.id = -1;
		this.name = null;
	}
	
	public SingleEntityIdentifier(int id){
		this.id = id;
		this.name = null;
	}
	
	public SingleEntityIdentifier(String name){
		this.id = -1;
		this.name = name.toLowerCase();
	}
	
	public static SingleEntityIdentifier parseSingleEntityIdentifier(String info) throws ConfigFormatException {
		if (info.equals("@")){
			return new SingleEntityIdentifier();
		}
		int id;
		try {
			id = Integer.parseInt(info);
			return new SingleEntityIdentifier(id);
		} catch (NumberFormatException nfe){
			
		}
		if (info.matches("^\\w+$")){
			return new SingleEntityIdentifier(info);
		}
		throw new ConfigFormatException("Illegal SingleEntityIdentifier: " + info);
	}
	
	@Override
	public boolean contains(EntityLivingBase c) {
		if (id == -1){
			if (name == null){
				return true;
			} else {
				return EntityList.getEntityString(c).toLowerCase().equals(name);
			}
		} else {
			return EntityList.getEntityID(c) == id;
		}
	}
	
	@Override
	public String toString() {
		return id == -1 ? (name == null ? "@" : name) : Integer.toString(id);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
