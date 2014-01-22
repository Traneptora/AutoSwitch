package thebombzen.mods.thebombzenapi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ThebombzenAPIConfigGuiButton extends GuiButton {

	private GuiScreen parentScreen;
	private String[] toolTips;
	private int toolTipWidth = -1;

	public ThebombzenAPIConfigGuiButton(GuiScreen parentScreen, int id, int x,
			int y, int width, int height, String displayString,
			String... tooltips) {
		super(id, x, y, width, height, displayString);
		this.toolTips = tooltips;
		this.parentScreen = parentScreen;
	}

	public ThebombzenAPIConfigGuiButton(GuiScreen parentScreen, int id, int x,
			int y, String displayString, String... tooltips) {
		super(id, x, y, displayString);
		this.toolTips = tooltips;
		this.parentScreen = parentScreen;
	}

	public void drawTooltip(Minecraft minecraft, int i, int j) {
		if (i >= xPosition && j >= yPosition && i < (xPosition + width)
				&& j < (yPosition + height)) {
			FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

			int lineHeight = fontRenderer.FONT_HEIGHT + 3;

			int x = i + 12, y = j - lineHeight * toolTips.length;
			if (toolTipWidth == -1) {
				for (String line : toolTips) {
					toolTipWidth = Math.max(toolTipWidth,
							fontRenderer.getStringWidth(line));
				}
			}

			if (x + toolTipWidth >= parentScreen.width) {
				x -= toolTipWidth + 24;
			}

			if (y < 3) {
				y += lineHeight * toolTips.length;
			}

			drawGradientRect(x - 3, y - 3, x + toolTipWidth + 3, y + lineHeight
					* toolTips.length, 0xc0000000, 0xc0000000);

			for (int index = 0; index < toolTips.length; index++) {
				fontRenderer.drawStringWithShadow(toolTips[index], x, y + index
						* lineHeight, -1);
			}
		}
	}

}
