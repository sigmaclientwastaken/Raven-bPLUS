package keystrokesmod.client.utils.enums;

import net.minecraft.client.gui.ScaledResolution;

public enum EnumSide {
    LEFT, RIGHT;

    public static EnumSide getSide(int x, ScaledResolution sr) {
        if(sr.getScaledWidth()/2f < x) {
            return RIGHT;
        } else {
            return LEFT;
        }
    }
}
