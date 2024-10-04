package net.alvisssss.advancedmissile.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.alvisssss.advancedmissile.AdvancedMissile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class RealisticExplosion {

    private final HashSet<BlockPos> affectedBlocks = Sets.newHashSet();
    private final HashSet<BlockPos> checkedBlocks = Sets.newHashSet();
    private final Map<PlayerEntity, Vec3d> affectedPlayers = Maps.newHashMap();
    private final int tntNum;
    private final Vec3d explosionCenter;
    private final World world;
    private final Entity entity;
    private int count = 0;
    private boolean blocksDestroyed = true;

    @Nullable
    private final BlockPos blockHitPos;
    private final Entity hitEntity;

    public RealisticExplosion(World world, Entity entity, double x, double y, double z, float power, @Nullable BlockPos blockHitPos, @Nullable Entity hitEntity, boolean precise) {

        this.entity = entity;
        this.world = world;

        this.tntNum = ((int) power);
        this.explosionCenter = new Vec3d(x, y, z);
        if (blockHitPos == null) {
            this.blockHitPos = entity.getBlockPos();
        } else {
            this.blockHitPos = blockHitPos;
        }

        this.hitEntity = hitEntity;

        if (this.tntNum > 0) {
            if (precise) {
                handlePreciseExplosion(this.blockHitPos != null);
            } else {
                handleSphericalExplosion();
            }

        }

    }

    public void handlePreciseExplosion(boolean block) { // Precision in exchange for more explosives.
        if (block) {
            float totalPower = 0.0f;
            for (int i = 0; i < this.tntNum; i++) {
                totalPower += 4 * (0.7f + this.world.random.nextFloat() * 0.6f);
            }
            float h = totalPower / 1.5f;

            float blastResistance = this.world.getBlockState(this.blockHitPos).getBlock().getBlastResistance();

            if (blastResistance > 0.0f) {
                h -= (blastResistance + 0.3f) * 0.6f;
                if (h >= 0.0f) {
                    this.affectedBlocks.add(this.blockHitPos);
                    this.count++;
                }
            } else {
                this.affectedBlocks.add(this.blockHitPos);
                this.count++;
            }
        } else {
            float q = this.tntNum * 2.0f;
            Vec3d vec3d = this.explosionCenter;
            double ac;
            double y;
            double x;
            double w;
            double z;
            double v;
            if (!(hitEntity.isImmuneToExplosion() || !((v = Math.sqrt(hitEntity.squaredDistanceTo(vec3d)) / (double)q) <= 1.0) || (z = Math.sqrt((w = hitEntity.getX() - this.explosionCenter.x) * w + (x = (hitEntity instanceof TntEntity ? hitEntity.getY() : hitEntity.getEyeY()) - this.explosionCenter.y) * x + (y = hitEntity.getZ() - this.explosionCenter.z) * y)) == 0.0)) {
                w /= z;
                x /= z;
                y /= z;
                double aa = Explosion.getExposure(vec3d, hitEntity);
                double ab = (1.0 - v) * aa;
                hitEntity.damage(world.getDamageSources().create(DamageTypes.EXPLOSION), (int)((ab * ab + ab) / 2.0 * 7.0 * (double)q + 1.0));
                if (hitEntity instanceof LivingEntity livingEntity) {
                    ac = ProtectionEnchantment.transformExplosionKnockback(livingEntity, ab);
                } else {
                    ac = ab;
                }
                Vec3d vec3d2 = new Vec3d(w * ac, x * ac, y * ac);
                hitEntity.setVelocity(hitEntity.getVelocity().add(vec3d2));
            }
        }
        explode();
    }

    public void handleSphericalExplosion() {

        if (!world.isClient) {

            double x0 = this.explosionCenter.x;
            double y0 = this.explosionCenter.y;
            double z0 = this.explosionCenter.z;
            int radius = 4;
            int maxRadius = 80;

            this.world.emitGameEvent(this.entity, GameEvent.EXPLODE, this.explosionCenter);

            while (this.blocksDestroyed && radius <= maxRadius) {

                this.blocksDestroyed = false;

                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dy = -radius; dy <= radius; dy++) {
                        for (int dz = -radius; dz <= radius; dz++) {

                            BlockPos targetBlockPos = new BlockPos(((int) Math.floor(x0) + dx), ((int) Math.floor(y0) + dy), ((int) Math.floor(z0) + dz));

                            if (this.checkedBlocks.contains(targetBlockPos)) continue;
                            if (!this.world.isInBuildLimit(targetBlockPos)) break;
                            if (this.world.getBlockState(targetBlockPos).isAir()) continue;

                            double distanceSquared = dx * dx + dy * dy + dz * dz;

                            if (distanceSquared <= radius * radius) {

                                this.checkedBlocks.add(targetBlockPos);

                                double p = 0.3f;
                                double d = (targetBlockPos.getX() + 0.5 - x0) / Math.sqrt(distanceSquared) * p;
                                double e = (targetBlockPos.getY() + 0.5 - y0) / Math.sqrt(distanceSquared) * p;
                                double f = (targetBlockPos.getZ() + 0.5 - z0) / Math.sqrt(distanceSquared) * p;
                                double m = x0;
                                double n = y0;
                                double o = z0;

                                float totalPower = 0.0f;
                                for (int i = 0; i < this.tntNum; i++) {
                                    totalPower += 4 * (0.7f + this.world.random.nextFloat() * 0.6f);
                                }
                                float h = totalPower / 1.5f;

                                for (float g = h; g > 0.0f; g -= 0.22500001f) {

                                    BlockPos blockPos = BlockPos.ofFloored(m, n, o);
                                    BlockState blockState = this.world.getBlockState(blockPos);
                                    FluidState fluidState = this.world.getFluidState(blockPos);

                                    float optional = blockState.isAir() && fluidState.isEmpty() ?
                                            0.0f : Math.max(blockState.getBlock().getBlastResistance(), fluidState.getBlastResistance());

                                    if (optional > 0.0f) g -= (optional + 0.3f) * 0.3f;
                                    if (g <= 0.0f) break;

                                    if (blockPos.equals(targetBlockPos)) {
                                        this.blocksDestroyed = true;
                                        this.affectedBlocks.add(targetBlockPos);
                                        this.count++;
                                        break;
                                    }
                                    m += d;
                                    n += e;
                                    o += f;
                                }


                            }

                        }
                    }
                }
                if (blocksDestroyed) radius += 2;
            }

            float q = radius * 2.0f;
            int k = MathHelper.floor(this.explosionCenter.x - (double)q - 1.0);
            int l = MathHelper.floor(this.explosionCenter.x + (double)q + 1.0);
            int r = MathHelper.floor(this.explosionCenter.y - (double)q - 1.0);
            int s = MathHelper.floor(this.explosionCenter.y + (double)q + 1.0);
            int t = MathHelper.floor(this.explosionCenter.z - (double)q - 1.0);
            int u = MathHelper.floor(this.explosionCenter.z + (double)q + 1.0);
            List<Entity> list = this.world.getOtherEntities(this.entity, new Box(k, r, t, l, s, u));
            Vec3d vec3d = this.explosionCenter;
            for (Entity entity : list) {
                PlayerEntity playerEntity;
                double ac;
                double y;
                double x;
                double w;
                double z;
                double v;
                if (entity.isImmuneToExplosion() || !((v = Math.sqrt(entity.squaredDistanceTo(vec3d)) / (double)q) <= 1.0) || (z = Math.sqrt((w = entity.getX() - this.explosionCenter.x) * w + (x = (entity instanceof TntEntity ? entity.getY() : entity.getEyeY()) - this.explosionCenter.y) * x + (y = entity.getZ() - this.explosionCenter.z) * y)) == 0.0) continue;
                w /= z;
                x /= z;
                y /= z;
                double aa = Explosion.getExposure(vec3d, entity);
                double ab = (1.0 - v) * aa;
                entity.damage(world.getDamageSources().create(DamageTypes.EXPLOSION), (int)((ab * ab + ab) / 2.0 * 7.0 * (double)q + 1.0));
                if (entity instanceof LivingEntity livingEntity) {
                    ac = ProtectionEnchantment.transformExplosionKnockback(livingEntity, ab);
                } else {
                    ac = ab;
                }
                Vec3d vec3d2 = new Vec3d(w * ac, x * ac, y * ac);
                entity.setVelocity(entity.getVelocity().add(vec3d2));
                if (!(entity instanceof PlayerEntity) || (playerEntity = (PlayerEntity)entity).isSpectator() || playerEntity.isCreative() && playerEntity.getAbilities().flying) continue;
                this.affectedPlayers.put(playerEntity, vec3d2);
            }
            explode();


        }


    }
    private static void tryMergeStack(ObjectArrayList<Pair<ItemStack, BlockPos>> stacks, ItemStack stack, BlockPos pos) {
        int i = stacks.size();
        for (int j = 0; j < i; ++j) {
            Pair<ItemStack, BlockPos> pair = stacks.get(j);
            ItemStack itemStack = pair.getFirst();
            if (!ItemEntity.canMerge(itemStack, stack)) continue;
            ItemStack itemStack2 = ItemEntity.merge(itemStack, stack, 16);
            stacks.set(j, Pair.of(itemStack2, pair.getSecond()));
            if (!stack.isEmpty()) continue;
            return;
        }
        stacks.add(Pair.of(stack, pos));
    }
    private void explode() {

        ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList = new ObjectArrayList<>();
        for (BlockPos blockPos : this.affectedBlocks) {
            World world;
            BlockState blockState = this.world.getBlockState(blockPos);

            BlockPos blockPos2 = blockPos.toImmutable();
            this.world.getProfiler().push("explosion_blocks");
            if ((world = this.world) instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld)world;
                BlockEntity blockEntity = blockState.hasBlockEntity() ? this.world.getBlockEntity(blockPos) : null;
                LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(serverWorld).add(LootContextParameters.ORIGIN, Vec3d.ofCenter(blockPos)).add(LootContextParameters.TOOL, ItemStack.EMPTY).addOptional(LootContextParameters.BLOCK_ENTITY, blockEntity).addOptional(LootContextParameters.THIS_ENTITY, this.entity);
                if (this.world.getGameRules().getBoolean(GameRules.TNT_EXPLOSION_DROP_DECAY)) {
                    builder.add(LootContextParameters.EXPLOSION_RADIUS, (float) this.tntNum);
                }
                blockState.onStacksDropped(serverWorld, blockPos, ItemStack.EMPTY, true);
                blockState.getDroppedStacks(builder).forEach(stack -> RealisticExplosion.tryMergeStack(objectArrayList, stack, blockPos2));
            }
            this.world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            this.world.getProfiler().pop();
        }
        for (Pair<ItemStack, BlockPos> pair : objectArrayList) {
            Block.dropStack(this.world, pair.getSecond(), pair.getFirst());
        }
        AdvancedMissile.LOGGER.info(this.count + " blocks destroyed.");
        if (this.world.isClient) {
            this.world.playSound(this.explosionCenter.x, this.explosionCenter.y, this.explosionCenter.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0f, (1.0f + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2f) * 0.7f, false);
            this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.explosionCenter.x, this.explosionCenter.y, this.explosionCenter.z, 1.0, 0.0, 0.0);
        }
    }
}
