package dex.passableleaves.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin extends Block {
    //todo injects, not overwrites

    // STUB
    private LeavesBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        if (pathComputationType == PathComputationType.AIR) {
            return true;
        }
        return false;
        //return super.isPathfindable(blockState, blockGetter, blockPos, pathComputationType);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.block();
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        //entity.makeStuckInBlock(blockState, new Vec3(0.25, 0.05f, 0.25)); // Can't use as it reset fall damage to 0
        var slowFactor = Vec3.ZERO;

        // Scale slowFactor with speed
        var normSpeed = entity.getDeltaMovement().normalize();
        var scaling = new Vec3(1/normSpeed.x, slowFactor.y == 1 ? 1 : 1/normSpeed.y, 1/normSpeed.z);

        if (entity instanceof Projectile) {
            slowFactor = new Vec3(0.5, 0.5, 0.5);
            slowFactor.multiply(scaling);
        } else if (entity instanceof LivingEntity living) {
            if (living.isFallFlying()) {
                // Can't reduce horizontal velocity otherwise no flight is possible
                slowFactor = new Vec3(1, 0.5, 1);

                // Damage entity for flying into a wall - those branches hurt!
                var damage = (float)((living.getDeltaMovement().length() - (living.getDeltaMovement().horizontalDistance())) * 10.0 - 3.0);
                living.hurt(DamageSource.FLY_INTO_WALL, (living.flyingSpeed * damage)/3f);
            } else {
                if (((LivingEntityAccessor) ((Object) living)).isJumping()) {
                    // Don't slow vertical velocity when jumping otherwise can't walk up a block
                    slowFactor = new Vec3(0.8, 1, 0.8);
                } else {
                    slowFactor = new Vec3(0.8, 0.8, 0.8);
                    slowFactor.multiply(scaling);
                }
            }
        } else {
            slowFactor = new Vec3(0.7, 0.7, 0.7);
            slowFactor.multiply(scaling);
        }

        // Change velocity
        ((EntityAccessor) ((Object) entity)).setStuckSpeedMultiplier(slowFactor);

        //todo stuck speed isn't great

        // Modify fallDistance and deal half fall damage
        entity.fallDistance *= 0.75f;
        if (entity.getDeltaMovement().length() > 0.1) {
            entity.causeFallDamage(entity.fallDistance, 0.5f, DamageSource.FALL);
        }

        // Play sound
        //todo azalea leaves or grass soundtype?
        var sound = SoundType.AZALEA_LEAVES;
        var rand = ThreadLocalRandom.current();

        entity.playSound(sound.getStepSound(),
                (float) (sound.getVolume() * rand.nextFloat() * entity.getDeltaMovement().length() / 1.2f),
                sound.getPitch() * rand.nextFloat());



        //todo add particles/overlay when inside?
    }

    /*@Override
    public RenderShape getRenderShape(BlockState blockState) {
        return ;
    }*/
}
