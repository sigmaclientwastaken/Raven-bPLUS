package keystrokesmod.client.mixin.mixins;

import keystrokesmod.client.event.impl.Render2DEvent;
import keystrokesmod.client.main.Raven;
import net.minecraft.client.gui.GuiSpectator;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiSpectator.class, priority = 1005)
public class MixinGuiSpectator {

    @Inject(method = "renderTooltip", at = @At("HEAD"))
    public void onRender(ScaledResolution p_renderTooltip_1_, float p_renderTooltip_2_, CallbackInfo ci) {
        Raven.eventBus.post(new Render2DEvent());
    }

}
