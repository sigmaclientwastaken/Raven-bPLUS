package keystrokesmod.client.hud;

import keystrokesmod.client.module.modules.HUD;
import keystrokesmod.client.utils.enums.EnumSide;
import net.minecraft.client.Minecraft;

public abstract class HudComponent {

    protected final Minecraft mc = Minecraft.getMinecraft();

    protected int x, y;

    public abstract EnumSide getSide();
    public abstract void draw(boolean editing);
    public abstract boolean isIn(int mouseX, int mouseY);

    public void onClick(int mouseX, int mouseY) {
        if(isIn(mouseX, mouseY)) {
            HUD.getInstance().setDrag(this, mouseX-x, mouseY-y);
        }
    }

    public int getX() {
        return x;
    }

    public HudComponent setX(int x) {
        this.x = x;
        return this;
    }

    public int getY() {
        return y;
    }

    public HudComponent setY(int y) {
        this.y = y;
        return this;
    }

}
