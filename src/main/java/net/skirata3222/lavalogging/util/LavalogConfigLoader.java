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
		
		blocks.add("minecraft:andesite_slab");
		blocks.add("minecraft:andesite_stairs");
		blocks.add("minecraft:andesite_wall");

		blocks.add("minecraft:anvil");
		blocks.add("minecraft:chipped_anvil");
		blocks.add("minecraft:damaged_anvil");

		blocks.add("minecraft:brick_slab");
		blocks.add("minecraft:brick_stairs");
		blocks.add("minecraft:brick_wall");

		blocks.add("minecraft:cobblestone_slab");
		blocks.add("minecraft:cobblestone_stairs");
		blocks.add("minecraft:cobblestone_wall");

		blocks.add("minecraft:copper_bars");
		blocks.add("minecraft:exposed_copper_bars");
		blocks.add("minecraft:weathered_copper_bars");
		blocks.add("minecraft:oxidized_copper_bars");
		blocks.add("minecraft:waxed_copper_bars");
		blocks.add("minecraft:waxed_exposed_copper_bars");
		blocks.add("minecraft:waxed_weathered_copper_bars");
		blocks.add("minecraft:waxed_oxidized_copper_bars");

		blocks.add("minecraft:copper_grate");
		blocks.add("minecraft:exposed_copper_grate");
		blocks.add("minecraft:weathered_copper_grate");
		blocks.add("minecraft:oxidized_copper_grate");
		blocks.add("minecraft:waxed_copper_grate");
		blocks.add("minecraft:waxed_exposed_copper_grate");
		blocks.add("minecraft:waxed_weathered_copper_grate");
		blocks.add("minecraft:waxed_oxidized_copper_grate");

		blocks.add("minecraft:cut_red_sandstone_slab");
		blocks.add("minecraft:cut_sandstone_slab");

		blocks.add("minecraft:deepslate_brick_slab");
		blocks.add("minecraft:deepslate_brick_stairs");
		blocks.add("minecraft:deepslate_brick_wall");

		blocks.add("minecraft:deepslate_tile_slab");
		blocks.add("minecraft:deepslate_tile_stairs");
		blocks.add("minecraft:deepslate_tile_wall");

		blocks.add("minecraft:diorite_slab");
		blocks.add("minecraft:diorite_stairs");
		blocks.add("minecraft:diorite_wall");

		blocks.add("minecraft:end_stone_brick_slab");
		blocks.add("minecraft:end_stone_brick_stairs");
		blocks.add("minecraft:end_stone_brick_wall");

		blocks.add("minecraft:granite_slab");
		blocks.add("minecraft:granite_stairs");
		blocks.add("minecraft:granite_wall");

		blocks.add("minecraft:iron_bars");

		blocks.add("minecraft:mossy_cobblestone_slab");
		blocks.add("minecraft:mossy_cobblestone_stairs");
		blocks.add("minecraft:mossy_cobblestone_wall");

		blocks.add("minecraft:mossy_stone_brick_slab");
		blocks.add("minecraft:mossy_stone_brick_stairs");
		blocks.add("minecraft:mossy_stone_brick_wall");

		blocks.add("minecraft:nether_brick_fence");
		blocks.add("minecraft:nether_brick_slab");
		blocks.add("minecraft:nether_brick_stairs");
		blocks.add("minecraft:nether_brick_wall");

		blocks.add("minecraft:pointed_dripstone");

		blocks.add("minecraft:polished_andesite_slab");
		blocks.add("minecraft:polished_andesite_stairs");

		blocks.add("minecraft:polished_blackstone_brick_slab");
		blocks.add("minecraft:polished_blackstone_brick_stairs");
		blocks.add("minecraft:polished_blackstone_brick_wall");

		blocks.add("minecraft:polished_blackstone_slab");
		blocks.add("minecraft:polished_blackstone_stairs");
		blocks.add("minecraft:polished_blackstone_wall");

		blocks.add("minecraft:polished_diorite_slab");
		blocks.add("minecraft:polished_diorite_stairs");

		blocks.add("minecraft:polished_granite_slab");
		blocks.add("minecraft:polished_granite_stairs");

		blocks.add("minecraft:prismarine_brick_slab");
		blocks.add("minecraft:prismarine_brick_stairs");

		blocks.add("minecraft:prismarine_slab");
		blocks.add("minecraft:prismarine_stairs");
		blocks.add("minecraft:prismarine_wall");

		blocks.add("minecraft:quartz_slab");
		blocks.add("minecraft:quartz_stairs");

		blocks.add("minecraft:red_nether_brick_slab");
		blocks.add("minecraft:red_nether_brick_stairs");
		blocks.add("minecraft:red_nether_brick_wall");

		blocks.add("minecraft:red_sandstone_slab");
		blocks.add("minecraft:red_sandstone_stairs");
		blocks.add("minecraft:red_sandstone_wall");

		blocks.add("minecraft:sandstone_slab");
		blocks.add("minecraft:sandstone_stairs");
		blocks.add("minecraft:sandstone_wall");

		blocks.add("minecraft:smooth_quartz_slab");
		blocks.add("minecraft:smooth_quartz_stairs");

		blocks.add("minecraft:smooth_red_sandstone_slab");
		blocks.add("minecraft:smooth_red_sandstone_stairs");

		blocks.add("minecraft:smooth_sandstone_slab");
		blocks.add("minecraft:smooth_sandstone_stairs");

		blocks.add("minecraft:smooth_stone_slab");

		blocks.add("minecraft:stone_brick_slab");
		blocks.add("minecraft:stone_brick_stairs");
		blocks.add("minecraft:stone_brick_wall");

		blocks.add("minecraft:stone_slab");
		blocks.add("minecraft:stone_stairs");

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
