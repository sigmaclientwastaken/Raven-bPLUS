package keystrokesmod.client.mixin.mixins;

import keystrokesmod.client.module.modules.combat.Aura;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * sk1er keystrokes on top
 */
@Mixin(value = ItemRenderer.class, priority = 1005)
public class MixinItemRenderer {

    @Redirect(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;getItemInUseCount()I"))
    private int mixin(AbstractClientPlayer instance) {
        return Aura.isAutoBlocking()? 1 : instance.getItemInUseCount();
    }

}
