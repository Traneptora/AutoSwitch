/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thebombzen.mods.thebombzenapi.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import thebombzen.mods.thebombzenapi.ThebombzenAPIBaseMod;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ThebombzenAPIConfigOpenButton extends GuiButton {

	public final GuiScreen containingScreen;
	private ThebombzenAPIBaseMod mod;

	public ThebombzenAPIConfigOpenButton(GuiScreen screen, int id, int x,
			int y, int w, int h, ThebombzenAPIBaseMod mod, String displayText) {
		super(id, x, y, w, h, displayText);
		this.containingScreen = screen;
		this.mod = mod;
	}

	public ThebombzenAPIBaseMod getMod() {
		return mod;
	}

}
