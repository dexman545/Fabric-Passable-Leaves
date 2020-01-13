package dex.passableleaves.mixin;

import dex.passableleaves.PassableLeaves;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
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

@Mixin(BlockState.class)
public abstract class LeavesBlockMixin {

	@Shadow public abstract Block getBlock();

	private List<String> getPassable() {
		return PassableLeaves.meh;
	}

	@Inject(at=@At("HEAD"), method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/EntityContext;)Lnet/minecraft/util/shape/VoxelShape;", cancellable = true)
	public void getCollisionShape(BlockView view, BlockPos pos, EntityContext ePos, CallbackInfoReturnable<VoxelShape> cir) {
		//for (String id : PassableLeaves.CONFIG.passableBlocks()) {
		//if (!getPassable().isEmpty()) {
			if (BlockTags.LEAVES.contains(this.getBlock()) || getPassable().contains(Registry.BLOCK.getId(this.getBlock()).toString())) {
			//if (Registry.BLOCK.getId(this.getBlock()).equals(Identifier.tryParse(PassableLeaves.CONFIG.passableBlocks().get(0)))) {
				cir.setReturnValue(VoxelShapes.empty());
				//}
			}
		//}
		//}
	}

	@Inject(at=@At("HEAD"), method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/shape/VoxelShape;", cancellable = true)
	public void getCollisionShape(BlockView view, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
		if (BlockTags.LEAVES.contains(this.getBlock()) || getPassable().contains(Registry.BLOCK.getId(this.getBlock()).toString())) {
				cir.setReturnValue(VoxelShapes.empty());
			}
	}

	@Inject(at=@At("HEAD"), method = "onEntityCollision")
	public void onEntityCollision(World world, BlockPos pos, Entity entity, CallbackInfo ci) {
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
					entity.handleFallDamage(entity.fallDistance, 0.5f);
				}
			}
	}



}

