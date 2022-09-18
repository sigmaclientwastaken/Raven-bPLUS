package keystrokesmod.client.hud.impl;

import keystrokesmod.client.hud.HudComponent;
import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.modules.HUD;
import keystrokesmod.client.module.modules.client.GuiModule;
import keystrokesmod.client.utils.RenderUtils;
import keystrokesmod.client.utils.Utils;
import keystrokesmod.client.utils.enums.EnumSide;
import keystrokesmod.client.utils.font.FontUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;

public class WatermarkComponent extends HudComponent {

    /*
    terrible code
    TODO: optimize
     */

    @Override
    public EnumSide getSide() {
        return EnumSide.getSide(x, new ScaledResolution(mc));
    }

    @Override
    public void draw(boolean editing) {
        HUD hud = HUD.getInstance();

        if(editing) {
            int width = 0;
            int height = 0;

            switch (HUD.watermarkMode.getMode()) {

                case Normal:
                    if (HUD.generalMcFont.isToggled()) {
                        width = mc.fontRendererObj.getStringWidth(hud.clientName);
                        height = mc.fontRendererObj.FONT_HEIGHT;
                    } else {
                        width = (int) FontUtil.normal.getStringWidth(hud.clientName);
                        height = FontUtil.normal.getHeight();
                    }
                    break;

            }

            Gui.drawRect(x, y, x + width, y + height, 0x50000000);
        }

        int color = -1;

        switch (HUD.watermarkColorMode.getMode()) {

            case Static:
                color = HUD.generalColor.getRGB();
                break;

            case Rainbow:
                color = RenderUtils.hsbRainbow((float) HUD.generalRainbowSaturation.getInput(),(float) HUD.generalRainbowBrightness.getInput(), 0,
                        (int) HUD.generalRainbowSeconds.getInput());
                break;

        }

        String clientName = hud.clientName;

        if(HUD.watermarkOnlyFirstChar.isToggled()) {
            clientName = clientName.substring(0, 1) + EnumChatFormatting.WHITE + clientName.substring(1);
        }

        switch (HUD.watermarkMode.getMode()) {

            case Normal:
                if (HUD.generalMcFont.isToggled()) {
                    mc.fontRendererObj.drawStringWithShadow(clientName, x, y, color);
                } else {
                    FontUtil.normal.drawSmoothString(clientName, x, y, color);
                }
                break;

        }

    }

    @Override
    public boolean isIn(int mouseX, int mouseY) {
        HUD hud = HUD.getInstance();
        switch (HUD.watermarkMode.getMode()) {

            case Normal:
                if (HUD.generalMcFont.isToggled()) {
                    return mouseX > x && mouseX < x + mc.fontRendererObj.getStringWidth(hud.clientName) && mouseY > y && mouseY < y + mc.fontRendererObj.FONT_HEIGHT;
                } else {
                    return mouseX > x && mouseX < x + FontUtil.normal.getStringWidth(hud.clientName) && mouseY > y && mouseY < y + FontUtil.normal.getHeight();
                }

        }
        return false;
    }

}
