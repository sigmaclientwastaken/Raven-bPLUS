package keystrokesmod.client.module.modules.render;

import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockRedstoneOre;

/**
 * chinese code, because god knows I can't write this shit correctly
 */
public class XRay extends Module {

    public static XRay instance;

    public static TickSetting hypixel;
    public static SliderSetting opacity;

    public XRay() {
        super("XRay", ModuleCategory.render);

        this.registerSettings(
                opacity = new SliderSetting("Opacity", 120, 0, 255, 1),
                hypixel = new TickSetting("Hypixel", true)
        );

        instance = this;

    }

    @Override
    public void onEnable() {
        mc.renderGlobal.loadRenderers();
    }

    @Override
    public void onDisable() {
        mc.renderGlobal.loadRenderers();
    }

    public static boolean isOreBlock(Block block) {
        return block instanceof BlockOre || block instanceof BlockRedstoneOre;
    }

}
