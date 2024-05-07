package net.alvisssss.advancedmissile.item.custom;

import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class MissileItem extends Item {

    public MissileItem(Settings settings) {
        super(settings);
    }
    // Changes the display text on the item according to the data stored.
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.literal("Tracks down target given valid data."));
        tooltip.add(Text.literal("Here be dragons for distance ~500."));
        if (stack.hasNbt()) {

            if (stack.getNbt().contains("name")) {
                tooltip.add(Text.literal("Name: " + stack.getNbt().getString("name")));
                tooltip.add(Text.literal("Target XYZ: " + ((int) stack.getNbt().getDouble("TXD")) + " " + (((int) stack.getNbt().getDouble("TYD"))) + " " + ((int) stack.getNbt().getDouble("TZD"))));
            }

            if (stack.getNbt().contains("fuel_count")) {
                tooltip.add(Text.literal("Time(s): 20 + " + stack.getNbt().getInt("fuel_count")));
            } else {
                tooltip.add(Text.literal("Time(s): 20"));
            }

            if (stack.getNbt().contains("tnt_count")) {
                tooltip.add(Text.literal("Addition Power: " + stack.getNbt().getInt("tnt_count")));
            }
        }
    }
}
