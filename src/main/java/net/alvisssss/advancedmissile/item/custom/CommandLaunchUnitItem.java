package net.alvisssss.advancedmissile.item.custom;

import net.alvisssss.advancedmissile.entity.custom.MissileEntity;
import net.alvisssss.advancedmissile.item.ModItems;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Predicate;

public class CommandLaunchUnitItem extends RangedWeaponItem {

    private static final String LOADED_KEY = "Loaded";
    public static final float MISSILE_SPEED = 2.55f;
    private boolean charged = false;
    private boolean loaded = false;

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

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (!findMissileInInventory(user).isEmpty()) {
            if (!CommandLaunchUnitItem.isLoaded(itemStack)) {
                this.charged = false;
                this.loaded = false;
                user.setCurrentHand(hand);
            }
            return TypedActionResult.consume(itemStack);
        }
        return TypedActionResult.fail(itemStack);
    }


    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        /*
        int i = this.getMaxUseTime(stack) - remainingUseTicks;
        float f = CommandLaunchUnitItem.getPullProgress(i, stack);
        if (f >= 1.0f && !CommandLaunchUnitItem.isLoaded(stack) && CommandLaunchUnitItem.loadMissiles(user, stack)) {
            CommandLaunchUnitItem.setLoaded(stack, true);
        } else if (f >= 1.0f && CommandLaunchUnitItem.isLoaded(stack) && user.isSneaking() && unloadMissile(user, stack)) {
            CommandLaunchUnitItem.setLoaded(stack, false);
        }

         */
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

        MissileEntity missile1 = new MissileEntity(world, shooter, nbt);

        Vec3d vec3d = shooter.getOppositeRotationVector(1.0f);
        Quaternionf quaternionf = new Quaternionf().setAngleAxis(0.0f, vec3d.x, vec3d.y, vec3d.z);
        Vec3d vec3d2 = shooter.getRotationVec(1.0f);
        Vector3f vector3f = vec3d2.toVector3f().rotate(quaternionf);
        missile1.setVelocity(vector3f.x(), vector3f.y(), vector3f.z(), speed, 1.0f);

        world.spawnEntity(missile1);

        Vec3d cameraPos = shooter.getCameraPosVec(1.0f);
        Vec3d lookVector = shooter.getRotationVec(1.0f);

        double newX = cameraPos.x + lookVector.x;
        double newY = cameraPos.y + lookVector.y;
        double newZ = cameraPos.z + lookVector.z;

        missile1.refreshPositionAndAngles(newX, newY, newZ, missile1.getYaw(), missile1.getPitch());

    }


    public static boolean loadMissiles(LivingEntity shooter, ItemStack clu) {

        boolean bl = shooter instanceof PlayerEntity && ((PlayerEntity)shooter).getAbilities().creativeMode;
        ItemStack itemStack;
        if (shooter instanceof PlayerEntity) {
            itemStack = CommandLaunchUnitItem.findMissileInInventory((PlayerEntity) shooter);
        } else {
            itemStack = ItemStack.EMPTY;
        }

        if (itemStack.isEmpty() && bl) {
            itemStack = new ItemStack(ModItems.MISSILE);
        }
        return CommandLaunchUnitItem.loadMissile(shooter, clu, itemStack, bl);

    }

    private static boolean loadMissile(LivingEntity shooter, ItemStack clu, ItemStack missile, boolean creative) {
        ItemStack itemStack;

        if (missile.isEmpty()) {
            return false;
        }
        if (!creative && missile.getItem() == ModItems.MISSILE) {
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
    private static boolean unloadMissile(LivingEntity shooter, ItemStack clu) {
        if (!((PlayerEntity)shooter).getAbilities().creativeMode) {
            ItemStack missile = new ItemStack(ModItems.MISSILE, 1);
            NbtCompound missileNbt = missile.getOrCreateNbt();
            missileNbt.copyFrom(clu.getOrCreateNbt());
            missileNbt.remove(LOADED_KEY);

            int returnStackSlot = findSlotInInventory(((PlayerEntity) shooter), missileNbt);
            if (returnStackSlot != -1) {
                ((PlayerEntity) shooter).getInventory().getStack(returnStackSlot).increment(1);
                clearMissileNbt(clu);
            } else {
                ((PlayerEntity) shooter).dropItem(missile, false);
            }
        }

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
    }

    private static ItemStack findMissileInInventory(PlayerEntity user) {

        for (int i = 0; i < user.getInventory().size(); i++) {
            ItemStack stack = user.getInventory().getStack(i);
            if (stack.getItem() == ModItems.MISSILE) {
                return stack;
            }
        }
        return user.getAbilities().creativeMode ? new ItemStack(ModItems.MISSILE) : ItemStack.EMPTY;
    }

    private static int findSlotInInventory(PlayerEntity user, NbtCompound missile) {
        for (int i = 0; i < user.getInventory().size(); i++) {
            ItemStack itemStack = user.getInventory().getStack(i);
            if (itemStack.getItem() == ModItems.MISSILE && itemStack.getCount() < itemStack.getMaxCount() && itemStack.getNbt() != null && itemStack.getNbt().equals(missile)) {
                return i;
            }
        }
        return user.getInventory().getEmptySlot();
    }

    public static boolean isLoaded(ItemStack stack) {
        NbtCompound nbtCompound = stack.getNbt();
        return nbtCompound != null && nbtCompound.getBoolean(LOADED_KEY);
    }

    public static void setLoaded(ItemStack stack, boolean loaded) {
        NbtCompound nbtCompound = stack.getOrCreateNbt();
        nbtCompound.putBoolean(LOADED_KEY, loaded);
    }
    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    public static int getPullTime(ItemStack stack) {
        return 40;
    }

    private static float getPullProgress(int useTicks, ItemStack stack) {
        float f = (float)useTicks / (float)CommandLaunchUnitItem.getPullTime(stack);
        if (f > 1.0f) {
            f = 1.0f;
        }
        return f;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient) {
            float f = (float)(stack.getMaxUseTime() - remainingUseTicks) / (float) CommandLaunchUnitItem.getPullTime(stack);
            if (f < 0.2f) {
                this.charged = false;
                this.loaded = false;
            }
            if (f >= 0.2f && !this.charged) {
                this.charged = true;
            }
            if (f >= 0.5f && !this.loaded) {
                this.loaded = true;
            }
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
        tooltip.add(Text.literal("By default, hold [R] to reload."));
        tooltip.add(Text.literal("Press [Left alt] to fire."));
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
