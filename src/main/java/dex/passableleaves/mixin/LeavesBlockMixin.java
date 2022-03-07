package dex.passableleaves.mixin;

import dex.passableleaves.PassableLeaves;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class LeavesBlockMixin {

	@Shadow public abstract Block getBlock();

	@Inject(at=@At("HEAD"), method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;", cancellable = true)
	private void getCollisionShape(BlockGetter view, BlockPos pos, CollisionContext ePos, CallbackInfoReturnable<VoxelShape> cir) {
		if (PassableLeaves.checkTagExists()) {
			if (BlockTags.LEAVES.contains(this.getBlock()) || PassableLeaves.getPassable().contains(Registry.BLOCK.getKey(this.getBlock()).toString())) {
				cir.setReturnValue(Shapes.empty());
			}
		}
	}

	@Inject(at=@At("HEAD"), method = "onEntityCollision")
	private void onEntityCollision(Level world, BlockPos pos, Entity entity, CallbackInfo ci) {
		if (PassableLeaves.checkTagExists()) {
			if (PassableLeaves.isPassable(getBlock())) {
				entity.fallDistance = entity.fallDistance * 0.95f;
				Vec3 oldVel = entity.getDeltaMovement();
				if (entity instanceof Player) {
					if (((Player) entity).isFallFlying()) {
						//can't reduce horizontal velocity otherwise no flight is possible
						entity.setDeltaMovement(oldVel.multiply(1.0D, (oldVel.y > 0 ? 1.0D : 0.5D), 1.0D));
						entity.hurt(DamageSource.FLY_INTO_WALL, (float) (entity.getDeltaMovement().length() / .25D) * 0.5f);
					} else {
						entity.setDeltaMovement(oldVel.multiply(0.8D, (oldVel.y > 0 ? 1.0D : 0.5D), 0.8D));
					}
				} else if (entity instanceof Projectile) {
					entity.setDeltaMovement(oldVel.scale(0.5D));

				} else {
					entity.setDeltaMovement(oldVel.multiply(0.7D, (oldVel.y > 0 ? 1.0D : 0.5D), 0.7D));
				}
				if (entity.getDeltaMovement().length() > 0.1D) {
					entity.causeFallDamage(entity.fallDistance, 0.5f, DamageSource.FALL);
				}
			}
		}
	}

	// Pathfinding patch
	@Inject(at=@At("HEAD"), method = "canPathfindThrough(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/ai/pathing/NavigationType;)Z", cancellable = true)
	private void changePathfinding(BlockGetter world, BlockPos pos, PathComputationType type, CallbackInfoReturnable<Boolean> cir) {
		if (PassableLeaves.checkTagExists()) {
			if (PassableLeaves.isPassable(getBlock())) {
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(at=@At("HEAD"), method = "canPathfindThrough(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/ai/pathing/NavigationType;)Z", cancellable = true)
	private void changePathing2(BlockGetter world, BlockPos pos, PathComputationType type, CallbackInfoReturnable<Boolean> cir) {
		if (PassableLeaves.checkTagExists()) {
			if (PassableLeaves.isPassable(getBlock())) {
				cir.setReturnValue(true);
			}
		}
	}

}

