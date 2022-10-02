package keystrokesmod.client.mixin.mixins;

import keystrokesmod.client.module.modules.render.XRay;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Block.class, priority = 1005)
public class MixinBlock {

    @Inject(method = "shouldSideBeRendered", at = @At("HEAD"), cancellable = true)
    public void shouldSideBeRendered(IBlockAccess p_shouldSideBeRendered_1_, BlockPos p_shouldSideBeRendered_2_,
                                     EnumFacing p_shouldSideBeRendered_3_, CallbackInfoReturnable<Boolean> cir) {
        if(XRay.instance.isEnabled() && XRay.hypixel.isToggled()) {
            cir.setReturnValue(XRay.isOreBlock((Block) (Object) this));
        }
    }

    @Inject(method = "getBlockLayer", at = @At("HEAD"), cancellable = true)
    public void getBlockLayer(CallbackInfoReturnable<EnumWorldBlockLayer> cir) {
        if(XRay.instance.isEnabled()) {
            cir.setReturnValue(XRay.isOreBlock((Block) (Object) this) ? EnumWorldBlockLayer.SOLID : EnumWorldBlockLayer.TRANSLUCENT);
        }
    }

    @Inject(method = "getAmbientOcclusionLightValue", at = @At("HEAD"), cancellable = true)
    public void getAmbientOcclusionLightValue(CallbackInfoReturnable<Float> cir) {
        if (XRay.instance.isEnabled()) {
            cir.setReturnValue(1F);
        }
    }

}
