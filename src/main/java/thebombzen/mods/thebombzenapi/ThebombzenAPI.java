package thebombzen.mods.thebombzenapi;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.MathHelper;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import thebombzen.mods.thebombzenapi.client.ClientProxy;
import thebombzen.mods.thebombzenapi.client.ThebombzenAPIConfigOpenScreen;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModAPIManager;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = "ThebombzenAPI", name = "ThebombzenAPI", version = "2.2.0")
public class ThebombzenAPI implements ITickHandler {

	@Instance(value = "ThebombzenAPI")
	public static ThebombzenAPI instance;

	@SidedProxy(clientSide = "thebombzen.mods.thebombzenapi.client.ClientProxy", serverSide = "thebombzen.mods.thebombzenapi.CommonProxy")
	public static CommonProxy proxy;

	@SideOnly(Side.CLIENT)
	private static Map<ThebombzenAPIBaseMod, boolean[]> keysDown;

	private static List<ThebombzenAPIBaseMod> mods = new ArrayList<ThebombzenAPIBaseMod>();
	public static final String newLine = String.format("%n");
	public static int prevWorld = 0;

	@SideOnly(Side.CLIENT)
	private static int[] thebombzenAPIKeyCodes;

	@SideOnly(Side.CLIENT)
	private static boolean[] thebombzenAPIKeysDown;

	@SideOnly(Side.CLIENT)
	private static Map<ThebombzenAPIBaseMod, boolean[]> togglesDown;

	public static boolean areItemStackListsEqual(List<ItemStack> list1,
			List<ItemStack> list2) {
		if (list1.size() != list2.size()) {
			return false;
		}

		List<ItemStack> list1C = new ArrayList<ItemStack>(list1);
		List<ItemStack> list2C = new ArrayList<ItemStack>(list2);

		Iterator<ItemStack> iter1 = list1C.iterator();

		outer: while (iter1.hasNext()) {
			ItemStack stack1 = iter1.next();
			Iterator<ItemStack> iter2 = list2C.iterator();
			while (iter2.hasNext()) {
				ItemStack stack2 = iter2.next();
				if (ItemStack.areItemStacksEqual(stack1, stack2)) {
					iter2.remove();
					iter1.remove();
					continue outer;
				}
			}
			return false;
		}
		return true;
	}

	public static List<Integer> asList(int[] toList) {
		List<Integer> ret = new ArrayList<Integer>();
		for (int i : toList) {
			ret.add(i);
		}
		return ret;
	}

	public static Object callPrivateMethod(Object arg, Class<?> clazz,
			String name, Class<?>[] parameterTypes, Object... args) {
		String[] names = { name };
		return callPrivateMethod(arg, clazz, names, parameterTypes, args);
	}

	public static Object callPrivateMethod(Object arg, Class<?> clazz,
			String[] names, Class<?>[] parameterTypes, Object... args) {
		for (String name : names) {
			try {
				Method method = clazz.getDeclaredMethod(name, parameterTypes);
				method.setAccessible(true);
				try {
					return method.invoke(arg, args);
				} catch (Exception e) {
					return null;
				}
			} catch (NoSuchMethodException nsme) {
				continue;
			}
		}
		return null;
	}

	public static int ceil_float(float f) {
		return -MathHelper.floor_float(-f);
	}

	public static int ceilDouble(double d) {
		return -MathHelper.floor_double(-d);
	}

	public static ThebombzenAPIBaseMod[] getMods() {
		return mods.toArray(new ThebombzenAPIBaseMod[mods.size()]);
	}

	public static Object getPrivateField(Object arg, Class<?> clazz, String name) {
		return getPrivateField(arg, clazz, new String[] { name });
	}

	public static Object getPrivateField(Object arg, Class<?> clazz,
			String[] names) {
		for (String name : names) {
			try {
				Field field = clazz.getDeclaredField(name);
				field.setAccessible(true);
				try {
					return field.get(arg);
				} catch (Exception e) {
					return null;
				}
			} catch (NoSuchFieldException nsfe) {
				continue;
			}
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	private static void handleKeyBindings() {
		for (ThebombzenAPIBaseMod mod : mods) {
			for (int i = 0; i < mod.getNumToggleKeys(); i++) {
				int keyCode = mod.getToggleKeyCode(i);
				boolean state = (keyCode < 0 ? Mouse
						.isButtonDown(keyCode + 100) : Keyboard
						.isKeyDown(keyCode));
				if (state && !togglesDown.get(mod)[i]) {
					mod.setToggleEnabled(i, !mod.isToggleEnabled(i), true);
					mod.writeToCorrectMemoryFile();
				}
				togglesDown.get(mod)[i] = state;
			}
			for (int i = 0; i < mod.getNumActiveKeys(); i++) {
				int keyCode = mod.getActiveKeyCode(i);
				boolean state = (keyCode < 0 ? Mouse
						.isButtonDown(keyCode + 100) : Keyboard
						.isKeyDown(keyCode));
				if (state && !keysDown.get(mod)[i]) {
					mod.activeKeyPressed(keyCode);
				}
				keysDown.get(mod)[i] = state;
			}
		}
		for (int i = 0; i < thebombzenAPIKeyCodes.length; i++) {
			int keyCode = thebombzenAPIKeyCodes[i];
			boolean state = (keyCode < 0 ? Mouse.isButtonDown(keyCode + 100)
					: Keyboard.isKeyDown(keyCode));
			if (state && !thebombzenAPIKeysDown[i]) {
				if (i == 0) { // open thebombzenapiguioptions
					if (Keyboard.isKeyDown(42)) {
						Minecraft.getMinecraft().displayGuiScreen(
								new ThebombzenAPIConfigOpenScreen());
					}
				}
			}
			thebombzenAPIKeysDown[i] = state;
		}
	}

	public static boolean isCurrentlyExecutingMethod(String methodName) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : trace) {
			if (element.getMethodName().equals(methodName)) {
				return true;
			}
		}
		return false;
	}

	public static void registerMod(ThebombzenAPIBaseMod mod) {
		if (!mods.contains(mod)) {
			mods.add(mod);
		} else {
			return;
		}
		if (!CommonProxy.class.equals(proxy.getClass())){
			boolean[] keysDownArray = new boolean[mod.getNumToggleKeys()];
			togglesDown.put(mod, keysDownArray);
		}
	}

	public static void setPrivateField(Object arg, Class<?> clazz, String name,
			Object set) {
		setPrivateField(arg, clazz, new String[] { name }, set);
	}

	public static void setPrivateField(Object arg, Class<?> clazz,
			String[] names, Object set) {
		for (String name : names) {
			try {
				Field field = clazz.getDeclaredField(name);
				field.setAccessible(true);
				try {
					field.set(arg, set);
				} catch (Exception e) {
					return;
				}
			} catch (NoSuchFieldException nsfe) {
				continue;
			}
		}
	}

	public static int[] toArray(Collection<? extends Integer> list) {
		List<Integer> ret = new ArrayList<Integer>();
		ret.addAll(list);
		return toArray(ret);
	}

	public static int[] toArray(List<? extends Integer> list) {
		int[] ret = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			ret[i] = list.get(i);
		}
		return ret;
	}

	@SideOnly(Side.CLIENT)
	public void clientTick() {

		Minecraft mc = Minecraft.getMinecraft();

		if (mc.theWorld == null) {
			return;
		}

		int currWorld = System.identityHashCode(mc.theWorld);

		if (prevWorld == 0) {
			for (ThebombzenAPIBaseMod mod : mods) {
				String latestVersion = mod.getLatestVersion();
				if (!latestVersion.equals(mod.getLongVersionString())) {
					mc.thePlayer.sendChatToPlayer(ChatMessageComponent
							.createFromText(latestVersion + " is available."));
				}
			}
		}

		if (prevWorld != currWorld) {
			for (ThebombzenAPIBaseMod mod : mods) {
				mod.readFromCorrectMemoryFile();
			}
		}

		if (mc.currentScreen == null) {
			handleKeyBindings();
		}

		for (ThebombzenAPIBaseMod mod : mods) {
			try {
				mod.getConfiguration().reloadPropertiesFromFileIfChanged();
			} catch (IOException ioe) {
				mod.throwException("Could not read properties!", ioe, false);
			}
		}

		prevWorld = currWorld;
	}

	@Override
	public String getLabel() {
		return "thebombzen.mods.thebombzenapi.ThebombzenAPI";
	}

	@EventHandler
	public void load(FMLInitializationEvent event) {
		if (event.getSide().equals(Side.CLIENT)) {
			TickRegistry.registerTickHandler(this, Side.CLIENT);
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if (event.getSide().equals(Side.CLIENT)) {
			keysDown = new HashMap<ThebombzenAPIBaseMod, boolean[]>();
			thebombzenAPIKeyCodes = new int[] { Keyboard.KEY_B };
			thebombzenAPIKeysDown = new boolean[] { false };
			togglesDown = new HashMap<ThebombzenAPIBaseMod, boolean[]>();
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if (type.contains(TickType.CLIENT)) {
			clientTick();
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {

	}

}
