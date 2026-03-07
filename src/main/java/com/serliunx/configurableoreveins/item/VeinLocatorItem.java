package com.serliunx.configurableoreveins.item;

import com.serliunx.configurableoreveins.util.BlockStateResolver;
import com.serliunx.configurableoreveins.ConfigurableOreVeinsMod;
import com.serliunx.configurableoreveins.config.GeneralConfig;
import com.serliunx.configurableoreveins.config.ModConfigManager;
import com.serliunx.configurableoreveins.config.VeinDefinition;
import com.serliunx.configurableoreveins.data.PlayerVeinStatusData;
import com.serliunx.configurableoreveins.data.VeinWorldData;
import com.serliunx.configurableoreveins.data.VeinWorldData.StatRecord;
import com.serliunx.configurableoreveins.data.VeinWorldData.VeinRecord;
import com.serliunx.configurableoreveins.network.LocatorVeinInfo;
import com.serliunx.configurableoreveins.network.NetworkHandler;
import com.serliunx.configurableoreveins.network.OpenLocatorGuiMessage;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
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

    /** 构造 VeinLocatorItem 实例。 */
    public VeinLocatorItem() {
        addPropertyOverride(
                new ResourceLocation("angle"),
                new IItemPropertyGetter() {
                    private double rotation;
                    private double rota;
                    private long lastUpdateTick;

                    @Override
                    public float apply(
                            ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                        if (entityIn == null && !stack.isOnItemFrame()) {
                            return 0.0F;
                        }
                        Entity entity = entityIn != null ? entityIn : stack.getItemFrame();
                        World world = worldIn != null ? worldIn : entity.world;
                        TargetInfo target = getManualTarget(stack);
                        if (target == null) {
                            target = getAutomaticTarget(stack);
                        }
                        if (target == null || target.dimensionId != world.provider.getDimension()) {
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

    /**
     * 刷新定位器的自动目标。
     *
     * @param stack 参数 stack。
     * @param world 参数 world。
     * @param entity 参数 entity。
     * @param itemSlot 参数 itemSlot。
     * @param isSelected 参数 isSelected。
    */
    @Override
    public void onUpdate(
            ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (world.isRemote || !(entity instanceof EntityPlayer)) {
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

    /**
     * 处理定位器右键行为。
     *
     * @param world 参数 world。
     * @param player 参数 player。
     * @param hand 参数 hand。
     * @return 处理结果。
    */
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }
        refreshAutomaticTarget(stack, world, player);
        int rangeChunks = Math.max(1, GeneralConfig.locatorRangeChunks);
        int maxResults = Math.max(1, GeneralConfig.locatorMaxResults);
        java.util.ArrayList<LocatorVeinInfo> payload =
                new java.util.ArrayList<LocatorVeinInfo>(
                        createNearbyVeinInfos(world, player, rangeChunks, maxResults));
        if (payload.isEmpty()) {
            clearTarget(stack);
            NetworkHandler.CHANNEL.sendTo(
                    new OpenLocatorGuiMessage(hand.ordinal(), rangeChunks, payload), (EntityPlayerMP) player);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }

        NetworkHandler.CHANNEL.sendTo(
                new OpenLocatorGuiMessage(hand.ordinal(), rangeChunks, payload), (EntityPlayerMP) player);

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    /**
     * 构建附近矿脉信息列表。
     *
     * @param world 参数 world。
     * @param player 参数 player。
     * @param rangeChunks 参数 rangeChunks。
     * @param maxResults 参数 maxResults。
     * @return 处理结果。
    */
    public static List<LocatorVeinInfo> createNearbyVeinInfos(
            World world, EntityPlayer player, int rangeChunks, int maxResults) {
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
        Collections.sort(
                results,
                new Comparator<VeinRecord>() {
                    @Override
                    public int compare(VeinRecord left, VeinRecord right) {
                        return Double.compare(
                                distanceSq(left, playerX, playerY, playerZ),
                                distanceSq(right, playerX, playerY, playerZ));
                    }
                });
        Map<Integer, VeinDescriptor> namesByHash = buildVeinNameIndex();
        int shown = Math.min(Math.max(0, maxResults), results.size());
        java.util.ArrayList<LocatorVeinInfo> payload = new java.util.ArrayList<LocatorVeinInfo>(shown);
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

    /**
     * 构建 VeinNameIndex。
     *
     * @return 处理结果。
    */
    private static Map<Integer, VeinDescriptor> buildVeinNameIndex() {
        ModConfigManager configManager = ConfigurableOreVeinsMod.getConfigManager();
        Map<Integer, VeinDescriptor> namesByHash = new HashMap<Integer, VeinDescriptor>();
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

    /**
     * 构建单条定位矿脉信息。
     *
     * @param dimensionId 参数 dimensionId。
     * @param record 参数 record。
     * @param namesByHash 参数 namesByHash。
     * @return 处理结果。
    */
    private static LocatorVeinInfo createLocatorVeinInfo(
            int dimensionId, VeinRecord record, Map<Integer, VeinDescriptor> namesByHash, boolean mined) {
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

    /**
     * 执行 distanceSq 逻辑。
     *
     * @param record 参数 record。
     * @param playerX 参数 playerX。
     * @param playerY 参数 playerY。
     * @param playerZ 参数 playerZ。
     * @return 处理结果。
    */
    private static double distanceSq(
            VeinRecord record, double playerX, double playerY, double playerZ) {
        double dx = record.getCenterX() - playerX;
        double dy = record.getCenterY() - playerY;
        double dz = record.getCenterZ() - playerZ;
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    /**
     * 查找最近的未标记矿脉。
     *
     * @param world 参数 world。
     * @param player 参数 player。
     * @param rangeChunks 参数 rangeChunks。
     * @return 处理结果。
    */
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
     * 设置定位器目标。
     *
     * @param stack 参数 stack。
     * @param dimensionId 参数 dimensionId。
     * @param x 参数 x。
     * @param y 参数 y。
     * @param z 参数 z。
     * @param veinName 参数 veinName。
    */
    public static void setTarget(
            ItemStack stack, int dimensionId, int x, int y, int z, String veinName) {
        setTarget(stack, dimensionId, x, y, z, veinName, 0xB2FF8C);
    }

    /**
     * 设置定位器目标。
     *
     * @param stack 参数 stack。
     * @param dimensionId 参数 dimensionId。
     * @param x 参数 x。
     * @param y 参数 y。
     * @param z 参数 z。
     * @param veinName 参数 veinName。
     * @param highlightColor 参数 highlightColor。
    */
    public static void setTarget(
            ItemStack stack, int dimensionId, int x, int y, int z, String veinName, int highlightColor) {
        setTargetTag(stack, TARGET_TAG, dimensionId, x, y, z, veinName, highlightColor);
    }

    /**
     * 清除定位器目标。
     *
     * @param stack 参数 stack。
    */
    public static void clearTarget(ItemStack stack) {
        clearTargetTag(stack, TARGET_TAG);
    }

    /**
     * 按坐标匹配后清除手动定位目标。
     *
     * @param stack 参数 stack。
     * @param dimensionId 参数 dimensionId。
     * @param x 参数 x。
     * @param y 参数 y。
     * @param z 参数 z。
     * @return 处理结果。
    */
    public static boolean clearTargetIfMatches(ItemStack stack, int dimensionId, int x, int y, int z) {
        return clearTargetTagIfMatches(stack, TARGET_TAG, dimensionId, x, y, z);
    }

    /**
     * 刷新定位器自动目标。
     *
     * @param stack 参数 stack。
     * @param world 参数 world。
     * @param player 参数 player。
     * @return 处理结果。
    */
    public static boolean refreshAutomaticTarget(ItemStack stack, World world, EntityPlayer player) {
        if (hasTargetTag(stack, TARGET_TAG)) {
            return false;
        }
        VeinRecord nearest = findNearestUnminedVein(world, player, Math.max(1, GeneralConfig.locatorRangeChunks));
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
     * 清除定位器自动目标。
     *
     * @param stack 参数 stack。
     * @return 处理结果。
    */
    public static boolean clearAutomaticTarget(ItemStack stack) {
        return clearTargetTag(stack, AUTO_TARGET_TAG);
    }

    /**
     * 按坐标匹配后清除自动定位目标。
     *
     * @param stack 参数 stack。
     * @param dimensionId 参数 dimensionId。
     * @param x 参数 x。
     * @param y 参数 y。
     * @param z 参数 z。
     * @return 处理结果。
    */
    public static boolean clearAutomaticTargetIfMatches(ItemStack stack, int dimensionId, int x, int y, int z) {
        return clearTargetTagIfMatches(stack, AUTO_TARGET_TAG, dimensionId, x, y, z);
    }

    /**
     * 执行 clearTargetTag 逻辑。
     *
     * @param stack 参数 stack。
     * @param targetTagName 参数 targetTagName。
     * @return 处理结果。
    */
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
     * 按坐标匹配后清除目标标签。
     *
     * @param stack 参数 stack。
     * @param targetTagName 参数 targetTagName。
     * @param dimensionId 参数 dimensionId。
     * @param x 参数 x。
     * @param y 参数 y。
     * @param z 参数 z。
     * @return 处理结果。
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

    /**
     * 获取 ManualTarget。
     *
     * @param stack 参数 stack。
     * @return 处理结果。
    */
    @Nullable
    private static TargetInfo getManualTarget(ItemStack stack) {
        return getTarget(stack, TARGET_TAG);
    }

    /**
     * 获取 AutomaticTarget。
     *
     * @param stack 参数 stack。
     * @return 处理结果。
    */
    @Nullable
    private static TargetInfo getAutomaticTarget(ItemStack stack) {
        return getTarget(stack, AUTO_TARGET_TAG);
    }

    /**
     * 获取 Target。
     *
     * @param stack 参数 stack。
     * @param targetTagName 参数 targetTagName。
     * @return 处理结果。
    */
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

    /**
     * 获取 FrameRotation。
     *
     * @param frame 参数 frame。
     * @return 处理结果。
    */
    private static double getFrameRotation(EntityItemFrame frame) {
        return MathHelper.wrapDegrees(180 + (frame.facingDirection.getHorizontalIndex() * 90));
    }

    /**
     * 执行 toPackedStateArray 逻辑。
     *
     * @param stats 参数 stats。
     * @return 处理结果。
    */
    private static int[] toPackedStateArray(List<StatRecord> stats) {
        int[] values = new int[stats.size()];
        for (int index = 0; index < stats.size(); index++) {
            values[index] = stats.get(index).getPackedState();
        }
        return values;
    }

    /**
     * 执行 toCountArray 逻辑。
     *
     * @param stats 参数 stats。
     * @return 处理结果。
    */
    private static int[] toCountArray(List<StatRecord> stats) {
        int[] values = new int[stats.size()];
        for (int index = 0; index < stats.size(); index++) {
            values[index] = stats.get(index).getCount();
        }
        return values;
    }

    /**
     * 判断是否包含 TargetTag。
     *
     * @param stack 参数 stack。
     * @param targetTagName 参数 targetTagName。
     * @return 处理结果。
    */
    private static boolean hasTargetTag(ItemStack stack, String targetTagName) {
        NBTTagCompound root = stack.getTagCompound();
        return root != null && root.hasKey(targetTagName);
    }

    /**
     * 设置 TargetTag。
     *
     * @param stack 参数 stack。
     * @param targetTagName 参数 targetTagName。
     * @param dimensionId 参数 dimensionId。
     * @param x 参数 x。
     * @param y 参数 y。
     * @param z 参数 z。
     * @param veinName 参数 veinName。
     * @param highlightColor 参数 highlightColor。
     * @return 处理结果。
    */
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

    /**
     * 解析 Descriptor。
     *
     * @param veinHash 参数 veinHash。
     * @return 处理结果。
    */
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
     * 定位目标的 NBT 简化数据。
     *
     * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
     * @version 0.0.1
     * @since 2026/3/7
    */
    private static class TargetInfo {
        private final int dimensionId;
        private final int x;
        private final int y;
        private final int z;

        /**
         * 构造 TargetInfo 实例。
         *
         * @param dimensionId 参数 dimensionId。
         * @param x 参数 x。
         * @param y 参数 y。
         * @param z 参数 z。
        */
        private TargetInfo(int dimensionId, int x, int y, int z) {
            this.dimensionId = dimensionId;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    /**
     * 定位显示描述数据。
     *
     * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
     * @version 0.0.1
     * @since 2026/3/7
    */
    private static class VeinDescriptor {
        private final String displayName;
        private final int highlightColor;
        private final int iconBlockId;
        private final int iconMeta;

        /**
         * 构造 VeinDescriptor 实例。
         *
         * @param displayName 参数 displayName。
         * @param highlightColor 参数 highlightColor。
         * @param iconBlockId 参数 iconBlockId。
         * @param iconMeta 参数 iconMeta。
        */
        private VeinDescriptor(String displayName, int highlightColor, int iconBlockId, int iconMeta) {
            this.displayName = displayName;
            this.highlightColor = highlightColor;
            this.iconBlockId = iconBlockId;
            this.iconMeta = iconMeta;
        }

        /**
         * 获取 DisplayName。
         *
         * @return 处理结果。
        */
        private String getDisplayName() {
            return displayName;
        }

        /**
         * 获取 HighlightColor。
         *
         * @return 处理结果。
        */
        private int getHighlightColor() {
            return highlightColor;
        }

        /**
         * 获取 IconBlockId。
         *
         * @return 处理结果。
        */
        private int getIconBlockId() {
            return iconBlockId;
        }

        /**
         * 获取 IconMeta。
         *
         * @return 处理结果。
        */
        private int getIconMeta() {
            return iconMeta;
        }
    }
}
