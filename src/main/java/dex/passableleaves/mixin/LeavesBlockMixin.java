package dex.passableleaves.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin extends Block {

	@Inject(method = "<init>(Lnet/minecraft/block/Block$Settings;)V", at = @At("RETURN"))
	public void LeavesBlockBlah(Settings settings, CallbackInfo ci) {
		((BlockAccessMixin) this).setCollidable(false);
	}

	public LeavesBlockMixin(Settings settings) {
		super(settings.noCollision());
	}

	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		//entity.slowMovement(state, new Vec3d(0.8D, 0.8D, 0.8D));
		/*((EntityAccessMixin) entity).setMovementMultiplier(new Vec3d(0.8D, 0.9D, 0.8D));*/
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

