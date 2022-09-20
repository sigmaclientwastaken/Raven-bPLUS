package keystrokesmod.client.mixin.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(C0DPacketCloseWindow.class)
public interface IC0DPacketCloseWindow {

    @Accessor
    int getWindowId();

}
