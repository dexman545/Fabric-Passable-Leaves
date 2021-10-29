package dex.passableleaves.mixin;

import dex.passableleaves.PassableLeaves;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockCollisionSpliterator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(BlockCollisionSpliterator.class)
public abstract class BlockCollisionSpliteratorMixin {
    @Inject(method = "offerBlockShape(Ljava/util/function/Consumer;)Z", at = @At("HEAD"), cancellable = true)
    private void callOfferBlockShape(Consumer<? super VoxelShape> action, CallbackInfoReturnable<Boolean> cir) {
        //PassableLeaves.isPassable(getBlock());
        //cir.setReturnValue(false);
    }

    @Inject(method = "method_30031(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private static void modifyPredicate(BlockState state, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        //PassableLeaves.isPassable(getBlock());
        //cir.setReturnValue(false);
    }


}
