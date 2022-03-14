package dex.passableleaves.config;

import com.google.gson.GsonBuilder;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record Config(
        VelocityDamper projectile,
        VelocityDamper glidingLiving,
        VelocityDamper jumpingLiving,
        VelocityDamper living,
        VelocityDamper entity,
        float fallDistanceFactor,
        float fallDamageFactor
) {

    public Config() {
        this(new VelocityDamper(0.5, 0.5, 0.5),
                new VelocityDamper(0.9, 0.99996, 0.9),
                new VelocityDamper(0.8, 1, 0.8),
                new VelocityDamper(0.8, 0.8, 0.8),
                new VelocityDamper(0.7, 0.7, 0.7),
                0.75f,
                0.5f);
    }

    public void writeJson(Path path) {
        var gson = new GsonBuilder()
                .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
                .setPrettyPrinting()
                .create();
        try (var wr = Files.newBufferedWriter(path)) {
            gson.toJson(this, Config.class, wr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Config readJson(Path path) {
        var gson = new GsonBuilder()
                .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
                .setLenient()
                .create();

        try {
            return gson.fromJson(Files.readString(path), Config.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Config();
    }

    public record VelocityDamper(float v_x, float v_y, float v_z) {
        public VelocityDamper(double v_x, double v_y, double v_z) {
            this((float) v_x, (float) v_y, (float) v_z);
        }

        public Vec3 toVec3() {
            return new Vec3(v_x, v_y, v_z);
        }
    }
}
