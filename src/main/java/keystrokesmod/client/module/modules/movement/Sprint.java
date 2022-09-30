package keystrokesmod.client.module.modules.movement;

import com.google.common.eventbus.Subscribe;
import keystrokesmod.client.event.impl.TickEvent;
import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.modules.world.Scaffold;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.Utils;
import net.minecraft.client.settings.KeyBinding;

public class Sprint extends Module {
    public static TickSetting multiDir, ignoreBlindness;

    public Sprint() {
        super("Sprint", ModuleCategory.movement);
        this.registerSetting(multiDir = new TickSetting("All Directions", false));
        this.registerSetting(ignoreBlindness = new TickSetting("Ignore Blindness", false));
    }

    @Subscribe
    public void p(TickEvent e) {
        KeyBinding.setKeyBindState(
                mc.gameSettings.keyBindSprint.getKeyCode(),

                Utils.Player.isPlayerInGame() && mc.inGameHasFocus &&
                        !(Raven.moduleManager.getModuleByClazz(Scaffold.class).isEnabled() && !Scaffold.sprint.isToggled())
        );
    }

}
