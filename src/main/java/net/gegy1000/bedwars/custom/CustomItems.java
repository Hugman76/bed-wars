package net.gegy1000.bedwars.custom;

import net.gegy1000.bedwars.BedWarsMod;
import net.gegy1000.bedwars.map.trace.RegionTraceMode;
import net.gegy1000.bedwars.map.trace.RegionTracer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class CustomItems {
    public static final CustomItem ADD_REGION = CustomItem.builder()
            .id(new Identifier(BedWarsMod.ID, "add_region"))
            .name(new LiteralText("Add Region"))
            .onUse(CustomItems::addRegion)
            .onSwingHand(CustomItems::changeRegionMode)
            .register();

    private static TypedActionResult<ItemStack> addRegion(PlayerEntity player, World world, Hand hand) {
        if (player instanceof RegionTracer) {
            RegionTracer constructor = (RegionTracer) player;

            RegionTraceMode traceMode = constructor.getMode();

            BlockPos pos = traceMode.tryTrace(player);
            if (pos != null) {
                if (constructor.isTracing()) {
                    constructor.finishTracing(pos);
                    player.sendMessage(new LiteralText("Use /map region commit <name> to add this region"), true);
                } else {
                    constructor.startTracing(pos);
                }
            }
        }

        return TypedActionResult.pass(ItemStack.EMPTY);
    }

    private static void changeRegionMode(PlayerEntity player, Hand hand) {
        if (player instanceof RegionTracer) {
            RegionTracer constructor = (RegionTracer) player;

            RegionTraceMode nextMode = constructor.getMode().next();
            constructor.setMode(nextMode);

            player.sendMessage(new LiteralText("Changed trace mode to: ").append(nextMode.getName()), true);
        }
    }
}