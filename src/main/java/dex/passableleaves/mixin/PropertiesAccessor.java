package dex.passableleaves.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockBehaviour.Properties.class)
public interface PropertiesAccessor {
    @Accessor
    boolean isCanOcclude();

    @Accessor
    void setCanOcclude(boolean canOcclude);
}
