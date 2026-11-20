package dev.aurora;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Extra rain impacts on the floor. Uses vanilla particles only (no custom render code),
 * a hard per-tick budget, and the frame-time governor, so cost stays tiny and it plays
 * nicely with Sodium / Iris / Lunar.
 */
final class RainFx {
    private RainFx() {}

    static void tick(ClientLevel level, LocalPlayer player) {
        Config c = Config.get();
        if (!c.rainSplashes) return;

        float rain = level.getRainLevel(1.0F);
        if (rain < 0.2F) return;

        int budget = Math.min(32, Math.round(c.rainDensity * rain * Perf.scale()));
        if (budget <= 0) return;

        RandomSource r = level.getRandom();
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();

        for (int i = 0; i < budget; i++) {
            double ang = r.nextDouble() * Math.PI * 2.0;
            double dist = r.nextDouble() * c.rainRadius; // denser near you
            int x = Mth.floor(player.getX() + Math.cos(ang) * dist);
            int z = Mth.floor(player.getZ() + Math.sin(ang) * dist);

            probe.set(x, player.getBlockY(), z);
            if (!level.hasChunkAt(probe)) continue;

            BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, probe);
            if (!level.isRainingAt(top)) continue; // sky access + biome precipitation check

            BlockPos ground = top.below();
            BlockState bs = level.getBlockState(ground);
            double h;
            if (!bs.getFluidState().isEmpty()) {
                h = bs.getFluidState().getHeight(level, ground);
            } else {
                VoxelShape shape = bs.getCollisionShape(level, ground);
                h = shape.isEmpty() ? 0.0 : shape.max(Direction.Axis.Y);
            }

            double px = x + r.nextDouble();
            double pz = z + r.nextDouble();
            double py = ground.getY() + h + 0.03;

            if (r.nextInt(8) == 0) {
                // occasional little crown-splash
                level.addParticle(ParticleTypes.SPLASH, px, py, pz,
                        (r.nextDouble() - 0.5) * 0.06, 0.0, (r.nextDouble() - 0.5) * 0.06);
            } else {
                level.addParticle(ParticleTypes.RAIN, px, py, pz, 0.0, 0.0, 0.0);
            }
        }
    }
}
