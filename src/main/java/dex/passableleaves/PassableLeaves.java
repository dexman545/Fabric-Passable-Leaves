package dex.passableleaves;

import dex.passableleaves.config.Config;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
