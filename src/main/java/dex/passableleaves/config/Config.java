package dex.passableleaves.config;

public record Config(
        VelocityDamper projectile,
        VelocityDamper glidingLiving,
        VelocityDamper jumpingLiving,
        VelocityDamper living,
        VelocityDamper entity,
        float fallDistanceFactor,
        float fallDamageFactor
) {
}

record VelocityDamper(float v_x, float v_y, float v_z) {}
