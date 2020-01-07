package dex.passableleaves.mixin;

import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Block.class)
public interface BlockAccessMixin {
	@Accessor("collidable")
	void setCollidable(boolean collidable);
}
