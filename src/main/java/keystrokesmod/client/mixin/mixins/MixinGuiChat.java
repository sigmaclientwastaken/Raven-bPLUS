package keystrokesmod.client.mixin.mixins;

import keystrokesmod.client.module.modules.HUD;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// most useless mixin ever but better than "hud" "editor"
// ah shit theres no mouseReleased in GuiChat
@Mixin(priority = 1005, value = GuiChat.class)
public class MixinGuiChat {

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    public void mouseClicked(int p_mouseClicked_1_, int p_mouseClicked_2_, int p_mouseClicked_3_, CallbackInfo ci) {
        HUD.getInstance().handleClick(p_mouseClicked_1_, p_mouseClicked_2_, p_mouseClicked_3_);
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    public void kms(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_, CallbackInfo ci) {
        HUD.getInstance().comps.forEach((compC, comp) -> {
            GlStateManager.resetColor();
            comp.draw(true);
        });

    }

}
