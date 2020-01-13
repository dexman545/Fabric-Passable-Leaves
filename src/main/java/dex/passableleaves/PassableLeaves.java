package dex.passableleaves;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.ActionResult;
import net.minecraft.util.registry.Registry;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class PassableLeaves implements ModInitializer {
	public static String config = FabricLoader.getInstance().getConfigDirectory().toString() + "/passable-leaves.cfg";
	private static final File bob = new File(config);
	public static List<String> meh = new ArrayList<String>();

	public static Optional<List<String>> getPassable() {

		ArrayList<String> b = new ArrayList<String>();

		//generate config file; removes incorrect values from existing one as well
		try {
			if (!bob.exists()) {
				FileUtils.openOutputStream(bob).write(("minecraft:oak_leaves\n" +
						"minecraft:spruce_leaves\n" +
						"minecraft:birch_leaves\n" +
						"minecraft:jungle_leaves\n" +
						"minecraft:acacia_leaves\n" +
						"minecraft:dark_oak_leaves\n").getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			Scanner s = new Scanner(bob);
			while (s.hasNext()) {
				b.add(s.next());
			}
			return Optional.of(b);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}


		return Optional.empty();
	}

	@Override
	public void onInitialize() {

		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
		{

			System.out.println(Registry.BLOCK.getId(world.getBlockState(pos).getBlock()));

			return ActionResult.PASS;
		});

		ServerStartCallback.EVENT.register(t -> {
			meh.clear();
			meh.addAll(Objects.requireNonNull(getPassable().orElse(null)));
		});

	}
}
