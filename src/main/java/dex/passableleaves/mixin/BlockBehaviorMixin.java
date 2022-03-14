package dex.passableleaves.mixin;

import dex.passableleaves.LeafCheck;
import dex.passableleaves.PassableLeaves;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockBehaviorMixin {

    @Shadow public abstract Block getBlock();

    @Inject(method = {"getFaceOcclusionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            "getOcclusionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"},
            at = @At("RETURN"), cancellable = true)
    private void modifyOcclusion(BlockGetter blockGetter, BlockPos blockPos, Direction direction, CallbackInfoReturnable<VoxelShape> cir) {
        if (LeafCheck.isLeaf(((BlockBehaviour.BlockStateBase) ((Object) this)))) {
            cir.setReturnValue(Shapes.block());
        }
    }

    @Inject(method = {"getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            "getShape*"},
            at = @At("RETURN"), cancellable = true)
    private void modifyCollision(BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<VoxelShape> cir) {
        if (LeafCheck.isLeaf(((BlockBehaviour.BlockStateBase) ((Object) this)))) {
            cir.setReturnValue(Shapes.block());
        }
    }

    @Inject(method = {"getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"},
            at = @At("RETURN"), cancellable = true)
    private void setCollisionShape(CallbackInfoReturnable<VoxelShape> cir) {
        if (LeafCheck.isLeaf(((BlockBehaviour.BlockStateBase) ((Object) this)))) {
            cir.setReturnValue(Shapes.empty());
        }
    }

    // ((BlockBehaviour.BlockStateBase) ((Object) this)) is what broke shadows
    /*@Inject(method = "isCollisionShapeFullBlock(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z",
    cancellable = true, at = @At("RETURN"))
    private void modifyFullShapeQuery(BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        if (LeafCheck.isLeaf(((BlockBehaviour.BlockStateBase) ((Object) this)))) {
            cir.setReturnValue(false);
        }
    }*/

    @Inject(method = {"entityInside"}, at = @At("TAIL"))
    private void modifyEntityMovement(Level level, BlockPos blockPos, Entity entity, CallbackInfo ci) {
        if (!LeafCheck.isLeaf(((BlockBehaviour.BlockStateBase) ((Object) this)))) return;
        //entity.makeStuckInBlock(blockState, new Vec3(0.25, 0.05f, 0.25)); // Can't use as it reset fall damage to 0
        var slowFactor = Vec3.ZERO;

        // Scale slowFactor with speed
        var normSpeed = entity.getDeltaMovement().normalize();
        var scaling = new Vec3(1/normSpeed.x, slowFactor.y == 1 ? 1 : 1/normSpeed.y, 1/normSpeed.z);

        if (entity instanceof Projectile) {
            slowFactor = PassableLeaves.config.projectile().toVec3();
            slowFactor.multiply(scaling);
        } else if (entity instanceof LivingEntity living) {
            if (living.isFallFlying()) {//todo projectile and gliding player are being caught
                // Can't reduce horizontal velocity too much or flaying will stop
                slowFactor = PassableLeaves.config.glidingLiving().toVec3();

                // Damage entity for flying into a wall - those branches hurt!
                var damage = (float)((living.getDeltaMovement().length() - (living.getDeltaMovement().horizontalDistance())) * 10.0 - 3.0);
                living.hurt(DamageSource.FLY_INTO_WALL, (living.flyingSpeed * damage)/3f);
            } else {
                if (((LivingEntityAccessor) ((Object) living)).isJumping()) {
                    // Don't slow vertical velocity when jumping otherwise can't walk up a block
                    slowFactor = PassableLeaves.config.jumpingLiving().toVec3();
                } else {
                    slowFactor = PassableLeaves.config.living().toVec3();
                    slowFactor.multiply(scaling);
                }
            }
        } else {
            slowFactor = PassableLeaves.config.entity().toVec3();
            slowFactor.multiply(scaling);
        }

        // Change velocity
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(slowFactor));

        // Modify fallDistance and deal half fall damage
        entity.fallDistance *= PassableLeaves.config.fallDistanceFactor();
        if (entity.getDeltaMovement().length() > 0.1) {
            entity.causeFallDamage(entity.fallDistance, PassableLeaves.config.fallDamageFactor(), DamageSource.FALL);
        }

        // Play sound
        //todo azalea leaves or grass soundtype?
        var sound = SoundType.AZALEA_LEAVES;

        var rand = ThreadLocalRandom.current();

        entity.playSound(sound.getStepSound(),
                (float) (sound.getVolume() * rand.nextFloat() * entity.getDeltaMovement().length() / 1.1f),
                sound.getPitch() * rand.nextFloat());

        // Spawn leaf particles
        if (rand.nextInt(15) != 1) {
            return;
        }
        //todo tweak position and movement, only spawn when moving, spawn more when moving faster
        double d = (double)blockPos.getX() + rand.nextDouble();
        double e = (double)blockPos.getY() - 0.05;
        double f = (double)blockPos.getZ() + rand.nextDouble();
        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, getBlock().defaultBlockState()),
                d, e, f, normSpeed.x * -4.0, 1.5, normSpeed.z * -4.0);
    }

    // Pathfinding fix
    @Inject(method = "isPathfindable", at = @At("RETURN"), cancellable = true)
    private void canPathFind(BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType, CallbackInfoReturnable<Boolean> cir) {
        if (LeafCheck.isLeaf(((BlockBehaviour.BlockStateBase) ((Object) this)))) {
            if (pathComputationType == PathComputationType.AIR) cir.setReturnValue(true);
            if (pathComputationType == PathComputationType.LAND) cir.setReturnValue(false);
        }
    }

    @Inject(method = {"isAir"}, at = @At("RETURN"), cancellable = true)
    private void modifySightForMobs(CallbackInfoReturnable<Boolean> cir) {
        if (LeafCheck.isLeaf(((BlockBehaviour.BlockStateBase) ((Object) this)))) {
            cir.setReturnValue(true);
        }
    }
}

