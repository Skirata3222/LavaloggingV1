package net.skirata3222.lavalogging.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class BlockListParser {
	public static Set<Identifier> parse(ResourceManager manager, String path) {
		Set<Identifier> result = new HashSet<>();
		try {
			Optional<Resource> opt = manager.getResource(Identifier.of("lavalogging", path));
			if (opt.isPresent()) {
				try (InputStream in = opt.get().getInputStream();
					Reader reader = new InputStreamReader(in)) {
					JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
					JsonArray arr = json.getAsJsonArray("values");
					for (JsonElement el : arr) {
						result.add(Identifier.of(el.getAsString()));
					}
				}
			}
		} catch (Exception e) {
			// might put logging here eventually
		}
		return result;
	}

}
