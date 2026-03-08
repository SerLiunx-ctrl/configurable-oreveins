package com.serliunx.configurableoreveins.item;

import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import com.serliunx.configurableoreveins.config.ModConfiguration;
import com.serliunx.configurableoreveins.config.vein.ModConfigManager;
import com.serliunx.configurableoreveins.config.vein.VeinDefinition;
import com.serliunx.configurableoreveins.data.PlayerVeinStatusData;
import com.serliunx.configurableoreveins.data.VeinWorldData;
import com.serliunx.configurableoreveins.data.VeinWorldData.StatRecord;
import com.serliunx.configurableoreveins.data.VeinWorldData.VeinRecord;
import com.serliunx.configurableoreveins.network.NetworkHandler;
import com.serliunx.configurableoreveins.network.message.OpenLocatorGuiMessage;
import com.serliunx.configurableoreveins.util.BlockStateResolver;
import com.serliunx.configurableoreveins.vein.LocatorVeinInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * 矿脉定位仪器物品实现。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class VeinLocatorItem extends Item {

    private static final String TARGET_TAG = "VeinTarget";
    private static final String AUTO_TARGET_TAG = "AutoVeinTarget";
    private static final String TARGET_DIMENSION_TAG = "Dimension";
    private static final String TARGET_X_TAG = "X";
    private static final String TARGET_Y_TAG = "Y";
    private static final String TARGET_Z_TAG = "Z";
    private static final String TARGET_NAME_TAG = "VeinName";
    private static final String TARGET_COLOR_TAG = "HighlightColor";
    private static final int AUTO_TARGET_REFRESH_INTERVAL = 10;

    public VeinLocatorItem() {
        addPropertyOverride(
                new ResourceLocation("angle"),
                new IItemPropertyGetter() {
                    private double rotation;
                    private double rota;
                    private long lastUpdateTick;

                    @Override
                    public float apply(@Nonnull ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                        if (entityIn == null && !stack.isOnItemFrame()) {
                            return 0.0F;
                        }
                        Entity entity = entityIn != null ? entityIn : stack.getItemFrame();
                        if (entity == null) {
                            return 0.0F;
                        }

                        World world = worldIn != null ? worldIn : entity.world;
                        TargetInfo target = getManualTarget(stack);
                        if (target == null) {
                            target = getAutomaticTarget(stack);
                        }
                        if (target == null ||
                                target.dimensionId != world.provider.getDimension()) {
                            return 0.0F;
                        }

                        double rotationValue;
                        if (world.provider.isSurfaceWorld()) {
                            double entityAngle =
                                    entityIn != null
                                            ? MathHelper.positiveModulo(entity.rotationYaw / 360.0D, 1.0D)
                                            : MathHelper.positiveModulo(
                                                    getFrameRotation((EntityItemFrame) entity) / 360.0D, 1.0D);
                            double targetAngle =
                                    Math.atan2(entity.posZ - (double) target.z, entity.posX - (double) target.x)
                                            / (Math.PI * 2D);
                            rotationValue = 0.5D - (entityAngle - 0.25D - targetAngle);
                        } else {
                            rotationValue = 0.0D;
                        }

                        if (entityIn != null) {
                            rotationValue = wobble(world, rotationValue);
                        }
                        return MathHelper.positiveModulo((float) rotationValue, 1.0F);
                    }

                    private double wobble(World world, double targetValue) {
                        if (world.getTotalWorldTime() != lastUpdateTick) {
                            lastUpdateTick = world.getTotalWorldTime();
                            double delta = targetValue - rotation;
                            delta = MathHelper.positiveModulo(delta + 0.5D, 1.0D) - 0.5D;
                            rota += delta * 0.1D;
                            rota *= 0.8D;
                            rotation = MathHelper.positiveModulo(rotation + rota, 1.0D);
                        }

                        return rotation;
                    }
                });
    }

    @Override
    public void onUpdate(@Nonnull ItemStack stack, World world, @Nonnull Entity entity, int itemSlot, boolean isSelected) {
        if (world.isRemote ||
                !(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        boolean heldInOffhand = player.getHeldItemOffhand() == stack;
        if (!isSelected && !heldInOffhand) {
            return;
        }

        if ((world.getTotalWorldTime() + itemSlot) % AUTO_TARGET_REFRESH_INTERVAL != 0) {
            return;
        }

        if (refreshAutomaticTarget(stack, world, player)) {
            player.inventory.markDirty();
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        refreshAutomaticTarget(stack, world, player);
        int rangeChunks = Math.max(1, ModConfiguration.general.locatorRangeChunks);
        int maxResults = Math.max(1, ModConfiguration.general.locatorMaxResults);
        java.util.ArrayList<LocatorVeinInfo> payload =
                new java.util.ArrayList<>(
                        createNearbyVeinInfos(world, player, rangeChunks, maxResults));
        if (payload.isEmpty()) {
            clearTarget(stack);
            NetworkHandler.CHANNEL.sendTo(
                    new OpenLocatorGuiMessage(hand.ordinal(), rangeChunks, payload), (EntityPlayerMP) player);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        NetworkHandler.CHANNEL.sendTo(
                new OpenLocatorGuiMessage(hand.ordinal(), rangeChunks, payload), (EntityPlayerMP) player);

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    /**
     * 构建附近矿脉信息列表.
     *
     * @param world 所处世界
     * @param player 玩家
     * @param rangeChunks 区块范围
     * @param maxResults 最大结果数量
     * @return 矿脉信息列表
     */
    public static List<LocatorVeinInfo> createNearbyVeinInfos(World world, EntityPlayer player,
                                                              int rangeChunks, int maxResults) {

        VeinWorldData worldData = VeinWorldData.get(world);
        PlayerVeinStatusData statusData = PlayerVeinStatusData.get(world);
        BlockPos playerPos = player.getPosition();
        int centerChunkX = playerPos.getX() >> 4;
        int centerChunkZ = playerPos.getZ() >> 4;
        List<VeinRecord> results =
                worldData.findNearby(
                        world.provider.getDimension(), centerChunkX, centerChunkZ, Math.max(1, rangeChunks));
        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        final double playerX = player.posX;
        final double playerY = player.posY;
        final double playerZ = player.posZ;

        results.sort(Comparator.comparingDouble(left -> distanceSq(left, playerX, playerY, playerZ)));

        Map<Integer, VeinDescriptor> namesByHash = buildVeinNameIndex();
        int shown = Math.min(Math.max(0, maxResults), results.size());
        java.util.ArrayList<LocatorVeinInfo> payload = new java.util.ArrayList<>(shown);
        for (int index = 0; index < results.size() && payload.size() < shown; index++) {
            VeinRecord record = results.get(index);
            boolean mined =
                    statusData.isMined(
                            player.getUniqueID(),
                            world.provider.getDimension(),
                            record.getChunkX(),
                            record.getChunkZ());
            payload.add(createLocatorVeinInfo(world.provider.getDimension(), record, namesByHash, mined));
        }
        return payload;
    }

    private static Map<Integer, VeinDescriptor> buildVeinNameIndex() {
        ModConfigManager configManager = ConfigurableOreVeinsMod.getConfigManager();
        Map<Integer, VeinDescriptor> namesByHash = new HashMap<>();
        if (configManager == null) {
            return namesByHash;
        }

        for (VeinDefinition vein : configManager.getVeins()) {
            int iconBlockId = 0;
            net.minecraft.block.state.IBlockState iconState =
                    BlockStateResolver.resolveState(
                            vein.getLocatorIconBlock(), vein.getLocatorIconMeta());
            if (iconState != null) {
                iconBlockId = net.minecraft.block.Block.getIdFromBlock(iconState.getBlock());
            }
            namesByHash.put(
                    vein.getName().hashCode(),
                    new VeinDescriptor(
                            vein.getDisplayName(),
                            vein.getHighlightColor(),
                            iconBlockId,
                            vein.getLocatorIconMeta()));
        }

        return namesByHash;
    }
    private static LocatorVeinInfo createLocatorVeinInfo(int dimensionId, VeinRecord record,
                                                         Map<Integer, VeinDescriptor> namesByHash, boolean mined) {
        VeinDescriptor descriptor =
                namesByHash.containsKey(record.getVeinHash())
                        ? namesByHash.get(record.getVeinHash())
                        : new VeinDescriptor(
                                "unknown#" + Integer.toHexString(record.getVeinHash()), 0xB2FF8C, 0, 0);
        return new LocatorVeinInfo(
                record.getVeinHash(),
                mined,
                dimensionId,
                record.getChunkX(),
                record.getChunkZ(),
                record.getCenterX(),
                record.getCenterY(),
                record.getCenterZ(),
                descriptor.getDisplayName(),
                descriptor.getHighlightColor(),
                record.getIconBlockId() > 0 ? record.getIconBlockId() : descriptor.getIconBlockId(),
                record.getIconBlockId() > 0 ? record.getIconMeta() : descriptor.getIconMeta(),
                record.getTotalBlocks(),
                toPackedStateArray(record.getStats()),
                toCountArray(record.getStats()));
    }

    private static double distanceSq(
            VeinRecord record, double playerX, double playerY, double playerZ) {
        double dx = record.getCenterX() - playerX;
        double dy = record.getCenterY() - playerY;
        double dz = record.getCenterZ() - playerZ;
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    @Nullable
    private static VeinRecord findNearestUnminedVein(World world, EntityPlayer player, int rangeChunks) {
        VeinWorldData worldData = VeinWorldData.get(world);
        PlayerVeinStatusData statusData = PlayerVeinStatusData.get(world);
        int centerChunkX = player.getPosition().getX() >> 4;
        int centerChunkZ = player.getPosition().getZ() >> 4;
        List<VeinRecord> candidates =
                worldData.findNearby(world.provider.getDimension(), centerChunkX, centerChunkZ, rangeChunks);
        VeinRecord nearest = null;
        double bestDistanceSq = Double.MAX_VALUE;
        for (VeinRecord record : candidates) {
            if (statusData.isMined(
                    player.getUniqueID(),
                    world.provider.getDimension(),
                    record.getChunkX(),
                    record.getChunkZ())) {
                continue;
            }
            double currentDistanceSq = distanceSq(record, player.posX, player.posY, player.posZ);
            if (nearest == null || currentDistanceSq < bestDistanceSq) {
                nearest = record;
                bestDistanceSq = currentDistanceSq;
            }
        }
        return nearest;
    }

    /**
     * 设置定位器目标
     */
    public static void setTarget(
            ItemStack stack, int dimensionId, int x, int y, int z, String veinName) {
        setTarget(stack, dimensionId, x, y, z, veinName, 0xB2FF8C);
    }

    /**
     * 设置定位器目标
     */
    public static void setTarget(
            ItemStack stack, int dimensionId, int x, int y, int z, String veinName, int highlightColor) {
        setTargetTag(stack, TARGET_TAG, dimensionId, x, y, z, veinName, highlightColor);
    }

    /**
     * 清除定位器目标
     */
    public static void clearTarget(ItemStack stack) {
        clearTargetTag(stack, TARGET_TAG);
    }

    /**
     * 按坐标匹配后清除手动定位目标
     */
    @SuppressWarnings("all")
    public static boolean clearTargetIfMatches(ItemStack stack, int dimensionId, int x, int y, int z) {
        return clearTargetTagIfMatches(stack, TARGET_TAG, dimensionId, x, y, z);
    }

    /**
     * 刷新定位器自动目标
     */
    public static boolean refreshAutomaticTarget(ItemStack stack, World world, EntityPlayer player) {
        if (hasTargetTag(stack, TARGET_TAG)) {
            return false;
        }
        VeinRecord nearest = findNearestUnminedVein(world, player, Math.max(1, ModConfiguration.general.locatorRangeChunks));
        if (nearest == null) {
            return clearAutomaticTarget(stack);
        }
        VeinDescriptor descriptor = resolveDescriptor(nearest.getVeinHash());
        return setTargetTag(
                stack,
                AUTO_TARGET_TAG,
                world.provider.getDimension(),
                nearest.getCenterX(),
                nearest.getCenterY(),
                nearest.getCenterZ(),
                descriptor.getDisplayName(),
                descriptor.getHighlightColor());
    }

    /**
     * 清除定位器自动目标
     */
    public static boolean clearAutomaticTarget(ItemStack stack) {
        return clearTargetTag(stack, AUTO_TARGET_TAG);
    }

    /**
     * 按坐标匹配后清除自动定位目标
     */
    @SuppressWarnings("all")
    public static boolean clearAutomaticTargetIfMatches(ItemStack stack, int dimensionId, int x, int y, int z) {
        return clearTargetTagIfMatches(stack, AUTO_TARGET_TAG, dimensionId, x, y, z);
    }

    private static boolean clearTargetTag(ItemStack stack, String targetTagName) {
        NBTTagCompound root = stack.getTagCompound();
        if (root != null) {
            if (!root.hasKey(targetTagName)) {
                return false;
            }
            root.removeTag(targetTagName);
            if (root.hasNoTags()) {
                stack.setTagCompound(null);
            }
            return true;
        }

        return false;
    }

    /**
     * 按坐标匹配后清除目标标签
     */
    private static boolean clearTargetTagIfMatches(
            ItemStack stack, String targetTagName, int dimensionId, int x, int y, int z) {
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(targetTagName)) {
            return false;
        }
        NBTTagCompound targetTag = root.getCompoundTag(targetTagName);
        if (targetTag.getInteger(TARGET_DIMENSION_TAG) != dimensionId
                || targetTag.getInteger(TARGET_X_TAG) != x
                || targetTag.getInteger(TARGET_Y_TAG) != y
                || targetTag.getInteger(TARGET_Z_TAG) != z) {
            return false;
        }
        return clearTargetTag(stack, targetTagName);
    }

    @Nullable
    private static TargetInfo getManualTarget(ItemStack stack) {
        return getTarget(stack, TARGET_TAG);
    }

    @Nullable
    private static TargetInfo getAutomaticTarget(ItemStack stack) {
        return getTarget(stack, AUTO_TARGET_TAG);
    }

    @Nullable
    private static TargetInfo getTarget(ItemStack stack, String targetTagName) {
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey(targetTagName)) {
            return null;
        }
        NBTTagCompound targetTag = root.getCompoundTag(targetTagName);
        return new TargetInfo(
                targetTag.getInteger(TARGET_DIMENSION_TAG),
                targetTag.getInteger(TARGET_X_TAG),
                targetTag.getInteger(TARGET_Y_TAG),
                targetTag.getInteger(TARGET_Z_TAG));
    }

    private static double getFrameRotation(EntityItemFrame frame) {
        if (frame.facingDirection == null) {
            return 1.0;
        }
        return MathHelper.wrapDegrees(180 + (frame.facingDirection.getHorizontalIndex() * 90));
    }

    private static int[] toPackedStateArray(List<StatRecord> stats) {
        int[] values = new int[stats.size()];
        for (int index = 0; index < stats.size(); index++) {
            values[index] = stats.get(index).getPackedState();
        }
        return values;
    }

    private static int[] toCountArray(List<StatRecord> stats) {
        int[] values = new int[stats.size()];
        for (int index = 0; index < stats.size(); index++) {
            values[index] = stats.get(index).getCount();
        }
        return values;
    }

    @SuppressWarnings("all")
    private static boolean hasTargetTag(ItemStack stack, String targetTagName) {
        NBTTagCompound root = stack.getTagCompound();
        return root != null && root.hasKey(targetTagName);
    }

    private static boolean setTargetTag(
            ItemStack stack,
            String targetTagName,
            int dimensionId,
            int x,
            int y,
            int z,
            String veinName,
            int highlightColor) {
        NBTTagCompound root = stack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            stack.setTagCompound(root);
        }
        NBTTagCompound existing =
                root.hasKey(targetTagName) ? root.getCompoundTag(targetTagName) : null;
        if (existing != null
                && existing.getInteger(TARGET_DIMENSION_TAG) == dimensionId
                && existing.getInteger(TARGET_X_TAG) == x
                && existing.getInteger(TARGET_Y_TAG) == y
                && existing.getInteger(TARGET_Z_TAG) == z
                && veinName.equals(existing.getString(TARGET_NAME_TAG))
                && existing.getInteger(TARGET_COLOR_TAG) == (highlightColor & 0xFFFFFF)) {
            return false;
        }

        NBTTagCompound targetTag = new NBTTagCompound();
        targetTag.setInteger(TARGET_DIMENSION_TAG, dimensionId);
        targetTag.setInteger(TARGET_X_TAG, x);
        targetTag.setInteger(TARGET_Y_TAG, y);
        targetTag.setInteger(TARGET_Z_TAG, z);
        targetTag.setString(TARGET_NAME_TAG, veinName);
        targetTag.setInteger(TARGET_COLOR_TAG, highlightColor & 0xFFFFFF);
        root.setTag(targetTagName, targetTag);
        return true;
    }

    private static VeinDescriptor resolveDescriptor(int veinHash) {
        ModConfigManager configManager = ConfigurableOreVeinsMod.getConfigManager();
        if (configManager != null) {
            for (VeinDefinition vein : configManager.getVeins()) {
                if (vein.getName().hashCode() == veinHash) {
                    int iconBlockId = 0;
                    net.minecraft.block.state.IBlockState iconState =
                            BlockStateResolver.resolveState(
                                    vein.getLocatorIconBlock(), vein.getLocatorIconMeta());
                    if (iconState != null) {
                        iconBlockId = net.minecraft.block.Block.getIdFromBlock(iconState.getBlock());
                    }
                    return new VeinDescriptor(
                            vein.getDisplayName(),
                            vein.getHighlightColor(),
                            iconBlockId,
                            vein.getLocatorIconMeta());
                }
            }
        }

        return new VeinDescriptor("unknown#" + Integer.toHexString(veinHash), 0xB2FF8C, 0, 0);
    }

    /**
     * 定位目标的 NBT 简化数据
     */
    private static class TargetInfo {

        private final int dimensionId;
        private final int x;
        private final int y;
        private final int z;

        private TargetInfo(int dimensionId, int x, int y, int z) {
            this.dimensionId = dimensionId;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    /**
     * 定位显示描述数据
     */
    private static class VeinDescriptor {

        private final String displayName;
        private final int highlightColor;
        private final int iconBlockId;
        private final int iconMeta;

        private VeinDescriptor(String displayName, int highlightColor, int iconBlockId, int iconMeta) {
            this.displayName = displayName;
            this.highlightColor = highlightColor;
            this.iconBlockId = iconBlockId;
            this.iconMeta = iconMeta;
        }

        private String getDisplayName() {
            return displayName;
        }

        private int getHighlightColor() {
            return highlightColor;
        }

        private int getIconBlockId() {
            return iconBlockId;
        }

        private int getIconMeta() {
            return iconMeta;
        }
    }
}
