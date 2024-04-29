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
            ItemStack missileStack = findMissileInInventory(user, hand);
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

    private ItemStack findMissileInInventory(PlayerEntity user, Hand hand) {
        // Search offhand first.
        if (hand == Hand.MAIN_HAND) {
            ItemStack stack = user.getInventory().getStack(45);
            if (stack.getItem() == ModItems.MISSILE) {
                return stack;
            }
        }
        // Search hotbar.
        for (int i = 36; i <= 44; i++) {
            ItemStack stack = user.getInventory().getStack(i);
            if (stack.getItem() == ModItems.MISSILE) {
                return stack;
            }
        }
        // Search rest oof the inventory.
        for (int i = 0; i < user.getInventory().size(); i++) {
            if (i >= 36 && i <= 45) continue;
            ItemStack stack = user.getInventory().getStack(i);
            if (stack.getItem() == ModItems.MISSILE) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private void spawnMissileEntity(World world, PlayerEntity user, ItemStack missileStack) {

        float speed = 2.55f; // Risk crashing over 3.15f.

        NbtCompound nbt = missileStack.getOrCreateNbt();
        nbt.putFloat("speed", speed);

        MissileEntity missile1 = new MissileEntity(world, user, nbt);

        missile1.readNbt(missileStack.getNbt());

        Vec3d vec3d = user.getOppositeRotationVector(1.0f);
        Quaternionf quaternionf = new Quaternionf().setAngleAxis(0.0f, vec3d.x, vec3d.y, vec3d.z);
        Vec3d vec3d2 = user.getRotationVec(1.0f);
        Vector3f vector3f = vec3d2.toVector3f().rotate(quaternionf);

        missile1.setVelocity(vector3f.x(), vector3f.y(), vector3f.z(), speed, 1.0f);

        world.spawnEntity(missile1);
    }
}