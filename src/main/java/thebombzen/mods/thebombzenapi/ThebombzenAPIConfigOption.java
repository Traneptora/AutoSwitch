package thebombzen.mods.thebombzenapi;

public interface ThebombzenAPIConfigOption {

	public static final int ARBITRARY_STRING = 3;
	public static final int BOOLEAN = 0;
	public static final int FINITE_STRING = 2;
	public static final int KEY = 1;

	public int getDefaultToggleIndex();

	public String getDefaultValue();

	public String[] getFiniteStringOptions();

	public String[] getInfo();

	public int getOptionType();

	public String getShortInfo();

	@Override
	public String toString();

}
