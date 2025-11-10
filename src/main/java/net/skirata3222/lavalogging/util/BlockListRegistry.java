package net.skirata3222.lavalogging.util;

import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockListRegistry {
	private static Set<Identifier> serverList = Set.of();
	private static Set<Identifier> clientList = Set.of();

	public static void setServerList(Set<Identifier> list) {
		serverList = list;
	}

	public static void setClientList(Set<Identifier> list) {
		clientList = list;
	}

	public static boolean isAllowed(Block block) {
		Identifier id = Registries.BLOCK.getId(block);
		// Prefer server list if available
		if (!serverList.isEmpty()) {
			return serverList.contains(id);
		}
		// Fallback to client list
		return clientList.contains(id);
	}

}
