package keystrokesmod.client.mixin.mixins;

import keystrokesmod.client.module.modules.HUD;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(priority = 1005, value = GuiScreen.class)
public class MixinGuiScreen {

    /*
    fuck you mixin why cant i inject into method from super like a normal person
     */

    @Inject(method = "mouseReleased", at = @At("RETURN"))
    public void kys(int p_mouseReleased_1_, int p_mouseReleased_2_, int p_mouseReleased_3_, CallbackInfo ci) {
        if(((GuiScreen) ((Object) this)) instanceof GuiChat && p_mouseReleased_3_ == 0) {
            HUD.getInstance().endDrag(p_mouseReleased_1_, p_mouseReleased_2_);
        }
    }

}
