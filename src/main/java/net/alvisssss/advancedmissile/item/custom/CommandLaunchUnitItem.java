package net.alvisssss.advancedmissile.item.custom;

import net.alvisssss.advancedmissile.AdvancedMissile;
import net.alvisssss.advancedmissile.entity.custom.MissileEntity;
import net.alvisssss.advancedmissile.item.ModItems;
import net.alvisssss.advancedmissile.keybind.Keybindings;
import net.alvisssss.advancedmissile.util.TargetEntityRenderer;
import net.alvisssss.advancedmissile.util.SightManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class CommandLaunchUnitItem extends RangedWeaponItem {

    public static final String LOADED_KEY = "Loaded";
    public static final String CHANGE_MODE_KEY = "isPrecise";
    public static final float MISSILE_SPEED = 2.55f;
    private Entity lastLookedEntity = null;
    private int consecutiveCollisionCheck = 0;
    private int ticksSinceLastCollisionCheck = 0;
    private static final int collisionCheckInterval = 5; // In ticks
    private static final int requiredCollisionCheckNum = 12;

    public CommandLaunchUnitItem(Settings settings) {
        super(settings.maxDamage(20));
    }

    @Override
    public Predicate<ItemStack> getProjectiles() {
        return null;
    }

    @Override
    public int getRange() {
        return 8;
    }

    public static float getMissileSpeed() {
        return MISSILE_SPEED;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        ticksSinceLastCollisionCheck = 0;
        consecutiveCollisionCheck = 0;
        lastLookedEntity = null;
        SightManager.resetZoomLevel();
        SightManager.resetTargetUuid();
        return TypedActionResult.fail(itemStack);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (user instanceof PlayerEntity && world != null && !world.isClient) {
            ticksSinceLastCollisionCheck++;
            if (ticksSinceLastCollisionCheck >= collisionCheckInterval) {
                targetCheck(world, (PlayerEntity) user, stack);
                ticksSinceLastCollisionCheck = 0;
            }
        }
    }
    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        return super.finishUsing(stack, world, user);
    }

    public static void shootAll(World world, LivingEntity entity, Hand hand, ItemStack stack, float speed) {
        CommandLaunchUnitItem.shoot(world, entity, hand, stack, speed);
        CommandLaunchUnitItem.clearMissileNbt(stack);
    }

    private static void shoot(World world, LivingEntity shooter, Hand hand, ItemStack clu, float speed) {
        if (world.isClient) {
            return;
        }

        clu.damage(1, shooter, e -> e.sendToolBreakStatus(hand));

        NbtCompound nbt = clu.getOrCreateNbt();
        nbt.putFloat("speed", speed);

        MissileEntity missile = new MissileEntity(world, nbt);

        Vec3d vec3d = shooter.getOppositeRotationVector(1.0f);
        Quaternionf quaternionf = new Quaternionf().setAngleAxis(0.0f, vec3d.x, vec3d.y, vec3d.z);
        Vec3d vec3d2 = shooter.getRotationVec(1.0f);
        Vector3f vector3f = vec3d2.toVector3f().rotate(quaternionf);
        missile.setVelocity(vector3f.x(), vector3f.y(), vector3f.z(), speed, 1.0f);

        world.spawnEntity(missile);

        Vec3d cameraPos = shooter.getCameraPosVec(1.0f);
        Vec3d lookVector = shooter.getRotationVec(1.0f);

        double newX = cameraPos.x + lookVector.x;
        double newY = cameraPos.y + lookVector.y;
        double newZ = cameraPos.z + lookVector.z;

        missile.refreshPositionAndAngles(newX, newY, newZ, missile.getYaw(), missile.getPitch());

        shooter.sendMessage(Text.literal("Initial Position: " + missile.getPos()));
        shooter.sendMessage(Text.literal("Initial Velocity: " + missile.getVelocity()));
        shooter.sendMessage(Text.literal("Angle (Pitch and Yaw): " + shooter.getPitch() + " " + shooter.getYaw()));
        shooter.sendMessage(Text.literal( "Time: " + world.getTimeOfDay()));

    }


    public static boolean loadMissiles(LivingEntity shooter, ItemStack clu) {

        boolean bl = shooter instanceof PlayerEntity && ((PlayerEntity)shooter).getAbilities().creativeMode;

        ItemStack itemStack = CommandLaunchUnitItem.findMissileInInventory(shooter);

        return CommandLaunchUnitItem.loadMissile(shooter, clu, itemStack, bl);

    }

    private static boolean loadMissile(LivingEntity shooter, ItemStack clu, ItemStack missile, boolean creative) {
        ItemStack itemStack;

        if (missile.isEmpty()) {
            return false;
        }
        if (!creative && missile.getItem() == ModItems.JAVELIN_MISSILE) {
            itemStack = missile.split(1);
            if (missile.isEmpty() && shooter instanceof PlayerEntity) {
                ((PlayerEntity)shooter).getInventory().removeOne(missile);
            }
        } else {
            itemStack = missile.copy();
        }
        CommandLaunchUnitItem.putMissileNbt(clu, itemStack);
        return true;
    }

    private static void putMissileNbt(ItemStack clu, ItemStack missile) {
        NbtCompound nbtCompound = clu.getOrCreateNbt();
        NbtCompound missileNbt = missile.getOrCreateNbt();
        nbtCompound.copyFrom(missileNbt);
    }

    private static void clearMissileNbt(ItemStack clu) {
        NbtCompound nbtCompound = clu.getNbt();
        if (nbtCompound != null) {
            nbtCompound.remove("tnt_count");
            nbtCompound.remove("fuel_count");
            nbtCompound.remove("isBlock");
            nbtCompound.remove("id");
            nbtCompound.remove("Target");
            nbtCompound.remove("name");
            nbtCompound.remove("TXD");
            nbtCompound.remove("TYD");
            nbtCompound.remove("TZD");
            nbtCompound.remove("speed");
        }
        SightManager.resetTargetUuid();
    }

    private static ItemStack findMissileInInventory(LivingEntity user) {
        if (user instanceof PlayerEntity player) {
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (stack.getItem() == ModItems.JAVELIN_MISSILE) {
                    return stack;
                }
            }
            return player.getAbilities().creativeMode ? new ItemStack(ModItems.JAVELIN_MISSILE) : ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    public static boolean isLoaded(ItemStack stack) {
        NbtCompound nbtCompound = stack.getNbt();
        return nbtCompound != null && nbtCompound.getBoolean(LOADED_KEY);
    }

    public static void setLoaded(ItemStack stack, boolean loaded) {
        NbtCompound nbtCompound = stack.getOrCreateNbt();
        nbtCompound.putBoolean(LOADED_KEY, loaded);
    }

    public static void setPreciseMode(ItemStack stack, boolean isPrecise) {
        NbtCompound nbtCompound = stack.getOrCreateNbt();
        nbtCompound.putBoolean(CHANGE_MODE_KEY, isPrecise);
    }

    public static boolean getPreciseMode(ItemStack stack) {
        NbtCompound nbtCompound = stack.getNbt();
        return nbtCompound != null && nbtCompound.getBoolean(CHANGE_MODE_KEY);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    private void targetCheck(World world, PlayerEntity player, ItemStack itemStack) {
        HashMap<UUID, Double> uuidDoubleHashMap = TargetEntityRenderer.getUuidDoubleHashMap();

        Double squaredDistance = 0.0;
        ServerWorld serverWorld = (ServerWorld) world;
        LivingEntity target = null;

        for (UUID uuid : uuidDoubleHashMap.keySet()) {
            target = (LivingEntity) serverWorld.getEntity(uuid);
            squaredDistance = uuidDoubleHashMap.get(uuid);
        }

        if (uuidDoubleHashMap.isEmpty()) {
            lastLookedEntity = null;
            consecutiveCollisionCheck = 0;
            SightManager.resetTargetUuid();
        } else if (uuidDoubleHashMap.size() > 1) {
            lastLookedEntity = null;
            consecutiveCollisionCheck = 0;
            SightManager.resetTargetUuid();
            player.sendMessage(Text.literal("Too many targets!"));
        } else if (squaredDistance < 2500) {
            lastLookedEntity = null;
            consecutiveCollisionCheck = 0;
            player.sendMessage(Text.literal("Target too close!"));
        } else if (target == null) {
            AdvancedMissile.LOGGER.info("Error in obtaining target as target is null!");
        } else if (target != lastLookedEntity) {
            lastLookedEntity = target;
            consecutiveCollisionCheck = 1;
        } else {
            consecutiveCollisionCheck++;
            if (consecutiveCollisionCheck >= requiredCollisionCheckNum) {
                player.sendMessage(Text.literal("Missile Target: " + target.getName().getString()));
                NbtCompound nbt = itemStack.getOrCreateNbt();
                nbt.putInt("id", target.getId());
                nbt.putUuid("Target", target.getUuid());
                nbt.putBoolean("isBlock", false);
                SightManager.setTargetUuid(target.getUuid());
            }
        }
    }

    private void performCollisionCheck(PlayerEntity player, ItemStack itemStack) {
        double d = 300D;
        Entity entity2 = MinecraftClient.getInstance().getCameraEntity();
        if (entity2 == null || MinecraftClient.getInstance().world == null)  return;
        Vec3d cameraPos = entity2.getCameraPosVec(1.0f);
        Vec3d direction = entity2.getRotationVec(1.0f);
        Vec3d rayEnd = cameraPos.add(direction.multiply(d));
        Box box = entity2.getBoundingBox().stretch(direction.multiply(d)).expand(1.0D, 1.0D, 1.0D);

        EntityHitResult entityHitResult = ProjectileUtil.getEntityCollision(player.getWorld(), entity2, cameraPos, rayEnd, box, entity -> !entity.isSpectator() && entity.canHit());

        if (entityHitResult != null && entityHitResult.getEntity() != null) {
            Entity entityHit = entityHitResult.getEntity();
            if (entityHitResult.getEntity().getPos().squaredDistanceTo(player.getPos()) >= 2500) {
                player.sendMessage(Text.literal("Looking at: " + entityHit.getName().getString() + consecutiveCollisionCheck));
                if (entityHit == lastLookedEntity) {
                    consecutiveCollisionCheck++;
                    if (consecutiveCollisionCheck >= requiredCollisionCheckNum) {
                        player.sendMessage(Text.literal("Missile Target: " + entityHit.getName().getString()));
                        NbtCompound nbt = itemStack.getOrCreateNbt();
                        nbt.putInt("id", entityHit.getId());
                        nbt.putUuid("Target", entityHit.getUuid());
                        nbt.putBoolean("isBlock", false);
                        SightManager.setTargetUuid(entityHit.getUuid());
                    }
                } else {
                    lastLookedEntity = entityHit;
                    consecutiveCollisionCheck = 1;
                }
            } else {
                player.sendMessage(Text.literal("Target too close!"));
            }

        } else {
            lastLookedEntity = null;
            consecutiveCollisionCheck = 0;
            player.sendMessage(Text.literal("Looking at: null"));
        }
    }

    @Override
    public boolean isUsedOnRelease(ItemStack stack) {
        return stack.isOf(this);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.CROSSBOW;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("Hold [" + Keybindings.reloadKey.getBoundKeyLocalizedText().getString() + "] to reload."));
        tooltip.add(Text.literal("Press [" + Keybindings.fireKey.getBoundKeyLocalizedText().getString() + "] to fire."));
        tooltip.add(Text.literal("Press [" + Keybindings.modeKey.getBoundKeyLocalizedText().getString() + "] to change mode."));
        if (stack.getNbt() != null) {

            if (stack.getNbt().contains("name")) {
                tooltip.add(Text.literal("Name: " + stack.getNbt().getString("name")));
                tooltip.add(Text.literal("Target XYZ: " + ((int) stack.getNbt().getDouble("TXD")) + " " + (((int) stack.getNbt().getDouble("TYD"))) + " " + ((int) stack.getNbt().getDouble("TZD"))));
            }

            if (stack.getNbt().contains("fuel_count")) {
                tooltip.add(Text.literal("Time(s): 20 + " + stack.getNbt().getInt("fuel_count")));
            }

            if (stack.getNbt().contains("tnt_count")) {
                tooltip.add(Text.literal("Addition Power: " + stack.getNbt().getInt("tnt_count")));
            }
        }
    }
}
