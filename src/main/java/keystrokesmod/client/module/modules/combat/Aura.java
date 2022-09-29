package keystrokesmod.client.module.modules.combat;

import com.google.common.eventbus.Subscribe;
import keystrokesmod.client.event.impl.PacketEvent;
import keystrokesmod.client.event.impl.UpdateEvent;
import keystrokesmod.client.main.Raven;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.modules.movement.KeepSprint;
import keystrokesmod.client.module.setting.impl.*;
import keystrokesmod.client.utils.MillisTimer;
import keystrokesmod.client.utils.PacketUtils;
import keystrokesmod.client.utils.Utils;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import org.apache.commons.lang3.RandomUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The real shit. (this is blatant aura)
 * This entire class is work in progress, do not touch/uncomment.
 * @author sigmaclientwastaken, some stuff is blatantly stolen from Radium by nevalack
 */
/*
public class Aura extends Module {
    public static DescriptionSetting reachDesc;
    public static DoubleSliderSetting reach, aps;
    public static ComboSetting<SortingMethod> sortingMode;
    public static ComboSetting<AutoBlock> autoblockMode;
    public static SliderSetting fov;
    public static TickSetting fovCheck;

    int waitTicks;
    MillisTimer attackTimer = new MillisTimer();
    private EntityLivingBase target;
    private boolean entityInBlockRange;
    private boolean blocking;

    private static Aura instance;

    private final Map<Entity, EntityData> entityDataCache = new HashMap<>();
    private final DataSupplier dataSupplier = entity -> {
        EntityData data = new EntityData(Utils.Player.getRotationsToEntity(entity), getDistToEntity(entity.posX, entity.posY, entity.posZ));
        entityDataCache.put(entity, data);
        return data;
    };

    public Aura() {
        super("Aura", ModuleCategory.combat);

        registerSettings(
                aps = new DoubleSliderSetting("APS", 6, 8, 1, 20, 0.1),
                reachDesc = new DescriptionSetting("Min: Attack | Max: Target"),
                reach = new DoubleSliderSetting("Reach", 3, 3.3, 3, 8, 0.1),
                fovCheck = new TickSetting("FOV Check", false),
                fov = new SliderSetting("FOV", 90, 0, 180, 1)
        );

        instance = this;
    }

    @Subscribe
    public void onPacket(PacketEvent e) {

        // anti retard tm
        if(e.getPacket() instanceof C0APacketAnimation) {
            attackTimer.reset();
        }

    }

    @Subscribe
    public void onUpdate(UpdateEvent e) {
        if(e.isPre()) {
            entityDataCache.clear();

            entityInBlockRange = false;

            EntityLivingBase optimalTarget = null;

            List<EntityLivingBase> entities = Utils.Player.getLivingEntities(this::isValid);

            // don't sort if only one entity
            if (entities.size() > 1) {
                entities.sort(sortingMode.getMode().getSorter());
            }

            for (EntityLivingBase entity : entities) {
                double dist = this.computeData(entity).dist;

                if (!this.entityInBlockRange && dist < reach.getInputMax())
                    this.entityInBlockRange = true;

                if (dist < reach.getInputMin()) {
                    optimalTarget = entity;
                    break;
                }
            }

            this.target = optimalTarget;

            if (isOccupied() || checkWaitTicks())
                return;

            if (optimalTarget != null) {

                RotationUtils.rotate(event, this.computeData(optimalTarget).rotations,
                        this.maxAngleChangeProperty.getValue().floatValue(), this.lockViewProperty.getValue());

                if (this.attackMethodProperty.getValue() == AttackTiming.PRE) {
                    if (this.entityInBlockRange && this.autoblockProperty.getValue() && isHoldingSword()) {
                        unblock();
                    }
                    tryAttack(event);
                    reblock = true;
                } else {
                    if(reblock) {
                        block();
                    }
                }

            } else {

                if(blocking) {
                    unblock();
                }

            }
        }

        if (e.isPre()) {







        } else {
            if (isOccupied())
                return;

            if (this.target != null && this.attackMethodProperty.getValue() == AttackTiming.POST) {
                tryAttack(event);
            } else if(target != null) {

            }
        }
    }

    private static double getDistToEntity(double x, double y, double z) {
        double xDif = x - mc.thePlayer.posX;
        double yDif = y - mc.thePlayer.posY;
        double zDif = z - mc.thePlayer.posZ;
        return Math.sqrt(xDif * xDif + zDif * zDif + yDif * yDif);
    }

    public static boolean isAutoBlocking() {
        Aura aura = getInstance();
        return aura.isEnabled() && (autoblockMode.getMode() != AutoBlock.None && autoblockMode.getMode() != AutoBlock.Fake) && aura.entityInBlockRange;
    }

    public static boolean isBlocking() {
        Aura aura = getInstance();
        return aura.isEnabled() && aura.blocking;
    }

    public static Aura getInstance() {
        return instance;
    }

    public static double getEffectiveHealth(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            return player.getHealth() * (Utils.Player.getTotalArmorProtection(player) / 20.0);
        } else return 0;
    }

    private void tryAttack(UpdateEvent e) {
        if (isUsingItem())
            return;

        EntityPlayer localPlayer = mc.thePlayer;

        double min = aps.getInputMin();
        double max = aps.getInputMax();

        if (min > max) {
            min = max;
        }

        double cps;
        if (min == max) cps = min;
        else cps = RandomUtils.nextDouble(min, max);

        if (attackTimer.hasElapsed((long) (1000L / cps)) && isLookingAtEntity(e.getYaw(), e.getPitch())) {
            localPlayer.swingItem();
            PacketUtils.sendPacket(new C02PacketUseEntity(this.target, C02PacketUseEntity.Action.ATTACK));


            Module keepSprint = Raven.moduleManager.getModuleByClazz(KeepSprint.class);
            if (keepSprint != null && keepSprint.isEnabled()) {
                KeepSprint.slowdown(target);
            } else {
                mc.thePlayer.motionX *= 0.6D;
                mc.thePlayer.motionZ *= 0.6D;
                mc.thePlayer.setSprinting(false);
            }
        }
    }

    private boolean isLookingAtEntity(final float yaw, final float pitch) {
        double range = reach.getInputMin();
        Vec3 src = mc.thePlayer.getPositionEyes(1.0F);
        final Vec3 rotationVec = getVectorForRotation(pitch, yaw);
        Vec3 dest = src.addVector(rotationVec.xCoord * range, rotationVec.yCoord * range, rotationVec.zCoord * range);
        MovingObjectPosition obj = mc.theWorld.rayTraceBlocks(src, dest,
                false, false, true);
        if (obj == null) return false;
        if (obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (this.checksProperty.isSelected(Checks.RAYTRACE)) return false;
            else if (this.computeData(this.target).dist > this.rangeThruWalls.getValue()) return false;
        }
        if (!this.checksProperty.isSelected(Checks.ROTATION)) return true;
        return this.target.getEntityBoundingBox().expand(0.1F, 0.1F, 0.1F).calculateIntercept(src, dest) != null;
    }

    private EntityData computeData(final Entity entity) {
        final EntityData requiredData;
        final EntityData data = this.entityDataCache.getOrDefault(entity, null);
        if (data == null) requiredData = this.dataSupplier.calculate(entity);
        else requiredData = data;
        return requiredData;
    }

    private boolean fovCheck(EntityLivingBase entity, int fov) {
        final float[] rotations = this.computeData(entity).rotations;
        final EntityPlayer player = mc.thePlayer;
        final float yawChange = MathHelper.wrapAngleTo180_float(player.rotationYaw - rotations[0]);
        final float pitchChange = MathHelper.wrapAngleTo180_float(player.rotationPitch - rotations[1]);
        return Math.sqrt(yawChange * yawChange + pitchChange * pitchChange) < fov;
    }

    @Override
    public void onEnable() {
        this.entityDataCache.clear();
    }

    @Override
    public void onDisable() {
        this.target = null;
        this.entityInBlockRange = false;
    }

    private boolean isInMenu() {
        return mc.currentScreen != null;
    }

    private boolean isOccupied() {
        return isInMenu();
    }

    public EntityLivingBase getTarget() {
        return this.target;
    }

    private boolean checkWaitTicks() {
        if (waitTicks > 0) {
            waitTicks--;
            return true;
        }
        return false;
    }

    private boolean isUsingItem() {
        return mc.thePlayer.isUsingItem() && !isHoldingSword();
    }

    private boolean isHoldingSword() {
        ItemStack stack;
        return (stack = mc.thePlayer.getCurrentEquippedItem()) != null && stack.getItem() instanceof ItemSword;
    }

    private boolean isValid(EntityLivingBase entity) {
        if (this.checksProperty.isSelected(Checks.ALIVE) && !entity.isEntityAlive())
            return false;
        if (entity.isInvisible() && !this.targetsProperty.isSelected(Targets.INVISIBLES))
            return false;
        if (entity == mc.thePlayer.ridingEntity)
            return false;
        if (entity instanceof EntityOtherPlayerMP) {
            final EntityPlayer player = (EntityPlayer) entity;
            if (!this.targetsProperty.isSelected(Targets.PLAYERS))
                return false;
            if (ModuleManager.getInstance(AntiBot.class).isBot(player))
                return false;
            if (!this.targetsProperty.isSelected(Targets.TEAMMATES) && PlayerUtils.isTeamMate(player))
                return false;
            if (!targetsProperty.isSelected(Targets.FRIENDS) &&
                    RadiumClient.getInstance().getPlayerManager().isFriend(player))
                return false;
        } else if (entity instanceof EntityMob) {
            if (!this.targetsProperty.isSelected(Targets.MOBS))
                return false;
        } else if (entity instanceof EntityAnimal) {
            if (!this.targetsProperty.isSelected(Targets.ANIMALS))
                return false;
        } else {
            // Ignore any other types of entities
            return false;
        }

        return this.computeData(entity).dist <
                Math.max(reach.getInputMax(), reach.getInputMin()) &&
                (!this.checksProperty.isSelected(Checks.FOV) || this.fovCheck(entity, this.fovProperty.getValue().intValue()));
    }

    // like make this work and stuff
    private enum AutoBlock {
        None, Fake, Terrible
    }

    private enum AttackTiming {
        Pre,
        Post
    }

    private enum Targets {
        PLAYERS,
        TEAMMATES,
        FRIENDS,
        INVISIBLES,
        MOBS,
        ANIMALS
    }

    private enum Checks {
        ALIVE,
        FOV,
        ROTATION,
        RAYTRACE
    }

    private enum SortingMethod {
        Distance(new DistanceSorting()),
        Health(new HealthSorting()),
        HurtTime(new HurtTimeSorting()),
        FOV(new CrosshairSorting()),
        Combined(new CombinedSorting());

        private final Comparator<EntityLivingBase> sorter;

        SortingMethod(Comparator<EntityLivingBase> sorter) {
            this.sorter = sorter;
        }

        public Comparator<EntityLivingBase> getSorter() {
            return sorter;
        }
    }

    @FunctionalInterface
    private interface DataSupplier {
        EntityData calculate(Entity entity);
    }

    private static class EntityData {
        private final float[] rotations;
        private final double dist;

        public EntityData(float[] rotations, double dist) {
            this.rotations = rotations;
            this.dist = dist;
        }
    }

    private abstract static class AngleBasedSorting implements Comparator<EntityLivingBase> {
        protected abstract float getCurrentAngle();

        @Override
        public int compare(EntityLivingBase o1, EntityLivingBase o2) {
            float yaw = this.getCurrentAngle();

            return Double.compare(
                    Math.abs(Utils.Player.getYawToEntity(o1) - yaw),
                    Math.abs(Utils.Player.getYawToEntity(o2) - yaw));
        }
    }

    private static class CrosshairSorting extends AngleBasedSorting {
        @Override
        protected float getCurrentAngle() {
            return mc.thePlayer.rotationYaw;
        }
    }

    private static class CombinedSorting implements Comparator<EntityLivingBase> {
        @Override
        public int compare(EntityLivingBase o1, EntityLivingBase o2) {
            int t1 = 0;
            for (SortingMethod sortingMethod : SortingMethod.values()) {
                Comparator<EntityLivingBase> sorter = sortingMethod.getSorter();
                if (sorter == this) continue;
                t1 += sorter.compare(o1, o2);
            }
            return t1;
        }
    }

    private static class DistanceSorting implements Comparator<EntityLivingBase> {
        @Override
        public int compare(EntityLivingBase o1, EntityLivingBase o2) {
            return Double.compare(o1.getDistanceSqToEntity(mc.thePlayer), o1.getDistanceSqToEntity(mc.thePlayer));
        }
    }

    private static class HealthSorting implements Comparator<EntityLivingBase> {
        @Override
        public int compare(EntityLivingBase o1, EntityLivingBase o2) {
            return Double.compare(getEffectiveHealth(o1), getEffectiveHealth(o2));
        }
    }

    private static class HurtTimeSorting implements Comparator<EntityLivingBase> {
        @Override
        public int compare(EntityLivingBase o1, EntityLivingBase o2) {
            return Integer.compare(
                    20 - o2.hurtResistantTime,
                    20 - o1.hurtResistantTime);
        }
    }

    private void block() {
        if (!mc.thePlayer.isBlocking()) {
            mc.playerController.sendUseItem(mc.thePlayer,
                    mc.theWorld, mc.thePlayer.getHeldItem());
            mc.thePlayer.setItemInUse(mc.thePlayer.getHeldItem(),
                    mc.thePlayer.getHeldItem().getMaxItemUseDuration());
            blocking = true;
        }
    }

    private void unblock() {
        if (mc.thePlayer.isBlocking()) {
            PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                    BlockPos.ORIGIN, EnumFacing.UP));
            blocking = false;
        }
    }

    // taken straight from entity class fuck invokers i am too sleep-deprived
    protected final Vec3 getVectorForRotation(float p_getVectorForRotation_1_, float p_getVectorForRotation_2_) {
        float f = MathHelper.cos(-p_getVectorForRotation_2_ * 0.017453292F - 3.1415927F);
        float f1 = MathHelper.sin(-p_getVectorForRotation_2_ * 0.017453292F - 3.1415927F);
        float f2 = -MathHelper.cos(-p_getVectorForRotation_1_ * 0.017453292F);
        float f3 = MathHelper.sin(-p_getVectorForRotation_1_ * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }

}

 */
