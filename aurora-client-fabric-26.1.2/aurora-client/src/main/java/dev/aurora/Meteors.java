package dev.aurora;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Occasional shooting stars on clear Overworld nights. At most 3 alive at once,
 * ~2 particles per meteor per tick, only while the game is running smoothly.
 */
final class Meteors {
    private static final class Meteor {
        double x, y, z, vx, vy, vz;
        int life = 22;
    }

    private static final List<Meteor> ACTIVE = new ArrayList<>();

    private Meteors() {}

    static void reset() {
        ACTIVE.clear();
    }

    static void tick(ClientLevel level, LocalPlayer p) {
        if (!Config.get().meteors || level.dimension() != Level.OVERWORLD) {
            ACTIVE.clear();
            return;
        }
        RandomSource r = level.getRandom();

        for (Iterator<Meteor> it = ACTIVE.iterator(); it.hasNext(); ) {
            Meteor m = it.next();
            for (int s = 0; s < 2; s++) {
                double t = s * 0.5;
                level.addAlwaysVisibleParticle(ParticleTypes.END_ROD,
                        m.x + m.vx * t, m.y + m.vy * t, m.z + m.vz * t,
                        (r.nextDouble() - 0.5) * 0.01, -0.005, (r.nextDouble() - 0.5) * 0.01);
            }
            m.x += m.vx;
            m.y += m.vy;
            m.z += m.vz;
            if (--m.life <= 0) it.remove();
        }

        if (ACTIVE.size() < 3
                && Perf.scale() > 0.5f
                && level.getRainLevel(1.0F) < 0.1F
                && r.nextInt(500) == 0
                && Soft.isNight(level)) {
            double ang = r.nextDouble() * Math.PI * 2.0;
            double radius = 30 + r.nextDouble() * 25;
            Meteor m = new Meteor();
            m.x = p.getX() + Math.cos(ang) * radius;
            m.z = p.getZ() + Math.sin(ang) * radius;
            m.y = p.getY() + 38 + r.nextDouble() * 20;
            double dir = r.nextBoolean() ? 1 : -1;
            m.vx = -Math.sin(ang) * 1.8 * dir;
            m.vz = Math.cos(ang) * 1.8 * dir;
            m.vy = -0.65;
            ACTIVE.add(m);
        }
    }
}
