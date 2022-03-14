package dex.passableleaves;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Optional;

public class LeafCheck {
    public static boolean isLeaf(BlockBehaviour.BlockStateBase bh) {
        try {
            if (getMcVer().compareTo(SemanticVersion.parse("1.18.2")) >= 0) {
                return keyedCheck(bh);
            }
        } catch (VersionParsingException e) {
            e.printStackTrace();
        }

        return fallbackCheck(bh);
    }

    private static boolean keyedCheck(BlockBehaviour.BlockStateBase bh) {
        return bh.is(BlockTags.LEAVES);
    }

    private static boolean fallbackCheck(BlockBehaviour.BlockStateBase bh) {
        return bh.getBlock() instanceof LeavesBlock;
    }

    private static Version getMcVer() {
        try {
            return FabricLoader.getInstance().getModContainer("minecraft")
                    .flatMap(modContainer -> Optional.ofNullable(modContainer.getMetadata().getVersion())).orElse(SemanticVersion.parse("1.18.1"));
        } catch (VersionParsingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
