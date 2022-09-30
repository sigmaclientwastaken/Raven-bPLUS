package keystrokesmod.client.module.modules.world;

import com.google.common.eventbus.Subscribe;
import keystrokesmod.client.event.impl.MoveEvent;
import keystrokesmod.client.event.impl.Render2DEvent;
import keystrokesmod.client.event.impl.UpdateEvent;
import keystrokesmod.client.module.Module;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.InventoryUtils;
import keystrokesmod.client.utils.MillisTimer;
import keystrokesmod.client.utils.PacketUtils;
import keystrokesmod.client.utils.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.apache.commons.lang3.RandomUtils;

/**
 * radium
 */
public class Scaffold extends Module {

    private static final BlockPos[] BLOCK_POSITIONS = new BlockPos[]
            {
                new BlockPos(-1, 0, 0),
                new BlockPos(1, 0, 0),
                new BlockPos(0, 0, -1),
                new BlockPos(0, 0, 1)
            };

    private static final EnumFacing[] FACINGS = new EnumFacing[]
            {
                EnumFacing.EAST,
                EnumFacing.WEST,
                EnumFacing.SOUTH,
                EnumFacing.NORTH
            };

    private static Scaffold instance;
    private int slot;

    private float lastYaw, lastPitch;

    public static TickSetting tower, sprint, safewalk, swing, keepY;
    public static SliderSetting speed, blockSlot, delay;

    public Scaffold() {
        super("Scaffold", ModuleCategory.world);
        this.registerSettings(
                tower = new TickSetting("Tower", true),
                sprint = new TickSetting("Allow Sprint", false),
                safewalk = new TickSetting("Safewalk", true),
                swing = new TickSetting("Show Swing", false),
                keepY = new TickSetting("Keep Y", false),
                speed = new SliderSetting("Move Speed", 1, 0.5, 2, 0.05),
                blockSlot = new SliderSetting("Block Slot", 9, 1, 9, 1),
                delay = new SliderSetting("Delay Ticks", 3, 0, 15, 1)
        );

        instance = this;
    }

    @Override
    public void onEnable() {
        blockCount = 0;
        placeCounter = 0;
        ticksSincePlace = 0;
        lastPos = (int) mc.thePlayer.posY;
        originalHotBarSlot = mc.thePlayer.inventory.currentItem;
        lastYaw = mc.thePlayer.prevRotationYaw;
        lastPitch = mc.thePlayer.prevRotationYaw;
    }

    @Override
    public void onDisable() {
        angles = null;
        mc.thePlayer.inventory.currentItem = originalHotBarSlot;
    }

    private final MillisTimer clickTimer = new MillisTimer();

    private int blockCount;
    private int originalHotBarSlot;
    private int bestBlockStack;
    private BlockData data;
    private float[] angles;
    private int placeCounter;
    private int ticksSincePlace;
    private int lastPos;
    private boolean towering;

    @Subscribe
    public void onMove(MoveEvent e) {
        e.setX(e.getX()*speed.getInput());
        e.setZ(e.getZ()*speed.getInput());
    }

    @Subscribe
    public void onRender2D(Render2DEvent e) {
        ScaledResolution sr = new ScaledResolution(mc);
        RenderUtils.drawOutlinedString(blockCount + " blocks", (int) (sr.getScaledWidth()/2f+8), (int) (sr.getScaledHeight()/2f-4), blockCount <= 16? 0xff0000 : -1, 0);
    }

    @Subscribe
    public void onUpdate(UpdateEvent e) {
        if(e.isPre()) {
            updateBlockCount();

            this.data = null;

            bestBlockStack = findBestBlockStack(InventoryUtils.ONLY_HOT_BAR_BEGIN, InventoryUtils.END);

            if (bestBlockStack == -1 && clickTimer.hasElapsed(250)) {
                bestBlockStack = findBestBlockStack(InventoryUtils.EXCLUDE_ARMOR_BEGIN, InventoryUtils.ONLY_HOT_BAR_BEGIN);

                if (bestBlockStack == -1) {
                    return;
                }

                boolean override = true;
                for (int i = InventoryUtils.END - 1; i >= InventoryUtils.ONLY_HOT_BAR_BEGIN; i--) {
                    final ItemStack stack = InventoryUtils.getStackInSlot(i);

                    if (!InventoryUtils.isValid(stack, true)) {
                        InventoryUtils.windowClick(bestBlockStack, i - InventoryUtils.ONLY_HOT_BAR_BEGIN,
                                InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                        bestBlockStack = i;
                        override = false;
                        clickTimer.reset();
                        break;
                    }
                }

                if (override) {
                    int blockSlot = (int) (Scaffold.blockSlot.getInput() - 1);
                    InventoryUtils.windowClick(bestBlockStack, blockSlot,
                            InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                    bestBlockStack = blockSlot + InventoryUtils.ONLY_HOT_BAR_BEGIN;
                    clickTimer.reset();
                }
            }

            if (bestBlockStack >= InventoryUtils.ONLY_HOT_BAR_BEGIN) {
                final BlockPos blockUnder = getBlockUnder();
                BlockData data = getBlockData(blockUnder);

                if (data == null)
                    data = getBlockData(blockUnder.add(0, -1, 0));

                if (data != null && bestBlockStack >= 36) {
                    if (validateReplaceable(data) && data.hitVec != null) {
                        angles = getRotations(data);
                    } else {
                        data = null;
                    }
                }

                if (angles != null)
                    rotate(e, angles, 30.0F, false);

                this.data = data;
            }
        } else if (data != null && bestBlockStack >= InventoryUtils.ONLY_HOT_BAR_BEGIN) {
            final EntityPlayerSP player = mc.thePlayer;

            if (++ticksSincePlace < delay.getInput()) return;

            player.inventory.currentItem = bestBlockStack - InventoryUtils.ONLY_HOT_BAR_BEGIN;
            if (mc.playerController.onPlayerRightClick(player, mc.theWorld,
                    player.getCurrentEquippedItem(),
                    data.pos, data.face, data.hitVec)) {
                placeCounter++;

                this.towering = tower.isToggled() && mc.gameSettings.keyBindJump.isKeyDown();

                if (this.towering && isDistFromGround(0.0626) &&
                        (placeCounter % 4 != 0)) {
                    player.motionY = 0.42 - 0.000454352838557992;
                }

                if (swing.isToggled()) player.swingItem();
                else PacketUtils.sendPacket(new C0APacketAnimation());

                ticksSincePlace = 0;
            }
        }

        lastYaw = e.getYaw();
        lastPitch = e.getPitch();
    }

    private static int findBestBlockStack(int start, int end) {
        int bestSlot = -1;
        int blockCount = -1;

        for (int i = end - 1; i >= start; --i) {
            ItemStack stack = InventoryUtils.getStackInSlot(i);

            if (stack != null &&
                    stack.getItem() instanceof ItemBlock &&
                    InventoryUtils.isGoodBlockStack(stack)) {
                if (stack.stackSize > blockCount) {
                    bestSlot = i;
                    blockCount = stack.stackSize;
                }
            }
        }

        return bestSlot;
    }

    private BlockPos getBlockUnder() {
        final EntityPlayerSP player = mc.thePlayer;
        final boolean useLastPos = keepY.isToggled() && !towering;
        final double playerPos = player.posY - 1.0;
        if (!useLastPos)
            lastPos = (int) player.posY;
        return new BlockPos(player.posX, useLastPos ? Math.min(lastPos, playerPos) : playerPos, player.posZ);
    }

    private static float[] getRotations(final BlockData data) {
        final EntityPlayerSP player = mc.thePlayer;

        final Vec3 hitVec = data.hitVec;

        final double xDif = hitVec.xCoord - player.posX;
        final double zDif = hitVec.zCoord - player.posZ;

        final double yDif = hitVec.yCoord - (player.posY + player.getEyeHeight());
        final double xzDist = StrictMath.sqrt(xDif * xDif + zDif * zDif);

        return new float[]{
                (float) (StrictMath.atan2(zDif, xDif) * (180.0D / StrictMath.PI)) - 90.0F,
                (float) (-(StrictMath.atan2(yDif, xzDist) * (180.0D / StrictMath.PI)))
        };
    }

    private static boolean validateBlockRange(final BlockData data) {
        final Vec3 pos = data.hitVec;
        if (pos == null)
            return false;
        final EntityPlayerSP player = mc.thePlayer;
        final double x = (pos.xCoord - player.posX);
        final double y = (pos.yCoord - (player.posY + player.getEyeHeight()));
        final double z = (pos.zCoord - player.posZ);
        return StrictMath.sqrt(x * x + y * y + z * z) <= 5.0D;
    }

    private static boolean validateReplaceable(final BlockData data) {
        final BlockPos pos = data.pos.offset(data.face);
        final World world = mc.theWorld;
        return world.getBlockState(pos)
                .getBlock()
                .isReplaceable(world, pos);
    }

    private static BlockData getBlockData(final BlockPos pos) {
        final BlockPos[] blockPositions = BLOCK_POSITIONS;
        final EnumFacing[] facings = FACINGS;
        final WorldClient world = mc.theWorld;

        // 1 of the 4 directions around player
        for (int i = 0; i < blockPositions.length; i++) {
            final BlockPos blockPos = pos.add(blockPositions[i]);
            if (InventoryUtils.isValidBlock(world.getBlockState(blockPos).getBlock(), false)) {
                final BlockData data = new BlockData(blockPos, facings[i]);
                if (validateBlockRange(data))
                    return data;
            }
        }

        // 2 Blocks Under e.g. When jumping
        final BlockPos posBelow = pos.add(0, -1, 0);
        if (InventoryUtils.isValidBlock(world.getBlockState(posBelow).getBlock(), false)) {
            final BlockData data = new BlockData(posBelow, EnumFacing.UP);
            if (validateBlockRange(data))
                return data;
        }

        // 2 Block extension & diagonal
        for (BlockPos blockPosition : blockPositions) {
            final BlockPos blockPos = pos.add(blockPosition);
            for (int i = 0; i < blockPositions.length; i++) {
                final BlockPos blockPos1 = blockPos.add(blockPositions[i]);
                if (InventoryUtils.isValidBlock(world.getBlockState(blockPos1).getBlock(), false)) {
                    final BlockData data = new BlockData(blockPos1, facings[i]);
                    if (validateBlockRange(data))
                        return data;
                }
            }
        }

        // 3 Block extension
        for (final BlockPos blockPosition : blockPositions) {
            final BlockPos blockPos = pos.add(blockPosition);
            for (final BlockPos position : blockPositions) {
                final BlockPos blockPos1 = blockPos.add(position);
                for (int i = 0; i < blockPositions.length; i++) {
                    final BlockPos blockPos2 = blockPos1.add(blockPositions[i]);
                    if (InventoryUtils.isValidBlock(world.getBlockState(blockPos2).getBlock(), false)) {
                        final BlockData data = new BlockData(blockPos2, facings[i]);
                        if (validateBlockRange(data))
                            return data;
                    }
                }
            }
        }

        return null;
    }

    public boolean isRotating() {
        return angles != null;
    }

    private void updateBlockCount() {
        blockCount = 0;

        for (int i = InventoryUtils.EXCLUDE_ARMOR_BEGIN; i < InventoryUtils.END; i++) {
            final ItemStack stack = InventoryUtils.getStackInSlot(i);

            if (stack != null && stack.getItem() instanceof ItemBlock &&
                    InventoryUtils.isGoodBlockStack(stack))
                blockCount += stack.stackSize;
        }
    }

    private static class BlockData {
        private final BlockPos pos;
        private final Vec3 hitVec;
        private final EnumFacing face;

        public BlockData(BlockPos pos, EnumFacing face) {
            this.pos = pos;
            this.face = face;
            this.hitVec = getHitVec();
        }

        private Vec3 getHitVec() {
            final Vec3i directionVec = face.getDirectionVec();
            double x = directionVec.getX() * 0.5D;
            double z = directionVec.getZ() * 0.5D;

            if (face.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                x = -x;
                z = -z;
            }

            final Vec3 hitVec = new Vec3(pos).addVector(x + z, directionVec.getY() * 0.5D, x + z);

            final Vec3 src = mc.thePlayer.getPositionEyes(1.0F);
            final MovingObjectPosition obj = mc.theWorld.rayTraceBlocks(src,
                    hitVec,
                    false,
                    false,
                    true);

            if (obj == null || obj.hitVec == null || obj.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
                return null;

            switch (face.getAxis()) {
                case Z:
                    obj.hitVec = new Vec3(obj.hitVec.xCoord, obj.hitVec.yCoord, Math.round(obj.hitVec.zCoord));
                    break;
                case X:
                    obj.hitVec = new Vec3(Math.round(obj.hitVec.xCoord), obj.hitVec.yCoord, obj.hitVec.zCoord);
                    break;
            }

            if (face != EnumFacing.DOWN && face != EnumFacing.UP) {
                final IBlockState blockState = mc.theWorld.getBlockState(obj.getBlockPos());
                final Block blockAtPos = blockState.getBlock();

                double blockFaceOffset;

                if (blockAtPos instanceof BlockSlab && !((BlockSlab) blockAtPos).isDouble()) {
                    final BlockSlab.EnumBlockHalf half = blockState.getValue(BlockSlab.HALF);

                    blockFaceOffset = RandomUtils.nextDouble(0.1, 0.4);

                    if (half == BlockSlab.EnumBlockHalf.TOP) {
                        blockFaceOffset += 0.5;
                    }
                } else {
                    blockFaceOffset = RandomUtils.nextDouble(0.1, 0.9);
                }

                obj.hitVec = obj.hitVec.addVector(0.0D, -blockFaceOffset, 0.0D);
            }

            return obj.hitVec;
        }
    }

    private boolean isDistFromGround(double dist) {
        return mc.theWorld.checkBlockCollision(mc.thePlayer.getEntityBoundingBox().addCoord(0.0D, -dist, 0.0D));
    }

    public static boolean safewalk() {
        return instance.isEnabled() && safewalk.isToggled();
    }

    private void rotate(final UpdateEvent event, final float[] rotations, final float aimSpeed, boolean lockview) {
        final float[] prevRotations = {lastYaw, lastPitch};

        final float[] cappedRotations = {
                maxAngleChange(prevRotations[0], rotations[0], aimSpeed),
                maxAngleChange(prevRotations[1], rotations[1], aimSpeed)
        };

        final float[] appliedRotations = applyGCD(cappedRotations, prevRotations);

        event.setYaw(appliedRotations[0]);
        event.setPitch(appliedRotations[1]);

        if (lockview) {
            mc.thePlayer.rotationYaw = appliedRotations[0];
            mc.thePlayer.rotationPitch = appliedRotations[1];
        }

        mc.thePlayer.setRotationYawHead(appliedRotations[0]);
        mc.thePlayer.renderYawOffset = appliedRotations[0];
    }

    private float maxAngleChange(final float prev, final float now, final float maxTurn) {
        float dif = MathHelper.wrapAngleTo180_float(now - prev);
        if (dif > maxTurn) dif = maxTurn;
        if (dif < -maxTurn) dif = -maxTurn;
        return prev + dif;
    }

    private double getMouseGCD() {
        final float sens = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        final float pow = sens * sens * sens * 8.0F;
        return pow * 0.15D;
    }

    private float[] applyGCD(final float[] rotations, final float[] prevRots) {
        final float yawDif = rotations[0] - prevRots[0];
        final float pitchDif = rotations[1] - prevRots[1];
        final double gcd = getMouseGCD();

        rotations[0] -= yawDif % gcd;
        rotations[1] -= pitchDif % gcd;
        return rotations;
    }

}
