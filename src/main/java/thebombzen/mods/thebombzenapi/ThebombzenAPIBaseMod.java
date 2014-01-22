package thebombzen.mods.thebombzenapi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.storage.SaveHandler;
import thebombzen.mods.thebombzenapi.client.ThebombzenAPIConfigScreen;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class ThebombzenAPIBaseMod {

	@SideOnly(Side.CLIENT)
	protected int[] toggleKeyCodes;

	@SideOnly(Side.CLIENT)
	protected boolean[] toggles;

	@SideOnly(Side.CLIENT)
	protected int[] activeKeyCodes;

	@SideOnly(Side.CLIENT)
	protected boolean[] defaultToggles;
	protected File modFolder;
	protected PrintStream debugLogger = null;
	protected StringBuilder debugBuilder = new StringBuilder();
	protected String prevDebugString = "";

	protected File debugFile;

	@SideOnly(Side.CLIENT)
	public abstract void activeKeyPressed(int keyCode);

	@SideOnly(Side.CLIENT)
	public abstract ThebombzenAPIConfigScreen createConfigScreen(GuiScreen base);

	@Override
	protected void finalize() throws Throwable {
		if (debugLogger != null) {
			debugLogger.close();
		}
	}

	protected void forceDebug(String string) {
		forceDebug("%s", string);
	}

	protected void forceDebug(String format, Object... args) {
		String s = String.format(format, args);
		if (s.matches("=+")) {
			String total = debugBuilder.toString();
			debugBuilder = new StringBuilder();
			if (!total.equals(prevDebugString)) {
				debugLogger.print(total);
				debugLogger.flush();
				prevDebugString = total;
			}
		}
		debugBuilder.append(s).append(ThebombzenAPI.newLine);
	}

	@SideOnly(Side.CLIENT)
	public int getActiveKeyCode(int index) {
		if (getNumActiveKeys() <= 0) {
			throw new UnsupportedOperationException();
		}
		if (index < 0 || index >= getNumActiveKeys()) {
			throw new IndexOutOfBoundsException();
		}
		return activeKeyCodes[index];
	}

	@SideOnly(Side.CLIENT)
	public NBTTagCompound getCompoundFromCurrentData() {
		NBTTagCompound settings = new NBTTagCompound();
		byte[] togglesByte = new byte[getNumToggleKeys()];
		for (int i = 0; i < getNumToggleKeys(); i++) {
			togglesByte[i] = isToggleEnabled(i) ? (byte) 1 : (byte) 0;
		}
		settings.setByteArray("toggles", togglesByte);

		NBTTagCompound data = new NBTTagCompound();
		data.setCompoundTag("Settings", settings);
		return data;
	}

	@SideOnly(Side.CLIENT)
	public NBTTagCompound getCompoundFromDefaultData() {
		NBTTagCompound settings = new NBTTagCompound();
		byte[] togglesByte = new byte[getNumToggleKeys()];
		for (int i = 0; i < getNumToggleKeys(); i++) {
			togglesByte[i] = isToggleDefaultEnabled(i) ? (byte) 1 : (byte) 0;
		}
		settings.setByteArray("toggles", togglesByte);

		NBTTagCompound data = new NBTTagCompound();
		data.setCompoundTag("Settings", settings);
		return data;
	}

	public abstract ThebombzenAPIConfiguration<?> getConfiguration();

	@SideOnly(Side.CLIENT)
	public File getCorrectMemoryFile() {
		if (Minecraft.getMinecraft().theWorld == null) {
			return null;
		}
		if (!Minecraft.getMinecraft().isSingleplayer()) {
			return new File(getModFolder(), "MEMORYSMP.dat");
		} else {
			return new File(((SaveHandler) Minecraft
					.getMinecraft()
					.getIntegratedServer()
					.worldServerForDimension(
							Minecraft.getMinecraft().thePlayer.dimension)
					.getSaveHandler()).getWorldDirectory(), getLongName()
					.toUpperCase() + "_MEMORY.dat");
		}
	}

	public String getLatestVersion() {
		String latestVersion = null;
		try {
			URL versionURL = getVersionFileURL();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					versionURL.openStream()));
			latestVersion = br.readLine();
			br.close();
		} catch (Throwable t) {
			latestVersion = getLongVersionString();
		}
		return latestVersion;
	}

	public abstract String getLongName();

	public abstract String getLongVersionString();

	public File getModFolder() {
		return modFolder;
	}

	@SideOnly(Side.CLIENT)
	public abstract int getNumActiveKeys();

	@SideOnly(Side.CLIENT)
	public abstract int getNumToggleKeys();

	public abstract String getShortName();

	@SideOnly(Side.CLIENT)
	public int getToggleKeyCode(int index) {
		if (getNumToggleKeys() <= 0) {
			throw new UnsupportedOperationException();
		}
		if (index < 0 || index >= getNumToggleKeys()) {
			throw new IndexOutOfBoundsException();
		}
		return toggleKeyCodes[index];
	}

	@SideOnly(Side.CLIENT)
	protected abstract String getToggleMessageString(int index, boolean enabled);

	public URL getVersionFileURL() {
		try {
			return new URL(getVersionFileURLString());
		} catch (MalformedURLException murle) {
			return null;
		}
	}

	protected abstract String getVersionFileURLString();

	@SideOnly(Side.CLIENT)
	public abstract boolean hasConfigScreen();

	@SideOnly(Side.CLIENT)
	public boolean isToggleDefaultEnabled(int index) {
		if (getNumToggleKeys() <= 0) {
			throw new UnsupportedOperationException();
		}
		if (index < 0 || index >= getNumToggleKeys()) {
			throw new IndexOutOfBoundsException();
		}
		return defaultToggles[index];
	}

	@SideOnly(Side.CLIENT)
	public boolean isToggleEnabled(int index) {
		if (getNumToggleKeys() <= 0) {
			throw new UnsupportedOperationException();
		}
		if (index < 0 || index >= getNumToggleKeys()) {
			throw new IndexOutOfBoundsException();
		}
		return toggles[index];
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent fmlpie) {

		if (fmlpie.getSide().isClient()) {
			toggleKeyCodes = new int[getNumToggleKeys()];
			toggles = new boolean[getNumToggleKeys()];
			defaultToggles = new boolean[getNumToggleKeys()];
			activeKeyCodes = new int[getNumActiveKeys()];
		}

		ThebombzenAPI.registerMod(this);

		File mineFile = ThebombzenAPI.proxy.getMinecraftFolder();
		File modsFolder = new File(mineFile, "mods");
		modFolder = new File(modsFolder, getLongName());
		modFolder.mkdirs();

		debugFile = new File(modFolder, "DEBUG.txt");
		try {
			debugLogger = new PrintStream(new FileOutputStream(debugFile));
		} catch (FileNotFoundException fnfe) {
			debugLogger = null;
			throwException("Unable to open debug output file.", fnfe, false);
		}

		try {
			getConfiguration().load();
		} catch (IOException ioe) {
			throwException("Unable to open configuration!", ioe, true);
		}

	}

	@SideOnly(Side.CLIENT)
	public void readFromCorrectMemoryFile() {
		File file = getCorrectMemoryFile();
		if (file != null) {
			NBTTagCompound data = readFromMemoryFile(file);
			saveCompoundToCurrentData(data);
		}
	}

	@SideOnly(Side.CLIENT)
	public NBTTagCompound readFromMemoryFile(File file) {
		try {
			if (!file.isFile()) {
				writeToMemoryFile(file, getCompoundFromDefaultData());
				return getCompoundFromDefaultData();
			}
			InputStream in = new FileInputStream(file);
			NBTTagCompound data = null;
			try {
				data = CompressedStreamTools.readCompressed(in);
			} catch (IOException ioe) {
				writeToMemoryFile(file, getCompoundFromCurrentData());
			}
			if (data != null) {
				return data;
			}
		} catch (IOException ioe) {
			throwException("Couldn't read from memory file.", ioe, false);
		}
		return getCompoundFromCurrentData();
	}

	@SideOnly(Side.CLIENT)
	public void saveCompoundToCurrentData(NBTTagCompound data) {
		NBTTagCompound settings = data.getCompoundTag("Settings");
		if (settings == null) {
			return;
		}
		byte[] togglesByte = settings.getByteArray("toggles");
		if (togglesByte.length == 0) {
			for (int i = 0; i < getNumToggleKeys(); i++) {
				setToggleEnabled(i, isToggleDefaultEnabled(i), false);
			}
		} else {
			for (int i = 0; i < getNumToggleKeys(); i++) {
				setToggleEnabled(i, togglesByte[i] != 0, false);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void setActiveKeyCode(int index, int keyCode) {
		if (getNumActiveKeys() <= 0) {
			throw new UnsupportedOperationException();
		}
		if (index < 0 || index >= getNumActiveKeys()) {
			throw new IndexOutOfBoundsException();
		}
		activeKeyCodes[index] = keyCode;
	}

	@SideOnly(Side.CLIENT)
	public void setToggleDefaultEnabled(int index, boolean enabled) {
		if (getNumToggleKeys() <= 0) {
			throw new UnsupportedOperationException();
		}
		if (index < 0 || index >= getNumToggleKeys()) {
			throw new IndexOutOfBoundsException();
		}
		defaultToggles[index] = enabled;
	}

	@SideOnly(Side.CLIENT)
	public void setToggleEnabled(int index, boolean enabled, boolean keyPress) {
		if (getNumToggleKeys() <= 0) {
			throw new UnsupportedOperationException();
		}
		if (index < 0 || index >= getNumToggleKeys()) {
			throw new IndexOutOfBoundsException();
		}
		toggles[index] = enabled;
		if (keyPress) {
			Minecraft.getMinecraft().thePlayer
					.sendChatToPlayer(ChatMessageComponent
							.createFromText(getToggleMessageString(index,
									enabled)));
		}

	}

	@SideOnly(Side.CLIENT)
	public void setToggleKeyCode(int index, int keyCode) {
		if (getNumToggleKeys() <= 0) {
			throw new UnsupportedOperationException();
		}
		if (index < 0 || index >= getNumToggleKeys()) {
			throw new IndexOutOfBoundsException();
		}
		toggleKeyCodes[index] = keyCode;
	}

	public void throwException(String info, Throwable exception, boolean fatal) {
		System.err.println(info);
		exception.printStackTrace();
		if (debugLogger != null) {
			debugLogger.println(info);
			exception.printStackTrace(debugLogger);
			debugLogger.flush();
		}
		if (fatal) {
			throw new RuntimeException(info, exception);
		}
	}

	@SideOnly(Side.CLIENT)
	public void writeToCorrectMemoryFile() {
		File file = getCorrectMemoryFile();
		if (file != null) {
			NBTTagCompound data = getCompoundFromCurrentData();
			writeToMemoryFile(file, data);
		}
	}

	@SideOnly(Side.CLIENT)
	public void writeToMemoryFile(File file, NBTTagCompound config) {
		try {
			file.delete();
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			CompressedStreamTools.writeCompressed(config, fos);
		} catch (IOException ioe) {
			throwException("Couldn't write to memory file.", ioe, false);
		}
	}

}
