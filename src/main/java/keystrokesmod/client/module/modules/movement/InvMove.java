package keystrokesmod.client.module.modules.movement;

import com.google.common.eventbus.Subscribe;
import keystrokesmod.client.clickgui.raven.ClickGui;
import keystrokesmod.client.event.impl.TickEvent;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.ComboSetting;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

public class InvMove extends Module {

    private final DescriptionSetting ds;
    private final ComboSetting<Mode> mode;

    public InvMove() {
        super("InvMove", ModuleCategory.movement);
        registerSetting(mode = new ComboSetting<>("Mode", Mode.Vanilla));
        registerSetting(ds = new DescriptionSetting("Vanilla does NOT work on Hypixel!"));
    }

    @Subscribe
    public void onTick(TickEvent e) {
        if (mc.currentScreen != null) {
            if (mc.currentScreen instanceof GuiChat) {
                return;
            }

            boolean doReturn = false;

            switch (mode.getMode()) {
                case Vanilla:
                    break;
                case Undetectable:
                    if(!(mc.currentScreen instanceof ClickGui)) {
                        doReturn = true;
                    }

                    break;
                case Test:
                    if(mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory)) {
                        doReturn = true;
                    }

                    break;
            }

            if(doReturn) return;

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(),
                    Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(),
                    Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()));
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(),
                    Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()));
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(),
                    Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()));
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(),
                    Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()));
            // EntityPlayerSP var1;

            /* no.
            if (Keyboard.isKeyDown(208) && mc.thePlayer.rotationPitch < 90.0F) {
                var1 = mc.thePlayer;
                var1.rotationPitch += 6.0F;
            }

            if (Keyboard.isKeyDown(200) && mc.thePlayer.rotationPitch > -90.0F) {
                var1 = mc.thePlayer;
                var1.rotationPitch -= 6.0F;
            }

            if (Keyboard.isKeyDown(205)) {
                var1 = mc.thePlayer;
                var1.rotationYaw += 6.0F;
            }

            if (Keyboard.isKeyDown(203)) {
                var1 = mc.thePlayer;
                var1.rotationYaw -= 6.0F;
            }
             */
        }

    }

    public enum Mode {
        Vanilla, Undetectable, Test
    }
}
