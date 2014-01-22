package thebombzen.mods.thebombzenapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Properties;

public abstract class ThebombzenAPIConfiguration<T extends Enum & ThebombzenAPIConfigOption> {

	protected ThebombzenAPIBaseMod mod;
	protected Class<T> optionClass;
	protected Properties properties = new Properties();

	protected File propsFile;
	protected long storedConfigLastModified;

	public ThebombzenAPIConfiguration(ThebombzenAPIBaseMod baseMod,
			Class<T> optionClass) {
		mod = baseMod;
		this.optionClass = optionClass;
		propsFile = new File(new File(ThebombzenAPI.proxy.getMinecraftFolder(),
				"config"), mod.getClass().getSimpleName() + ".cfg");
	}

	public ThebombzenAPIConfigOption[] getAllOptions() {
		return optionClass.getEnumConstants();
	}

	public String getProperty(ThebombzenAPIConfigOption option) {
		String value = properties.getProperty(option.toString());
		return (value != null) ? value : "";
	}

	public boolean getPropertyBoolean(ThebombzenAPIConfigOption option) {
		return Boolean.parseBoolean(getProperty(option));
	}

	protected File getPropertyFile() {
		return this.propsFile;
	}

	private void initializeDefaults() {
		for (ThebombzenAPIConfigOption option : getAllOptions()) {
			setPropertyWithoutSave(option, option.getDefaultValue());
		}
	}

	public void load() throws IOException {
		initializeDefaults();
		loadProperties();
		saveProperties();
	}

	protected void loadProperties() throws IOException {
		if (!propsFile.exists()) {
			propsFile.createNewFile();
		}
		InputStream is = new FileInputStream(getPropertyFile());
		properties.load(is);
		is.close();
		for (ThebombzenAPIConfigOption option : getAllOptions()) {
			if (option.getDefaultToggleIndex() >= 0) {
				mod.setToggleDefaultEnabled(option.getDefaultToggleIndex(),
						getPropertyBoolean(option));
			}
		}
		storedConfigLastModified = getPropertyFile().lastModified();
	}

	public boolean reloadPropertiesFromFileIfChanged() throws IOException {
		if (shouldRefreshConfig()) {
			loadProperties();
			return true;
		} else {
			return false;
		}
	}

	public void saveProperties() {
		try {
			PrintStream os = new PrintStream(new FileOutputStream(
					getPropertyFile()));
			properties.store(os, mod.getLongName() + " basic properties");
			ThebombzenAPIConfigOption[] options = getAllOptions();
			Arrays.sort(options);
			StringBuilder builder = new StringBuilder();
			for (ThebombzenAPIConfigOption option : options) {
				builder.append("# ").append(option.toString())
						.append(ThebombzenAPI.newLine);
				for (String info : option.getInfo()) {
					builder.append("#     ").append(info)
							.append(ThebombzenAPI.newLine);
				}
			}
			os.print(builder.toString());
			os.flush();
			os.close();
			storedConfigLastModified = getPropertyFile().lastModified();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setProperty(ThebombzenAPIConfigOption option, String value) {
		setPropertyWithoutSave(option, value);
		saveProperties();
	}

	protected void setPropertyWithoutSave(ThebombzenAPIConfigOption option,
			String value) {
		properties.setProperty(option.toString(), value);
	}

	protected boolean shouldRefreshConfig() {
		long configLastModified = getPropertyFile().lastModified();
		if (storedConfigLastModified != configLastModified) {
			return true;
		} else {
			return false;
		}
	}

}
