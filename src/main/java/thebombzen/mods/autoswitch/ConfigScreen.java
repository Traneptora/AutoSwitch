package thebombzen.mods.autoswitch;

import java.awt.Desktop;
import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import thebombzen.mods.thebombzenapi.ThebombzenAPIBaseMod;
import thebombzen.mods.thebombzenapi.client.ThebombzenAPIConfigScreen;

public class ConfigScreen extends ThebombzenAPIConfigScreen {

	public ConfigScreen(ThebombzenAPIBaseMod mod, GuiScreen parentScreen,
			Configuration config) {
		super(mod, parentScreen, config);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if (button.id == 4911) {
			try {
				Desktop.getDesktop().open(
						((Configuration) config).getExtraConfigFile());
			} catch (IOException e) {
				mod.throwException("Unable to open file!", e, false);
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.add(new GuiButton(4911, this.width / 2 - 100,
				this.height / 6 + 140, 200, 20,
				"Open AutoSwitch Overrides File..."));
	}

}
