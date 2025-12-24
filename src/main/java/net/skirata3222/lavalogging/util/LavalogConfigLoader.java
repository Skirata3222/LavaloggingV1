package net.skirata3222.lavalogging.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


public class LavalogConfigLoader {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static Set<Block> BLOCKLIST = Set.of();

	public static void load() {
		Path configDir = FabricLoader.getInstance().getConfigDir().resolve("lavalogging");
		Path configFile = configDir.resolve("blocklist.json");

		try {
			if (Files.notExists(configDir)) {
				Files.createDirectories(configDir);
			}

			if (Files.notExists(configFile)) {
				createDefault(configFile);
			}

			readConfig(configFile);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load blocklist config", e);
		}

	}

	private static void createDefault(Path path) throws IOException {
		JsonObject root = new JsonObject();
		JsonArray blocks = new JsonArray();

		blocks.add("minecraft:cobblestone_slab");
		// add default blocks here

		root.add("blocks", blocks);

		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			GSON.toJson(root, writer);
		}
	}

	private static void readConfig(Path path) throws IOException {
		try (Reader reader = Files.newBufferedReader(path)) {
			JsonElement element = GSON.fromJson(reader, JsonElement.class);
			if (!element.isJsonObject()) {
				throw new IllegalStateException("blocklist.json root must be an object");
			}

			JsonObject obj = element.getAsJsonObject();
			JsonArray blocksArray = obj.getAsJsonArray("blocks");

			Set<Block> result = new HashSet<>();

			if (blocksArray != null) {
				for (JsonElement el : blocksArray) {
					if (!el.isJsonPrimitive() || !el.getAsJsonPrimitive().isString()) {
						continue; // skip bad entries instead of crashing
					}

					String idString = el.getAsString();
					ResourceLocation id = ResourceLocation.parse(idString);
					Optional<Reference<Block>> opt = BuiltInRegistries.BLOCK.get(id);

					if (opt.isPresent()) {
						Block block = opt.get().value();
						result.add(block);
					} else {
						System.err.println("[Lavaloggable] Unknown block id in blocklist.json: " + idString);
					}
				}
			}

			BLOCKLIST = Set.copyOf(result);
		}
	}

}
