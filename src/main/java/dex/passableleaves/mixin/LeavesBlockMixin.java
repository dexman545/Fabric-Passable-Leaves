package dex.passableleaves.mixin;

import dex.passableleaves.PassableLeaves;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class LeavesBlockMixin {

	@Shadow public abstract Block getBlock();

	private List<String> getPassable() {
		return PassableLeaves.meh;
	}

	// fix better nether
	private boolean checkTagExists() {
		if (BlockTags.LEAVES != null) {
			try {
				BlockTags.LEAVES.values();
				return true;
			} catch (Exception e) {
				return false;
			}
		}


		return false;
	}

	@Inject(at=@At("HEAD"), method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;", cancellable = true)
	private void getCollisionShape(BlockView view, BlockPos pos, ShapeContext ePos, CallbackInfoReturnable<VoxelShape> cir) {
		if (checkTagExists()) {
			if (BlockTags.LEAVES.contains(this.getBlock()) || getPassable().contains(Registry.BLOCK.getId(this.getBlock()).toString())) {
				cir.setReturnValue(VoxelShapes.empty());
			}
		}
	}

	@Inject(at=@At("HEAD"), method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;", cancellable = true)
	private void getCollisionShape(BlockView view, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
		if (checkTagExists()) {
			if (BlockTags.LEAVES.contains(this.getBlock()) || getPassable().contains(Registry.BLOCK.getId(this.getBlock()).toString())) {
				cir.setReturnValue(VoxelShapes.empty());
			}
		}
	}

	@Inject(at=@At("HEAD"), method = "onEntityCollision")
	private void onEntityCollision(World world, BlockPos pos, Entity entity, CallbackInfo ci) {
		if (checkTagExists()) {
			if (BlockTags.LEAVES.contains(this.getBlock()) || getPassable().contains(Registry.BLOCK.getId(this.getBlock()).toString())) {
				entity.fallDistance = entity.fallDistance * 0.95f;
				Vec3d oldVel = entity.getVelocity();
				if (entity instanceof PlayerEntity) {
					if (((PlayerEntity) entity).isFallFlying()) {
						//can't reduce horizontal velocity otherwise no flight is possible
						entity.setVelocity(oldVel.multiply(1.0D, (oldVel.y > 0 ? 1.0D : 0.5D), 1.0D));
						entity.damage(DamageSource.FLY_INTO_WALL, (float) (entity.getVelocity().length() / .25D) * 0.5f);
					} else {
						entity.setVelocity(oldVel.multiply(0.8D, (oldVel.y > 0 ? 1.0D : 0.5D), 0.8D));
					}
				} else if (entity instanceof ProjectileEntity) {
					entity.setVelocity(oldVel.multiply(0.5D));

				} else {
					entity.setVelocity(oldVel.multiply(0.7D, (oldVel.y > 0 ? 1.0D : 0.5D), 0.7D));
				}
				if (entity.getVelocity().length() > 0.1D) {
					entity.handleFallDamage(entity.fallDistance, 0.5f, DamageSource.FALL);
				}
			}
		}
	}

	// Pathfinding patch
	@Inject(at=@At("HEAD"), method = "canPathfindThrough(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/ai/pathing/NavigationType;)Z", cancellable = true)
	private void changePathfinding(BlockView world, BlockPos pos, NavigationType type, CallbackInfoReturnable<Boolean> cir) {
		if (checkTagExists()) {
			if (BlockTags.LEAVES.contains(this.getBlock()) || getPassable().contains(Registry.BLOCK.getId(this.getBlock()).toString())) {
				cir.setReturnValue(true);
			}
		}
	}



}

