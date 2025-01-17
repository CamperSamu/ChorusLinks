package pepjebs.choruslinks.utils;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import pepjebs.choruslinks.ChorusLinksMod;
import pepjebs.choruslinks.block.ChorusLinkBlock;
import pepjebs.choruslinks.block.entity.ChorusLinkBlockEntity;
import pepjebs.choruslinks.item.GoldenChorusFruitItem;

import java.util.List;
import java.util.stream.Collectors;

public class ChorusLinksUtils {

    public static Pair<BlockPos, ServerWorld> doChorusFruitConsume(ItemStack stack, World world, ServerPlayerEntity user) {
        if (stack.getItem() instanceof GoldenChorusFruitItem && stack.hasGlint()
                && stack.getOrCreateTag().contains(GoldenChorusFruitItem.GOLDEN_CHORUS_BIND_POS_TAG)) {
            int[] blockPosCoords = stack.getOrCreateTag().getIntArray(GoldenChorusFruitItem.GOLDEN_CHORUS_BIND_POS_TAG);
            String boundDim = stack.getOrCreateTag().getString(GoldenChorusFruitItem.GOLDEN_CHORUS_BIND_DIM_TAG);
            ServerWorld destWorld = null;
            if (blockPosCoords.length == 3) {
                BlockPos blockPos = new BlockPos(blockPosCoords[0], blockPosCoords[1], blockPosCoords[2]);
                if (boundDim.compareTo(world.getRegistryKey().getValue().toString()) != 0 && world.getServer() != null) {
                    if (ChorusLinksMod.CONFIG != null && !ChorusLinksMod.CONFIG.enableEnchantedInterDimensionTeleport) {
                        return new Pair<>(doChorusLinkSearch(stack, world, user), (ServerWorld) world);
                    }
                    // We need to set the destination dimension
                    for (ServerWorld w : world.getServer().getWorlds()) {
                        if (w.getRegistryKey().getValue().toString().compareTo(boundDim) == 0) {
                            destWorld = w;
                            break;
                        }
                    }
                    if (destWorld != null) {
                        world = destWorld;
                    } else {
                        return new Pair<>(doChorusLinkSearch(stack, world, user), (ServerWorld) world);
                    }
                }
                if (!world.isChunkLoaded(blockPos.getX() >> 4, blockPos.getZ() >> 4)) {
                    world.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
                }
                if (world.isChunkLoaded(blockPos.getX() >> 4, blockPos.getZ() >> 4)
                        && world.getBlockState(blockPos).getBlock() instanceof ChorusLinkBlock) {
                    return new Pair<>(blockPos, (ServerWorld) world);
                }
            }
        }
        return new Pair<>(doChorusLinkSearch(stack, world, user), (ServerWorld) world);
    }
//FIXME: change ChorusLink search logic
    public static BlockPos doChorusLinkSearch(ItemStack stack, World world, ServerPlayerEntity user) {
        List<ChorusLinkBlockEntity> chorusLinks = world.blockEntities
                .stream()
                .filter(be -> be instanceof ChorusLinkBlockEntity)
                .map(be -> (ChorusLinkBlockEntity) be)
                .collect(Collectors.toList());
        BlockPos nearestChorusLink = null;
        double nearestSoFar = Double.MAX_VALUE;
        int radius = ChorusLinksMod.CONFIG == null ? 64 : ChorusLinksMod.CONFIG.baseChorusFruitLinkRadius;
        if (stack.getItem() instanceof GoldenChorusFruitItem) {
            radius *= ((GoldenChorusFruitItem) stack.getItem()).getRadiusMultiplier();
        }
        for (ChorusLinkBlockEntity link : chorusLinks) {
            BlockPos targetPos = link.getPos();
            if (targetPos.isWithinDistance(user.getPos(), radius) && world.isChunkLoaded(targetPos.getX() >> 4, targetPos.getZ() >> 4)) {
                BlockState state = world.getBlockState(targetPos);
                if (world.getReceivedStrongRedstonePower(targetPos) != 0) continue;
                double playerDist = targetPos.getSquaredDistance(user.getPos(), true);
                if (state.getBlock() instanceof ChorusLinkBlock && nearestSoFar > playerDist) {
                    nearestChorusLink = targetPos;
                    nearestSoFar = playerDist;
                }
            }
        }
        return nearestChorusLink;
    }

    public static boolean doesBoundPosEqualBlockPos(ItemStack stack, BlockPos pos) {
        if (stack.getItem() instanceof GoldenChorusFruitItem && stack.hasGlint()
                && stack.getOrCreateTag().contains(GoldenChorusFruitItem.GOLDEN_CHORUS_BIND_POS_TAG)) {
            int[] blockPos = stack.getOrCreateTag().getIntArray(GoldenChorusFruitItem.GOLDEN_CHORUS_BIND_POS_TAG);
            if (blockPos.length == 3) {
                return blockPos[0] == pos.getX() && blockPos[1] == pos.getY() && blockPos[2] == pos.getZ();
            }
        }
        return false;
    }

    public static void doChorusLinkTeleport(ItemStack usingStack, ServerWorld world, ServerPlayerEntity user, BlockPos blockPos) {
        if (world.getRegistryKey().getValue().toString().compareTo(user.world.getRegistryKey().getValue().toString()) != 0) {
            user.teleport(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), user.getYaw(), user.getPitch());
        }
        if (user.hasVehicle()) {
            user.stopRiding();
        }
        if (user.teleport(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5, true)) {
            SoundEvent soundEvent = SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
            world.playSound(null, user.getX(), user.getY(), user.getZ(), soundEvent, SoundCategory.PLAYERS, 1.0F, 1.0F);
            user.playSound(soundEvent, 1.0F, 1.0F);
        }
        user.getItemCooldownManager().set(usingStack.getItem(), 20);
    }

    public static void doVanillaChorusFruitConsumption(ItemStack stack, World world, ServerPlayerEntity user) {
        double d = user.getX();
        double e = user.getY();
        double f = user.getZ();

        for(int i = 0; i < 16; ++i) {
            double g = user.getX() + (user.getRandom().nextDouble() - 0.5D) * 16.0D;
            double h = MathHelper.clamp(user.getY() +
                    (double)(user.getRandom().nextInt(16) - 8), 0.0D, (world.getDimension().getHeight() - 1));
            double j = user.getZ() + (user.getRandom().nextDouble() - 0.5D) * 16.0D;
            if (user.hasVehicle()) {
                user.stopRiding();
            }

            if (user.teleport(g, h, j, true)) {
                SoundEvent soundEvent = SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
                world.playSound(null, d, e, f, soundEvent, SoundCategory.PLAYERS, 1.0F, 1.0F);
                user.playSound(soundEvent, 1.0F, 1.0F);
                break;
            }
        }

        // We need "stack.getItem()" instead of "this"
        ((PlayerEntity)user).getItemCooldownManager().set(stack.getItem(), 20);
    }
}
