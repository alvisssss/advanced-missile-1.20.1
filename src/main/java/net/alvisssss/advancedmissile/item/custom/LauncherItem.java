package net.alvisssss.advancedmissile.item.custom;


import com.google.common.collect.Lists;
import net.alvisssss.advancedmissile.entity.custom.MissileEntity;
import net.alvisssss.advancedmissile.item.ModItems;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;


public class LauncherItem extends RangedWeaponItem {
    public LauncherItem(Settings settings) {
        super(settings);
    }

    @Override
    public Predicate<ItemStack> getProjectiles() {
        return null;
    }

    @Override
    public Predicate<ItemStack> getHeldProjectiles() {
        return itemStack -> ModItems.MISSILE.equals(itemStack.getItem());
    }

    @Override
    public int getRange() {
        return 8;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient()) {
            // Look for ModItems.MISSILE in the player's inventory
            ItemStack missileStack = findMissileInInventory(user);
            if (!missileStack.isEmpty()) {
                // Spawn MissileEntity and write NBT data from ModItems.MISSILE to the entity
                spawnMissileEntity(world, user, missileStack);
                // Play sound and consume item
                if (!user.getAbilities().creativeMode) {
                    missileStack.decrement(1);
                }
                return TypedActionResult.success(stack);
            }
        }
        return TypedActionResult.pass(stack);
    }

    private ItemStack findMissileInInventory(PlayerEntity user) {
        for (int i = 0; i < user.getInventory().size(); i++) {
            ItemStack stack = user.getInventory().getStack(i);
            if (stack.getItem() == ModItems.MISSILE) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private void spawnMissileEntity(World world, PlayerEntity user, ItemStack missileStack) {
        NbtCompound nbt = missileStack.getOrCreateNbt();
        nbt.putFloat("speed", 3.15f);

        // Test cases
        //MissileEntity missile = new MissileEntity(EntityType.SHULKER_BULLET, world);
        MissileEntity missile1 = new MissileEntity(world, user, world.getEntityById(nbt.getInt("id")), nbt);
        ShulkerBulletEntity shulkerBulletEntity = new ShulkerBulletEntity(world, user, world.getEntityById(nbt.getInt("id")), null);

        missile1.readNbt(missileStack.getNbt());
        //missile.readNbt(missileStack.getNbt());


        //missile1.setPosition(user.getX(), user.getY(), user.getZ());

        Vec3d vec3d = user.getOppositeRotationVector(1.0f);
        Quaternionf quaternionf = new Quaternionf().setAngleAxis(0.0f, vec3d.x, vec3d.y, vec3d.z);
        Vec3d vec3d2 = user.getRotationVec(1.0f);
        Vector3f vector3f = vec3d2.toVector3f().rotate(quaternionf);

        missile1.setVelocity(vector3f.x(), vector3f.y(), vector3f.z(), 3.15f, 1.0f);

        world.spawnEntity(missile1);
    }

    /*
    private static final String CHARGED_KEY = "Charged";
    private static final String CHARGED_PROJECTILES_KEY = "ChargedProjectiles";
    private static final int DEFAULT_PULL_TIME = 25;
    public static final int RANGE = 8;
    private boolean charged = false;
    private boolean loaded = false;
    private static final float field_30867 = 0.2f;
    private static final float field_30868 = 0.5f;
    private static final float DEFAULT_SPEED = 3.15f;
    private static final float FIREWORK_ROCKET_SPEED = 1.6f;

    public LauncherItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public Predicate<ItemStack> getHeldProjectiles() {
        return CROSSBOW_HELD_PROJECTILES;
    }

    @Override
    public Predicate<ItemStack> getProjectiles() {
        return BOW_PROJECTILES;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (LauncherItem.isCharged(itemStack)) {
            LauncherItem.shootAll(world, user, hand, itemStack, LauncherItem.getSpeed(itemStack), 1.0f);
            LauncherItem.setCharged(itemStack, false);
            return TypedActionResult.consume(itemStack);
        }
        if (!user.getProjectileType(itemStack).isEmpty()) {
            if (!LauncherItem.isCharged(itemStack)) {
                this.charged = false;
                this.loaded = false;
                user.setCurrentHand(hand);
            }
            return TypedActionResult.consume(itemStack);
        }
        return TypedActionResult.fail(itemStack);
    }

    private static float getSpeed(ItemStack stack) {
        if (LauncherItem.hasProjectile(stack, Items.FIREWORK_ROCKET)) {
            return 1.6f;
        }
        return 3.15f;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        int i = this.getMaxUseTime(stack) - remainingUseTicks;
        float f = LauncherItem.getPullProgress(i, stack);
        if (f >= 1.0f && !LauncherItem.isCharged(stack) && LauncherItem.loadProjectiles(user, stack)) {
            LauncherItem.setCharged(stack, true);
            SoundCategory soundCategory = user instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE;
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_CROSSBOW_LOADING_END, soundCategory, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.5f + 1.0f) + 0.2f);
        }
    }

    private static boolean loadProjectiles(LivingEntity shooter, ItemStack crossbow) {
        int i = EnchantmentHelper.getLevel(Enchantments.MULTISHOT, crossbow);
        int j = i == 0 ? 1 : 3;
        boolean bl = shooter instanceof PlayerEntity && ((PlayerEntity)shooter).getAbilities().creativeMode;
        ItemStack itemStack = shooter.getProjectileType(crossbow);
        ItemStack itemStack2 = itemStack.copy();
        for (int k = 0; k < j; ++k) {
            if (k > 0) {
                itemStack = itemStack2.copy();
            }
            if (itemStack.isEmpty() && bl) {
                itemStack = new ItemStack(Items.ARROW);
                itemStack2 = itemStack.copy();
            }
            if (LauncherItem.loadProjectile(shooter, crossbow, itemStack, k > 0, bl)) continue;
            return false;
        }
        return true;
    }

    private static boolean loadProjectile(LivingEntity shooter, ItemStack crossbow, ItemStack projectile, boolean simulated, boolean creative) {
        ItemStack itemStack;
        boolean bl;
        if (projectile.isEmpty()) {
            return false;
        }
        boolean bl2 = bl = creative && projectile.getItem() == ModItems.MISSILE;
        if (!(bl || creative || simulated)) {
            itemStack = projectile.split(1);
            if (projectile.isEmpty() && shooter instanceof PlayerEntity) {
                ((PlayerEntity)shooter).getInventory().removeOne(projectile);
            }
        } else {
            itemStack = projectile.copy();
        }
        LauncherItem.putProjectile(crossbow, itemStack);
        return true;
    }

    public static boolean isCharged(ItemStack stack) {
        NbtCompound nbtCompound = stack.getNbt();
        return nbtCompound != null && nbtCompound.getBoolean(CHARGED_KEY);
    }

    public static void setCharged(ItemStack stack, boolean charged) {
        NbtCompound nbtCompound = stack.getOrCreateNbt();
        nbtCompound.putBoolean(CHARGED_KEY, charged);
    }

    private static void putProjectile(ItemStack crossbow, ItemStack projectile) {
        NbtCompound nbtCompound = crossbow.getOrCreateNbt();
        NbtList nbtList = nbtCompound.contains(CHARGED_PROJECTILES_KEY, NbtElement.LIST_TYPE) ? nbtCompound.getList(CHARGED_PROJECTILES_KEY, NbtElement.COMPOUND_TYPE) : new NbtList();
        NbtCompound nbtCompound2 = new NbtCompound();
        projectile.writeNbt(nbtCompound2);
        nbtList.add(nbtCompound2);
        nbtCompound.put(CHARGED_PROJECTILES_KEY, nbtList);
    }

    private static List<ItemStack> getProjectiles(ItemStack crossbow) {
        NbtList nbtList;
        ArrayList<ItemStack> list = Lists.newArrayList();
        NbtCompound nbtCompound = crossbow.getNbt();
        if (nbtCompound != null && nbtCompound.contains(CHARGED_PROJECTILES_KEY, NbtElement.LIST_TYPE) && (nbtList = nbtCompound.getList(CHARGED_PROJECTILES_KEY, NbtElement.COMPOUND_TYPE)) != null) {
            for (int i = 0; i < nbtList.size(); ++i) {
                NbtCompound nbtCompound2 = nbtList.getCompound(i);
                list.add(ItemStack.fromNbt(nbtCompound2));
            }
        }
        return list;
    }

    private static void clearProjectiles(ItemStack crossbow) {
        NbtCompound nbtCompound = crossbow.getNbt();
        if (nbtCompound != null) {
            NbtList nbtList = nbtCompound.getList(CHARGED_PROJECTILES_KEY, NbtElement.LIST_TYPE);
            nbtList.clear();
            nbtCompound.put(CHARGED_PROJECTILES_KEY, nbtList);
        }
    }

    public static boolean hasProjectile(ItemStack crossbow, Item projectile) {
        return LauncherItem.getProjectiles(crossbow).stream().anyMatch(s -> s.isOf(projectile));
    }

    private static void shoot(World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, boolean creative, float speed, float divergence, float simulated) {
        ProjectileEntity projectileEntity;
        if (world.isClient) {
            return;
        }
        boolean bl = projectile.isOf(ModItems.MISSILE);
        if (bl) {
            Entity target;
            if (projectile.getNbt() != null && projectile.getNbt().getInt("id") != 0) {
                target = world.getEntityById(projectile.getNbt().getInt("id"));
            } else {
                target = shooter;
            }
            projectileEntity = new MissileEntity(world, shooter, target, null);
            projectileEntity.readNbt(projectile.getNbt());
        } else {
            projectileEntity = LauncherItem.createArrow(world, shooter, crossbow, projectile);
            if (creative || simulated != 0.0f) {
                ((PersistentProjectileEntity)projectileEntity).pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            }
        }
        if (shooter instanceof CrossbowUser) {
            CrossbowUser crossbowUser = (CrossbowUser)((Object)shooter);
            crossbowUser.shoot(crossbowUser.getTarget(), crossbow, projectileEntity, simulated);
        } else {
            Vec3d vec3d = shooter.getOppositeRotationVector(1.0f);
            Quaternionf quaternionf = new Quaternionf().setAngleAxis((double)(simulated * ((float)Math.PI / 180)), vec3d.x, vec3d.y, vec3d.z);
            Vec3d vec3d2 = shooter.getRotationVec(1.0f);
            Vector3f vector3f = vec3d2.toVector3f().rotate(quaternionf);
            projectileEntity.setVelocity(vector3f.x(), vector3f.y(), vector3f.z(), speed, divergence);
        }
        crossbow.damage(bl ? 3 : 1, shooter, e -> e.sendToolBreakStatus(hand));
        world.spawnEntity(projectileEntity);
    }

    private static PersistentProjectileEntity createArrow(World world, LivingEntity entity, ItemStack crossbow, ItemStack arrow) {
        ArrowItem arrowItem = (ArrowItem)(arrow.getItem() instanceof ArrowItem ? arrow.getItem() : Items.ARROW);
        PersistentProjectileEntity persistentProjectileEntity = arrowItem.createArrow(world, arrow, entity);
        if (entity instanceof PlayerEntity) {
            persistentProjectileEntity.setCritical(true);
        }
        persistentProjectileEntity.setSound(SoundEvents.ITEM_CROSSBOW_HIT);
        persistentProjectileEntity.setShotFromCrossbow(true);
        int i = EnchantmentHelper.getLevel(Enchantments.PIERCING, crossbow);
        if (i > 0) {
            persistentProjectileEntity.setPierceLevel((byte)i);
        }
        return persistentProjectileEntity;
    }

    public static void shootAll(World world, LivingEntity entity, Hand hand, ItemStack stack, float speed, float divergence) {
        List<ItemStack> list = LauncherItem.getProjectiles(stack);
        for (int i = 0; i < list.size(); ++i) {
            boolean bl;
            ItemStack itemStack = list.get(i);
            boolean bl2 = bl = entity instanceof PlayerEntity && ((PlayerEntity)entity).getAbilities().creativeMode;
            if (itemStack.isEmpty()) continue;
            if (i == 0) {
                LauncherItem.shoot(world, entity, hand, stack, itemStack, bl, speed, divergence, 0.0f);
                continue;
            }
            if (i == 1) {
                LauncherItem.shoot(world, entity, hand, stack, itemStack, bl, speed, divergence, -10.0f);
                continue;
            }
            if (i != 2) continue;
            LauncherItem.shoot(world, entity, hand, stack, itemStack, bl, speed, divergence, 10.0f);
        }
        LauncherItem.postShoot(world, entity, stack);
    }




    private static void postShoot(World world, LivingEntity entity, ItemStack stack) {
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
            if (!world.isClient) {
                Criteria.SHOT_CROSSBOW.trigger(serverPlayerEntity, stack);
            }
            serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
        }
        LauncherItem.clearProjectiles(stack);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient) {
            int i = EnchantmentHelper.getLevel(Enchantments.QUICK_CHARGE, stack);
            SoundEvent soundEvent2 = i == 0 ? SoundEvents.ITEM_CROSSBOW_LOADING_MIDDLE : null;
            float f = (float)(stack.getMaxUseTime() - remainingUseTicks) / (float)LauncherItem.getPullTime(stack);
            if (f < 0.2f) {
                this.charged = false;
                this.loaded = false;
            }
            if (f >= 0.2f && !this.charged) {
                this.charged = true;
            }
            if (f >= 0.5f && soundEvent2 != null && !this.loaded) {
                this.loaded = true;
            }
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return LauncherItem.getPullTime(stack) + 3;
    }

    public static int getPullTime(ItemStack stack) {

        return 100;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.CROSSBOW;
    }


    private static float getPullProgress(int useTicks, ItemStack stack) {
        float f = (float)useTicks / (float)LauncherItem.getPullTime(stack);
        if (f > 1.0f) {
            f = 1.0f;
        }
        return f;
    }

    @Override
    public boolean isUsedOnRelease(ItemStack stack) {
        return stack.isOf(this);
    }

    @Override
    public int getRange() {
        return 8;
    }

     */
}