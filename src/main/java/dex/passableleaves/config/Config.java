package dex.passableleaves.config;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public interface Config {
    double getVelocityDamping();

    Set<Block> passableBlocks();

    Set<TagKey<Block>> passableTags();
}
