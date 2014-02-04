package thebombzen.mods.autoswitch;

import java.awt.Desktop;
import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import thebombzen.mods.thebombzenapi.client.ThebombzenAPIConfigScreen;

public class ConfigScreen extends ThebombzenAPIConfigScreen {

	public ConfigScreen(GuiScreen parentScreen) {
		super(AutoSwitch.instance, parentScreen, AutoSwitch.instance.getConfiguration());
	}

	@Override
	protected void func_146284_a(GuiButton button) {
		super.func_146284_a(button);
		// field_146127_k == id
		if (button.field_146127_k == 4911) {
			try {
				Desktop.getDesktop().open(((Configuration) config).getExtraConfigFile());
			} catch (IOException e) {
				mod.throwException("Unable to open file!", e, false);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		this.field_146292_n.add(new GuiButton(4911, this.field_146294_l / 2 - 100,
				this.field_146295_m / 6 + 140, 200, 20,
				"Open AutoSwitch Overrides File..."));
	}

}
