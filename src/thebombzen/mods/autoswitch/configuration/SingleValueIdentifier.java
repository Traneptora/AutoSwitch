package thebombzen.mods.autoswitch.configuration;

import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

public class SingleValueIdentifier {
	private String modid;
	private String name;
	private int damageValue;
	public SingleValueIdentifier(String modid, String name, int damageValue){
		this.modid = modid;
		this.name = name;
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
		SingleValueIdentifier other = (SingleValueIdentifier) obj;
		if (damageValue != other.damageValue)
			return false;
		if (modid == null) {
			if (other.modid != null)
				return false;
		} else if (!modid.equals(other.modid))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	public int getDamageValue() {
		return damageValue;
	}
	public String getModid() {
		return modid;
	}
	public String getName() {
		return name;
	}
	public UniqueIdentifier getUniqueIdentifier(){
		return new UniqueIdentifier(modid + ":" + name);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + damageValue;
		result = prime * result + ((modid == null) ? 0 : modid.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	public void setDamageValue(int damageValue) {
		this.damageValue = damageValue;
	}
	public void setModid(String modid) {
		this.modid = modid;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	
}
