package keystrokesmod.client.module.modules.other;

import com.google.common.eventbus.Subscribe;
import keystrokesmod.client.event.impl.PacketEvent;
import keystrokesmod.client.event.impl.UpdateEvent;
import keystrokesmod.client.mixin.mixins.IC0DPacketCloseWindow;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.ComboSetting;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.DoubleSliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.PacketUtils;
import keystrokesmod.client.utils.Utils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumChatFormatting;

import java.util.LinkedList;

public class Disabler extends Module {
    public static DescriptionSetting warning, mmcSafeWarning1, mmcSafeWarning2;
    public static ComboSetting mode;
    public static DoubleSliderSetting mmcSafeDelay;
    public static TickSetting hypSilentInv, hypSprintFix;

    LinkedList<Packet<?>> mmcPackets = new LinkedList<>();
    boolean mmc;
    boolean idfk;
    boolean idfk2;

    public Disabler() {
        super("Disabler", ModuleCategory.other);

        this.registerSetting(warning = new DescriptionSetting("WILL BAN DONT USE ON MAIN"));
        this.registerSetting(mode = new ComboSetting("Mode", Mode.MMCSafe));
        this.registerSetting(new DescriptionSetting("   "));
        this.registerSetting(
                mmcSafeWarning1 = new DescriptionSetting(EnumChatFormatting.GRAY + "Difference between min and max"));
        this.registerSetting(
                mmcSafeWarning2 = new DescriptionSetting(EnumChatFormatting.GRAY + "should be less than 5."));
        this.registerSetting(mmcSafeDelay = new DoubleSliderSetting("MMCSafe Delay", 77, 80, 10, 200, 1));
        this.registerSetting(new DescriptionSetting("    "));
        this.registerSettings(
                new DescriptionSetting("InvMove bypass, might flag."),
                hypSilentInv = new TickSetting("Hypixel Silent Inv", false),
                new DescriptionSetting("have this on."),
                hypSprintFix = new TickSetting("Hypixel Sprint Fix", true)
        );

        this.registerSetting(new DescriptionSetting(
                EnumChatFormatting.BOLD.toString() + EnumChatFormatting.AQUA + "GHOST CLIENT WITH DISABLER OP"));

    }

    @Override
    public void onEnable() {
        mmcPackets.clear();
    }

    @Subscribe
    public void onUpdate(UpdateEvent e) {
        if(mc.thePlayer.ticksExisted <= 1) {
            idfk = false;
            idfk2 = false;
        }
    }

    @Subscribe
    public void onPacket(PacketEvent e) {
        switch ((Mode) mode.getMode()) {
            case MMCSafe:
                if (e.isOutgoing() && !mmc) {
                    if (e.getPacket() instanceof C00PacketKeepAlive) {
                        mmcPackets.add(e.getPacket());
                        e.cancel();
                    }

                    if (e.getPacket() instanceof C0FPacketConfirmTransaction) {
                        mmcPackets.add(e.getPacket());
                        e.cancel();
                    }

                    int packetsCap = Utils.Java.randomInt(mmcSafeDelay.getInputMin(), mmcSafeDelay.getMax());

                    while (mmcPackets.size() >= packetsCap) {
                        mmc = true;
                        mc.thePlayer.sendQueue.addToSendQueue(mmcPackets.poll());
                    }
                    mmc = false;
                }
                break;

            case Hypixel:
                if (hypSilentInv.isToggled()) {
                    if((e.getPacket() instanceof C16PacketClientStatus &&
                                    ((C16PacketClientStatus) e.getPacket()).getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT)
                                    || (e.getPacket() instanceof C0DPacketCloseWindow && ((IC0DPacketCloseWindow) e.getPacket()).getWindowId()
                                    == mc.thePlayer.inventoryContainer.windowId)) {
                        e.cancel();
                    }

                    if(e.getPacket() instanceof C0EPacketClickWindow) {
                        if(((C0EPacketClickWindow) e.getPacket()).getWindowId() == mc.thePlayer.inventoryContainer.windowId) {
                            e.cancel();

                            if(idfk) {
                                PacketUtils.sendPacketSilent(new C0BPacketEntityAction());
                            }

                            PacketUtils.sendPacketSilent(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                            PacketUtils.sendPacketSilent(e.getPacket());
                            PacketUtils.sendPacketSilent(new C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId));
                        }
                    }
                }

                if(e.getPacket() instanceof C0BPacketEntityAction) {
                    boolean die = false;

                    switch(((C0BPacketEntityAction) e.getPacket()).getAction()) {
                        case STOP_SNEAKING:
                            if(!idfk2) {
                                die = true;
                            }
                            idfk2 = false;
                            break;

                        case START_SNEAKING:
                            if(idfk2) {
                                die = true;
                            }
                            idfk2 = true;
                            break;
                        case STOP_SPRINTING:
                            if(!idfk) {
                                die = true;
                            }
                            idfk = false;
                            break;

                        case START_SPRINTING:
                            if(idfk) {
                                die = true;
                            }
                            idfk = true;
                            break;
                    }

                    if(die && hypSprintFix.isToggled()) {
                        e.cancel();
                    }
                }
                break;
        }
    }

    public enum Mode {
        MMCSafe, // doesnt work
        Hypixel // SEX????????????????????????
    }
}
