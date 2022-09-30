package keystrokesmod.client.module;

import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module.ModuleCategory;
import keystrokesmod.client.module.modules.HUD;
import keystrokesmod.client.module.modules.client.GuiModule;
import keystrokesmod.client.module.modules.client.*;
import keystrokesmod.client.module.modules.combat.*;
import keystrokesmod.client.module.modules.config.ConfigSettings;
import keystrokesmod.client.module.modules.hotkey.*;
import keystrokesmod.client.module.modules.minigames.*;
import keystrokesmod.client.module.modules.minigames.Sumo.SumoBot;
import keystrokesmod.client.module.modules.minigames.Sumo.SumoClicker;
import keystrokesmod.client.module.modules.movement.*;
import keystrokesmod.client.module.modules.other.*;
import keystrokesmod.client.module.modules.player.*;
import keystrokesmod.client.module.modules.render.*;
import keystrokesmod.client.module.modules.world.AntiBot;
import keystrokesmod.client.module.modules.world.ChatLogger;
import keystrokesmod.client.module.modules.world.Scaffold;
import net.minecraft.client.gui.FontRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModuleManager {
    private Map<Class<? extends Module>, Module> modules = new HashMap<>();

    public static boolean initialized;
    public GuiModuleManager guiModuleManager;

    public ModuleManager() {
        System.out.println(ModuleCategory.values());
        if(initialized)
            return;
        this.guiModuleManager = new GuiModuleManager();
        addModule(new ChestStealer());
        addModule(new AutoArmour());
        addModule(new LeftClicker());
        addModule(new RightClicker());
        addModule(new AimAssist());
        addModule(new ClickAssist());
        addModule(new DelayRemover());
        addModule(new HitBox());
        addModule(new Reach());
        addModule(new Velocity());
        addModule(new Boost());
        addModule(new Fly());
        addModule(new InvMove());
        addModule(new KeepSprint());
        addModule(new NoSlow());
        addModule(new Sprint());
        addModule(new StopMotion());
        addModule(new LegitSpeed());
        addModule(new Timer());
        addModule(new VClip());
        addModule(new AutoJump());
        addModule(new AutoPlace());
        addModule(new BedAura());
        addModule(new FallSpeed());
        addModule(new FastPlace());
        addModule(new Freecam());
        addModule(new NoFall());
        addModule(new SafeWalk());
        addModule(new AntiBot());
        addModule(new AntiShuffle());
        addModule(new Chams());
        addModule(new ChestESP());
        addModule(new Nametags());
        addModule(new PlayerESP());
        addModule(new Tracers());
        addModule(new HUD());
        addModule(new BridgeInfo());
        addModule(new DuelsStats());
        addModule(new MurderMystery());
        addModule(new SumoFences());
        addModule(new SlyPort());
        addModule(new FakeChat());
        addModule(new NameHider());
        addModule(new WaterBucket());
        // addModule(new AutoConfig());
        addModule(new Terminal());
        addModule(new GuiModule());
        addModule(new SelfDestruct());
        addModule(new ChatLogger());
        addModule(new BridgeAssist());
        addModule(new Fullbright());
        addModule(new UpdateCheck());
        addModule(new AutoHeader());
        addModule(new AutoTool());
        addModule(new Blocks());
        addModule(new Ladders());
        addModule(new Weapon());
        addModule(new Pearl());
        addModule(new Armour());
        addModule(new Healing());
        addModule(new Trajectories());
        addModule(new WTap());
        addModule(new BlockHit());
        addModule(new STap());
        addModule(new AutoWeapon());
        addModule(new BedwarsOverlay());

        addModule(new ShiftTap());
        addModule(new FPSSpoofer());

        addModule(new AutoBlock());
        addModule(new MiddleClick());
        addModule(new Projectiles());
        addModule(new FakeHud());
        addModule(new ConfigSettings());
        addModule(new SumoBot());
        addModule(new SumoClicker());
        addModule(new Parkour());
        addModule(new Disabler());
        addModule(new JumpReset());
        addModule(new KvAura());
        addModule(new Spin());
        addModule(new Scaffold());
        //addModule(new SpeedTest());
        //addModule(new LegitAura());
        //addModule(new TargetHUD());
        // why ?
        // idk dude. you tell me why. I am pretty sure this was blowsy's work.
        initialized = true;
    }

    public void addModule(Module m) {
        modules.put(m.getClass(), m);
    }

    public void removeModuleByName(String s) {
        Module m = getModuleByName(s);
        modules.remove(m.getClass());
        m.component.category.r3nd3r();
    }

    // prefer using getModuleByClazz();
    // ok might add in 1.0.18
    public Module getModuleByName(String name) {
        if (!initialized)
            return null;

        for (Module module : modules.values()) {
            if (module.getName().replaceAll(" ", "").equalsIgnoreCase(name) || module.getName().equalsIgnoreCase(name))
                return module;
        }
        return null;
    }

    public Module getModuleByClazz(Class<? extends Module> c) {
        if (!initialized)
            return null;

        return modules.get(c);
    }

    public List<Module> getEnabledModules() {
        return modules.values().stream().filter(Module::isEnabled).collect(Collectors.toList());
    }

    public List<Module> getModules() {
        ArrayList<Module> allModules = new ArrayList<>(modules.values());
        try {
            allModules.addAll(Raven.configManager.configModuleManager.getConfigModules());
        } catch (NullPointerException ignored) {
        }
        try {
            allModules.addAll(guiModuleManager.getModules());
        } catch (NullPointerException ignored) {
        }
        return allModules;
    }

    public List<Module> getConfigModules() {
        List<Module> modulesOfC = new ArrayList<>();

        for (Module mod : getModules()) {
            if (!mod.isClientConfig()) {
                modulesOfC.add(mod);
            }
        }

        return modulesOfC;
    }

    public List<Module> getClientConfigModules() {
        List<Module> modulesOfCC = new ArrayList<>();

        for (Module mod : getModules()) {
            if (mod.isClientConfig()) {
                modulesOfCC.add(mod);
            }
        }

        return modulesOfCC;
    }

    public List<Module> getModulesInCategory(Module.ModuleCategory categ) {
        ArrayList<Module> modulesOfCat = new ArrayList<>();

        for (Module mod : getModules()) {
            if (mod.moduleCategory().equals(categ)) {
                modulesOfCat.add(mod);
            }
        }

        return modulesOfCat;
    }

    public int numberOfModules() {
        return modules.size();
    }

    public int getLongestActiveModule(FontRenderer fr) {
        int length = 0;
        for (Module mod : modules.values()) {
            if (mod.isEnabled()) {
                if (fr.getStringWidth(mod.getName()) > length) {
                    length = fr.getStringWidth(mod.getName());
                }
            }
        }
        return length;
    }

    public int getBoxHeight(FontRenderer fr, int margin) {
        int length = 0;
        for (Module mod : modules.values()) {
            if (mod.isEnabled()) {
                length += fr.FONT_HEIGHT + margin;
            }
        }
        return length;
    }

}
