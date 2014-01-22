package thebombzen.mods.thebombzenapi.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import thebombzen.mods.thebombzenapi.ThebombzenAPIBaseMod;
import thebombzen.mods.thebombzenapi.ThebombzenAPIConfigOption;
import thebombzen.mods.thebombzenapi.ThebombzenAPIConfiguration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class ThebombzenAPIConfigScreen extends GuiScreen {

	protected ThebombzenAPIConfiguration<?> config;
	protected GuiButton currentKeyButton = null;
	protected ThebombzenAPIBaseMod mod;
	protected GuiScreen parentScreen;
	protected final String title;
	protected Map<ThebombzenAPIConfigGuiButton, ThebombzenAPIConfigOption> tooltipButtons = new HashMap<ThebombzenAPIConfigGuiButton, ThebombzenAPIConfigOption>();

	public ThebombzenAPIConfigScreen(ThebombzenAPIBaseMod mod,
			GuiScreen parentScreen, ThebombzenAPIConfiguration<?> config) {
		this.mod = mod;
		title = mod.getLongName() + " Options";
		this.config = config;
		this.parentScreen = parentScreen;
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 4912) {
			mc.displayGuiScreen(this.parentScreen);
			return;
		} else if (button.id >= 4913) {
			ThebombzenAPIConfigOption option = tooltipButtons.get(button);
			if (option.getOptionType() == ThebombzenAPIConfigOption.BOOLEAN) {
				boolean newProp = !config.getPropertyBoolean(option);
				config.setProperty(option, Boolean.toString(newProp));
				button.displayString = getDisplayGuiString(option);
			} else if (option.getOptionType() == ThebombzenAPIConfigOption.FINITE_STRING) {
				String[] strings = option.getFiniteStringOptions();
				int index = Arrays.asList(strings).indexOf(
						config.getProperty(option));
				index = (index + 1) % strings.length;
				config.setProperty(option, strings[index]);
				button.displayString = getDisplayGuiString(option);
			} else if (option.getOptionType() == ThebombzenAPIConfigOption.KEY) {
				if (button != currentKeyButton) {
					if (currentKeyButton != null) {
						currentKeyButton.displayString = getDisplayGuiString(tooltipButtons
								.get(currentKeyButton));
					}
					button.displayString = option.getShortInfo() + ": > ??? <";
					currentKeyButton = button;
				}
			}
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, title, this.width / 2, 10,
				16777215);
		super.drawScreen(i, j, f);
		for (ThebombzenAPIConfigGuiButton button : tooltipButtons.keySet()) {
			button.drawTooltip(this.mc, i, j);
		}
	}

	protected String getDisplayGuiString(ThebombzenAPIConfigOption option) {
		if (option.getOptionType() == ThebombzenAPIConfigOption.BOOLEAN) {
			return option.getShortInfo() + ": "
					+ (config.getPropertyBoolean(option) ? "ON" : "OFF");
		} else {
			return option.getShortInfo() + ": " + config.getProperty(option);
		}
	}

	@Override
	public void initGui() {
		ThebombzenAPIConfigOption[] options = config.getAllOptions();
		int i = 0;
		for (ThebombzenAPIConfigOption option : options) {
			if (option.getOptionType() == ThebombzenAPIConfigOption.ARBITRARY_STRING) {
				continue;
			}
			ThebombzenAPIConfigGuiButton button = new ThebombzenAPIConfigGuiButton(
					this, 4913 + i, width / 2 - 206 + (i % 2) * 207, height / 6
							+ 23 * (i >> 1) - 18, 205, 20,
					getDisplayGuiString(option), option.getInfo());
			i++;
			this.buttonList.add(button);
			this.tooltipButtons.put(button, option);
		}
		this.buttonList.add(new GuiButton(4912, this.width / 2 - 100,
				this.height / 6 + 168, 200, 20, StatCollector
						.translateToLocal("gui.done")));
	}

	@Override
	public void keyTyped(char keyChar, int keyCode) {
		super.keyTyped(keyChar, keyCode);
		if (keyCode != 1 && currentKeyButton != null) {
			ThebombzenAPIConfigOption option = tooltipButtons
					.get(currentKeyButton);
			config.setProperty(option, Keyboard.getKeyName(keyCode));
			currentKeyButton.displayString = getDisplayGuiString(option);
			currentKeyButton = null;
		}
	}

}
