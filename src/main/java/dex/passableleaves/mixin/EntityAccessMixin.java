package dex.passableleaves.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessMixin {
    @Accessor("movementMultiplier")
    void setMovementMultiplier(Vec3d movementMultiplier);
}
