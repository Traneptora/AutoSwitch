package thebombzen.mods.thebombzenapi.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import thebombzen.mods.thebombzenapi.ThebombzenAPIBaseMod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ThebombzenAPIConfigOpenScreen extends GuiScreen {

	public static final String screenTitle = "Options";

	/**
	 * Fired when a control is clicked. This is the equivalent of
	 * ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton guiButton) {
		if (guiButton.enabled) {
			if (guiButton.id == 1735) {
				mc.displayGuiScreen(null);
			} else if (guiButton.id > 1735
					&& guiButton instanceof ThebombzenAPIConfigOpenButton) {
				ThebombzenAPIConfigOpenButton button = (ThebombzenAPIConfigOpenButton) guiButton;
				GuiScreen screen = button.getMod().createConfigScreen(this);
				mc.displayGuiScreen(screen);
			}
		}
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer,
				ThebombzenAPIConfigOpenScreen.screenTitle, this.width / 2, 10,
				16777215);
		super.drawScreen(par1, par2, par3);
	}

	@Override
	public void initGui() {
		ThebombzenAPIBaseMod[] mods = ThebombzenAPI.getMods();
		for (int i = 0; i < mods.length; i++) {
			if (mods[i].hasConfigScreen()) {
				this.buttonList.add(new ThebombzenAPIConfigOpenButton(this,
						1736 + i, (this.width - 200) / 2,
						(this.height / 6 + i * 30), 200, 20, mods[i], mods[i]
								.getLongName()));
			}
		}
		this.buttonList.add(new ThebombzenAPIConfigOpenButton(this, 1735,
				this.width / 2 - 100, this.height / 6 + 168, 200, 20, null,
				StatCollector.translateToLocal("gui.done")));
	}
}
