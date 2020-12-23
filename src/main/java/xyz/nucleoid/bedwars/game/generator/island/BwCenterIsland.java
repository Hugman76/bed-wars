package xyz.nucleoid.bedwars.game.generator.island;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.util.BlockBounds;

public final class BwCenterIsland {
    private final NoiseIslandConfig config;
    private final BlockPos origin;

    public BwCenterIsland(NoiseIslandConfig config, BlockPos origin) {
        this.config = config;
        this.origin = origin;
    }

    public void addTo(BwMap map, MapTemplate template, long seed) {
        NoiseIslandGenerator generator = this.config.createGenerator(this.origin, seed);
        generator.addTo(template);

        // TODO: scale with team count
        Direction[] horizontals = new Direction[] { Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST };

        for (Direction horizontal : horizontals) {
            this.addEmeraldSpawn(map, template, this.origin.offset(horizontal, 8));
        }

        this.addCenterSpawn(map, template);
    }

    private void addEmeraldSpawn(BwMap map, MapTemplate template, BlockPos pos) {
        BlockPos surfacePos = template.getTopPos(pos.getX(), pos.getZ(), Heightmap.Type.WORLD_SURFACE_WG);

        template.setBlockState(surfacePos, Blocks.EMERALD_BLOCK.getDefaultState());
        map.addEmeraldGenerator(BlockBounds.of(surfacePos.up()));
        map.addProtectedBlock(surfacePos.asLong());
    }

    private void addCenterSpawn(BwMap map, MapTemplate template) {
        BlockPos surfacePos = template.getTopPos(this.origin.getX(), this.origin.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
        map.setCenterSpawn(surfacePos.up());
    }
}
