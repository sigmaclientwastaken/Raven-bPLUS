package keystrokesmod.client.hud.impl;

import keystrokesmod.client.hud.HudComponent;
import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.modules.HUD;
import keystrokesmod.client.utils.ColorSupplier;
import keystrokesmod.client.utils.RenderUtils;
import keystrokesmod.client.utils.enums.EnumSide;
import keystrokesmod.client.utils.font.FontUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

public class ArraylistComponent extends HudComponent {

    /*
    terrible code + not done
    TODO: finish and optimize
     */

    @Override
    public EnumSide getSide() {
        return EnumSide.getSide(x, new ScaledResolution(mc));
    }

    @Override
    public void draw(boolean editing) {
        HUD hud = HUD.getInstance();

        ColorSupplier color = null;

        switch (HUD.watermarkColorMode.getMode()) {

            case Static:
                color = HUD.ColorMode.Static.getColor();
                break;

            case Rainbow:
                color = HUD.ColorMode.Rainbow.getColor();
                break;

            case Fade:
                color = HUD.ColorMode.Fade.getColor();
                break;

            case Astolfo:
                color = HUD.ColorMode.Astolfo.getColor();
                break;

        }

        if(color == null)
            return;

        List<String> modules = new ArrayList<>();
        Raven.moduleManager.getEnabledModules().forEach(module -> modules.add(module.getName()));

        EnumSide side = getSide();

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
