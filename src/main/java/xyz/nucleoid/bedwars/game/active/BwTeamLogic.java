package xyz.nucleoid.bedwars.game.active;

import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.active.modifiers.BwGameTriggers;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.util.BlockBounds;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.Nullable;

public final class BwTeamLogic {
    private final BwActive game;

    BwTeamLogic(BwActive game) {
        this.game = game;
    }

    public void applyEnchantments(GameTeam team) {
        this.game.participantsFor(team).forEach(participant -> {
            ServerPlayerEntity player = participant.player();
            if (player != null) {
                this.game.playerLogic.applyEnchantments(player, participant);
            }
        });
    }

    public boolean canRespawn(BwParticipant participant) {
        return this.tryRespawn(participant) != null;
    }

    @Nullable
    public BwMap.TeamSpawn tryRespawn(BwParticipant participant) {
        BwActive.TeamState teamState = this.game.getTeam(participant.team);
        if (teamState != null && teamState.hasBed) {
            return this.game.map.getTeamSpawn(participant.team);
        }

        return null;
    }

    public void onBedBroken(ServerPlayerEntity player, BlockPos pos) {
        GameTeam destroyerTeam = null;

        BwParticipant participant = this.game.getParticipant(player);
        if (participant != null && !participant.eliminated) {
            destroyerTeam = participant.team;
        }

        Bed bed = this.findBed(pos);
        if (bed == null || bed.team.equals(destroyerTeam)) {
            return;
        }

        this.game.broadcast.broadcastBedBroken(player, bed.team, destroyerTeam);

        this.removeBed(bed.team);
    }

    public void removeBed(GameTeam team) {
        BwActive.TeamState teamState = this.game.getTeam(team);
        if (teamState == null || !teamState.hasBed) {
            return;
        }

        teamState.hasBed = false;

        BlockBounds bed = this.game.map.getTeamRegions(team).bed;

        ServerWorld world = this.game.world;
        bed.forEach(p -> {
            world.setBlockState(p, Blocks.AIR.getDefaultState(), 0b100010);
        });

        this.game.triggerModifiers(BwGameTriggers.BED_BROKEN);
    }

    @Nullable
    private Bed findBed(BlockPos pos) {
        for (GameTeam team : this.game.config.teams) {
            BwMap.TeamRegions teamRegions = this.game.map.getTeamRegions(team);
            BlockBounds bed = teamRegions.bed;
            if (bed != null && bed.contains(pos)) {
                return new Bed(team, bed);
            }
        }
        return null;
    }

    private static class Bed {
        final GameTeam team;
        final BlockBounds bounds;

        Bed(GameTeam team, BlockBounds bounds) {
            this.team = team;
            this.bounds = bounds;
        }
    }
}
