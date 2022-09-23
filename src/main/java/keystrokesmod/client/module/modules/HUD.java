package keystrokesmod.client.module.modules;

import com.google.common.eventbus.Subscribe;
import keystrokesmod.client.event.impl.Render2DEvent;
import keystrokesmod.client.hud.HudComponent;
import keystrokesmod.client.hud.impl.WatermarkComponent;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.*;
import keystrokesmod.client.utils.ColorSupplier;
import keystrokesmod.client.utils.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import org.fusesource.jansi.Ansi;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static net.minecraft.util.EnumChatFormatting.*;

public class HUD extends Module {
    public static ComboSetting<WatermarkMode> watermarkMode;
    public static ComboSetting<ColorMode> watermarkColorMode;
    public static TickSetting watermarkOnlyFirstChar;

    public static TickSetting generalMcFont;
    public static TickSetting generalMcFontShadow;
    public static SliderSetting generalRainbowSaturation;
    public static SliderSetting generalRainbowBrightness;
    public static SliderSetting generalRainbowSeconds;
    public static SliderSetting generalRainbowOffset;
    public static RGBSetting generalColor;

    private static HUD instance;

    public final String clientName = "Raven";

    // the nightmare
    private HudComponent dragging;
    private int dragX, dragY;

    public final Map<Class<? extends HudComponent>, HudComponent> comps = new HashMap<>();

    public HUD() {
        super("HUD", ModuleCategory.render);
        showInHud = false;
        instance = this;

        this.registerSettings(
                new DescriptionSetting(GRAY + "Watermark Settings"), // register watermark settings with gray + reset at the end to prevent duplicate names
                watermarkMode = new ComboSetting<>("Mode" + GRAY + RESET, WatermarkMode.Normal),
                watermarkColorMode = new ComboSetting<>("Color Mode" + GRAY + RESET, ColorMode.Static),
                watermarkOnlyFirstChar = new TickSetting("Only Color First Char" + GRAY + RESET, true),
                new DescriptionSetting(" "),
                new DescriptionSetting(GRAY + "General Settings"), // register general settings with red + reset at the end to prevent duplicate names
                generalMcFont = new TickSetting("Minecraft Font" + RED + RESET, true),
                generalMcFontShadow = new TickSetting("MC Font Shadow"  + RED + RESET, true),
                generalColor = new RGBSetting("Color" + RED + RESET, 107, 105, 214),
                generalRainbowBrightness = new SliderSetting("Rainbow Brightness" + RED + RESET, 1, 0, 1, 0.01),
                generalRainbowSaturation = new SliderSetting("Rainbow Saturation" + RED + RESET, 1, 0, 1, 0.01),
                generalRainbowSeconds = new SliderSetting("Rainbow Seconds" + RED + RESET, 4, 1, 10, 1),
                generalRainbowOffset = new SliderSetting("Rainbow Offset" + RED + RESET, 150, 100, 1000, 10)
        );

        add(new WatermarkComponent());

    }

    @Subscribe
    public void onRender2D(Render2DEvent e) {
        if(mc.thePlayer != null && mc.theWorld != null) {
            comps.forEach((compC, comp) -> {
                GlStateManager.resetColor();
                comp.draw(false);
            });
        }
    }

    private void add(HudComponent comp) {
        comps.put(comp.getClass(), comp);
    }

    /**
     * why? because this is needed multiple times per frame
     */
    public static HUD getInstance() {
        return instance;
    }

    public void setDrag(HudComponent comp, int dragX, int dragY) {
        getInstance().dragging = comp;
        getInstance().dragX = dragX;
        getInstance().dragY = dragY;
    }

    public void updateDrag(int mouseX, int mouseY) {
        if(dragging != null) {
            dragging.setX(mouseX - dragX).setY(mouseY - dragY);
        }
    }

    public void endDrag(int mouseX, int mouseY) {
        updateDrag(mouseX, mouseY);
        getInstance().dragging = null;
    }

    public void handleClick(int mouseX, int mouseY, int mouseButton) {
        if(mouseButton == 0) {
            comps.forEach((compC, comp) -> {
                comp.onClick(mouseX, mouseY);
            });
        }
    }

    public enum WatermarkMode {
        Normal, // done
        CSGO,
        Power
    }

    public enum ColorMode {
        Static((in) -> generalColor.getRGB()),
        Fade((in) -> RenderUtils.blend(generalColor.getColor(), generalColor.getColor().darker(), in/100D).getRGB()),
        Rainbow((in) -> RenderUtils.hsbRainbow((float) generalRainbowSaturation.getInput(),(float) generalRainbowBrightness.getInput(),
                (int) (in * generalRainbowOffset.getInput()), (int) generalRainbowSeconds.getInput())),
        Astolfo((in) -> -1);

        private final ColorSupplier color;

        ColorMode(ColorSupplier color) {
            this.color = color;
        }

        public ColorSupplier getColor() {
            return color;
        }

    }

    public static class HudFontUtils {

        private final HUD hud;

        public HudFontUtils(HUD hud) {
            this.hud = hud;
        }

    }

}