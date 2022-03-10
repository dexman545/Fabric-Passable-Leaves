package dex.passableleaves;

import dex.passableleaves.config.Config;
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
import java.nio.file.*;
import java.util.*;

public class PassableLeaves implements ModInitializer {
	public static final Path configFile = FabricLoader.getInstance().getConfigDir().resolve("passable-leaves.json");
	public static final Config config = Config.readJson(configFile);

    @Override
	public void onInitialize() {
		try {
			Files.createDirectories(configFile.getParent());
			if (Files.notExists(configFile)) {
				Files.createFile(configFile);
			}
			config.writeJson(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
