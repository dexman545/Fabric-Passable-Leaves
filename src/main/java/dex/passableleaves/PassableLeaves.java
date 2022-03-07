package dex.passableleaves;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class PassableLeaves implements ModInitializer {
	public static String config = FabricLoader.getInstance().getConfigDir().resolve("passable-leaves.cfg").toString();

    @Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(t -> {

		});

	}
}
