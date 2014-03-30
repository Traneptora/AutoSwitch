package thebombzen.mods.autoswitch.configuration;

import java.awt.Desktop;
import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import thebombzen.mods.autoswitch.AutoSwitch;
import thebombzen.mods.thebombzenapi.client.ThebombzenAPIConfigScreen;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConfigScreen extends ThebombzenAPIConfigScreen {

	public ConfigScreen(GuiScreen parentScreen) {
		super(AutoSwitch.instance, parentScreen, AutoSwitch.instance.getConfiguration());
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		// field_146127_k == id
		if (button.id == 4911) {
			try {
				try {
					Desktop.getDesktop().edit(((Configuration)config).getExtraConfigFile());
				} catch (UnsupportedOperationException e){
					Desktop.getDesktop().open(((Configuration)config).getExtraConfigFile());
				}
			} catch (IOException e) {
				mod.throwException("Unable to open file!", e, false);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.add(new GuiButton(4911, this.width / 2 - 100,
				this.height / 6 + 140, 200, 20,
				"Open AutoSwitch Overrides File..."));
	}

}
