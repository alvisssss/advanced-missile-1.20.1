package net.alvisssss.advancedmissile.item.custom;

import net.alvisssss.advancedmissile.util.MissileDispenserBehaviour;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TomahawkMissileItem extends Item {

    public static final float MISSILE_SPEED = 2.55f;

    public TomahawkMissileItem(Settings settings) {
        super(settings);
        DispenserBlock.registerBehavior(this, new MissileDispenserBehaviour());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.literal("Tracks down target given valid data."));
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
